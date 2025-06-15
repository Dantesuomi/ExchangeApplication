package com.assignment.ExchangeApplication.service;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.enums.TransactionOperation;
import com.assignment.ExchangeApplication.enums.TransferStatus;
import com.assignment.ExchangeApplication.enums.TransferType;
import com.assignment.ExchangeApplication.exceptions.FailedAccountUpdateException;
import com.assignment.ExchangeApplication.exceptions.NegativeAmountException;
import com.assignment.ExchangeApplication.exceptions.PermissionDeniedException;
import com.assignment.ExchangeApplication.model.Account;
import com.assignment.ExchangeApplication.model.Client;
import com.assignment.ExchangeApplication.model.Transaction;
import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.model.dto.TransactionRequest;
import com.assignment.ExchangeApplication.model.dto.TransferRequest;
import com.assignment.ExchangeApplication.model.dto.TransferResult;
import com.assignment.ExchangeApplication.repository.TransactionRepository;
import com.assignment.ExchangeApplication.service.interfaces.AccountService;
import com.assignment.ExchangeApplication.service.interfaces.CurrencyExchangeService;
import com.assignment.ExchangeApplication.service.interfaces.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.*;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);


    private final AccountService accountService;
    private final CurrencyExchangeService currencyExchangeService;
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(AccountService accountService,
                                  CurrencyExchangeService currencyExchangeService,
                                  TransactionRepository transactionRepository
    ) {
        this.accountService = accountService;
        this.currencyExchangeService = currencyExchangeService;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public AccountResponseDto depositAccount(Authentication authentication, TransactionRequest request) {
        log.info("Initiating deposit request for IBAN: {}", request.getAccountIban());
        Account account;
        try {
            account = accountService.getAccountByIban(request.getAccountIban());
        }
        catch (NoSuchElementException e) {
            log.warn("Account not found for IBAN: {}", request.getAccountIban());
            throw new EntityNotFoundException(ACCOUNT_NOT_FOUND_ERROR);
        }

        if (!doesAccountBelongsToRequester(authentication, account)){
            log.warn("Unauthorized deposit attempt on account IBAN: {}",
                    request.getAccountIban());
            throw new PermissionDeniedException(UNAUTHORIZED_ACCOUNT_ERROR);
        }
                BigDecimal amountToDeposit = request.getAmount();
        BigDecimal currentAccountBalance = account.getBalance();
        BigDecimal newAccountBalance = currentAccountBalance.add(amountToDeposit);

        account.setBalance(newAccountBalance);
        accountService.updateAccount(account);
        log.debug("Account balance updated for IBAN: {}", request.getAccountIban());

        Transaction transaction = new Transaction();
        transaction.setDestinationAccount(account);
        transaction.setDestinationAmountCredited(amountToDeposit);
        transaction.setSourceAmountDebited(BigDecimal.ZERO);
        transaction.setDestinationCurrencyCode(account.getCurrency());
        transaction.setTransactionOperation(TransactionOperation.DEPOSIT);
        transaction.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
        transactionRepository.save(transaction);

        log.info("Transaction record created for deposit to IBAN: {}", request.getAccountIban());

        return new AccountResponseDto(account);
    }

    @Override
    @Transactional
    public AccountResponseDto withdrawAccount(Authentication authentication, TransactionRequest request) {
        log.info("Initiating withdrawal request for IBAN: {}", request.getAccountIban());

        Account account;
        try {
            account = accountService.getAccountByIban(request.getAccountIban());
        }
        catch (NoSuchElementException e) {
            log.warn("Account not found for IBAN: {}", request.getAccountIban());
            throw new EntityNotFoundException(ACCOUNT_NOT_FOUND_ERROR);
        }

        if (!doesAccountBelongsToRequester(authentication, account)){
            log.warn("Unauthorized withdrawal attempt on account IBAN: {}",
                    request.getAccountIban());
            throw new PermissionDeniedException(UNAUTHORIZED_ACCOUNT_ERROR);
        }
        BigDecimal amountToWithdraw = request.getAmount();
        BigDecimal currentAccountBalance = account.getBalance();
        BigDecimal newAccountBalance = currentAccountBalance.subtract(amountToWithdraw);
        if(newAccountBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeAmountException(INSUFFICIENT_BALANCE_ERROR);
        }
        account.setBalance(newAccountBalance);
        accountService.updateAccount(account);
        log.debug("Account balance updated for withdrawal. IBAN: {}", request.getAccountIban());

        Transaction transaction = new Transaction();
        transaction.setSourceAccount(account);
        transaction.setSourceAmountDebited(amountToWithdraw);
        transaction.setDestinationAmountCredited(BigDecimal.ZERO);
        transaction.setSourceCurrencyCode(account.getCurrency());
        transaction.setTransactionOperation(TransactionOperation.WITHDRAWAL);
        transaction.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
        transactionRepository.save(transaction);

        log.info("Transaction record created for withdrawal from IBAN: {}", request.getAccountIban());

        return new AccountResponseDto(account);
    }

    @Override
    @Transactional
    public TransferResult transferBetweenAccounts (Authentication authentication, TransferRequest transferRequest){
        String sourceIban = transferRequest.getSourceAccountNumber();
        String destinationIban = transferRequest.getDestinationAccountNumber();

        log.info("Initiating transfer from {} to {}", sourceIban, destinationIban);

        Account sourceAccount = getAccount(sourceIban);
        if (sourceAccount == null) {
            log.warn("Source account not found for IBAN: {}", sourceIban);
            return generateFailedTransfer(SOURCE_ACCOUNT_NOT_FOUND_ERROR);
        }
        if (!doesAccountBelongsToRequester(authentication, sourceAccount)) {
            log.warn("Unauthorized access attempt on account ID: {} by user: {}", sourceAccount.getId(), authentication.getName());
            return generateFailedTransfer(UNAUTHORIZED_ACCOUNT_ERROR);
        }

        Account destinationAccount = getAccount(destinationIban);
        if (destinationAccount == null) {
            log.warn("Destination account not found for IBAN: {}", destinationIban);
            return generateFailedTransfer(DESTINATION_ACCOUNT_NOT_FOUND_ERROR);
        }

        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            log.warn("Transfer attempt between identical accounts: {}", sourceAccount.getId());
            return generateFailedTransfer(IDENTICAL_SOURCE_AND_DESTINATION_ACCOUNT_ERROR);
        }

        if (!doesCurrencyMatchesAccount(destinationAccount, transferRequest.getDestinationCurrency())) {
            log.warn("Currency mismatch: requested {}, account supports {}", transferRequest.getDestinationCurrency(), destinationAccount.getCurrency());
            return generateFailedTransfer(INVALID_CURRENCY_ERROR);
        }

        Transaction transaction = generateTransaction(sourceAccount, destinationAccount, transferRequest);
        return executeAccountTransfer(transaction);
    }

    @Override
    public Page<Transaction> getTransactionsForAccount(Authentication authentication, UUID accountId, Pageable pageable) {
        log.info("Getting transactions for account ID: {}", accountId);

        Optional<Account> optionalAccount = accountService.getAccountById(accountId);
        Account account = optionalAccount.orElseThrow(() -> {
            log.warn("Account not found for ID: {}", accountId);
            return new EntityNotFoundException(ACCOUNT_NOT_FOUND_ERROR);
        });

        if (!doesAccountBelongsToRequester(authentication, account)){
            log.warn("Unauthorized access attempt to transactions of account ID: {} ", accountId);
            throw new PermissionDeniedException(UNAUTHORIZED_ACCOUNT_ERROR);
        }
        Page<Transaction> transactions = transactionRepository.findBySourceOrDestinationAccount(accountId, pageable);
        log.info("Retrieving transactions for account ID: {}", accountId);
        transactions.getContent().forEach(transaction -> {
            if (transaction.getSourceAccount() != null && transaction.getSourceAccount().getId().equals(accountId)) {
                transaction.setTransferType(TransferType.SENT);
            } else {
                transaction.setTransferType(TransferType.RECEIVED);
            }
        });

        return transactions;
    }

    private Boolean doesAccountBelongsToRequester(Authentication authentication, Account account){
        UUID accountOwner = account.getClient().getId();
        Client client = (Client) authentication.getPrincipal();
        UUID authorizedClientId = client.getId();
        return accountOwner.equals(authorizedClientId);
    }

    private Boolean doesCurrencyMatchesAccount(Account account, CurrencyCode currencyCode){
        CurrencyCode accountCurrency = account.getCurrency();
        return accountCurrency.equals(currencyCode);
    }

    private void updateAccountBalance(Account account, BigDecimal accountBalance) throws Exception {
        account.setBalance(accountBalance);
    }

    private Transaction generateTransaction(Account sourceAccount, Account destinationAccount, TransferRequest transferRequest) {

        log.info("Generating transaction from source IBAN: {} to destination IBAN: {} ", sourceAccount.getIban(), destinationAccount.getIban());

        Transaction transaction = new Transaction();

        BigDecimal amountToTransferInSourceCurrency;
        if (doesCurrencyMatchesAccount(sourceAccount, transferRequest.getDestinationCurrency())) {
            log.debug("Currency matches source account currency ({}). No conversion needed.", sourceAccount.getCurrency());
            amountToTransferInSourceCurrency = transferRequest.getAmount();
        } else {
            log.debug("Currency mismatch detected. Converting from {} to {}.",
                     sourceAccount.getCurrency(), transferRequest.getDestinationCurrency());
            Map<CurrencyCode, BigDecimal> exchangeRates = currencyExchangeService.getExchangeRates(transferRequest.getDestinationCurrency());
            CurrencyCode sourceAccountCurrencyCode = sourceAccount.getCurrency();
            BigDecimal sourceCurrencyExchangeRate = exchangeRates.get(sourceAccountCurrencyCode);
            amountToTransferInSourceCurrency = transferRequest.getAmount().multiply(sourceCurrencyExchangeRate);
            log.debug("Converted amount to destination currency ({}): ", transferRequest.getDestinationCurrency());
        }

        transaction.setDescription(transferRequest.getDescription());
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setSourceCurrencyCode(sourceAccount.getCurrency());
        transaction.setDestinationCurrencyCode(transferRequest.getDestinationCurrency());
        transaction.setSourceAmountDebited(amountToTransferInSourceCurrency);
        transaction.setDestinationAmountCredited(transferRequest.getAmount());
        transaction.setTransactionOperation(TransactionOperation.TRANSFER);

        log.info("Transaction generated");

        return transaction;
    }

    private TransferResult executeAccountTransfer(Transaction transaction){
        Account sourceAccount = transaction.getSourceAccount();
        Account destinationAccount = transaction.getDestinationAccount();
        BigDecimal sourceAccountBalance = sourceAccount.getBalance();
        BigDecimal amountToTransferInSourceCurrency = transaction.getSourceAmountDebited();
        BigDecimal newSourceAccountBalance = sourceAccountBalance.subtract(amountToTransferInSourceCurrency);

        if(newSourceAccountBalance.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Transfer failed. Insufficient funds for Account : {})", sourceAccount.getId());
            return new TransferResult(TransferStatus.FAILED, INSUFFICIENT_BALANCE_ERROR);
        }
        BigDecimal destinationAccountBalance = destinationAccount.getBalance();
        BigDecimal amountToTransferInDestinationCurrency = transaction.getDestinationAmountCredited();
        BigDecimal newDestinationAccountBalance = destinationAccountBalance.add(amountToTransferInDestinationCurrency);
        transaction.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));


        try {
            updateAccountBalance(sourceAccount, newSourceAccountBalance);
            log.info("Source account {} balance updated",
                    sourceAccount.getId());
        } catch (Exception e) {
            log.error("Failed to update balance for source account {}", e.getMessage(), e);
            throw new FailedAccountUpdateException(e.getMessage());
        }

        try {
            updateAccountBalance(destinationAccount, newDestinationAccountBalance);
            log.info("Destination account {} balance updated",
                    destinationAccount.getId());
        } catch (Exception e) {
            log.error("Failed to update balance for destination account {}", e.getMessage(), e);
            throw new FailedAccountUpdateException(e.getMessage());
        }

        transactionRepository.save(transaction);

        log.info("Transfer successful: debited from {}, credited to {}",
                sourceAccount.getId(), destinationAccount.getId());
        return new TransferResult(TransferStatus.SUCCESSFUL, TRANSFER_SUCCEEDED);
    }

    private Account getAccount(String iban) {
        try {
            return accountService.getAccountByIban(iban);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private TransferResult generateFailedTransfer(String errorMessage) {
        return new TransferResult(TransferStatus.FAILED, errorMessage);
    }
}
