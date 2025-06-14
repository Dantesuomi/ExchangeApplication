package com.assignment.ExchangeApplication.service;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.enums.TransactionOperation;
import com.assignment.ExchangeApplication.enums.TransferStatus;
import com.assignment.ExchangeApplication.enums.TransferType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.*;

@Service
public class TransactionServiceImpl implements TransactionService {

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
    public AccountResponseDto depositAccount(Authentication authentication, TransactionRequest request) {
        Account account = accountService.getAccountByIban(request.getAccountIban());
        if (!doesAccountBelongsToRequester(authentication, account)){
            throw new PermissionDeniedException(UNAUTHORIZED_ACCOUNT_ERROR);
        }

        BigDecimal amountToDeposit = request.getAmount();
        BigDecimal currentAccountBalance = account.getBalance();
        BigDecimal newAccountBalance = currentAccountBalance.add(amountToDeposit);

        account.setBalance(newAccountBalance);
        accountService.updateAccount(account);

        return new AccountResponseDto(account);
    }

    @Override
    public AccountResponseDto withdrawAccount(Authentication authentication, TransactionRequest request) {
        Account account = accountService.getAccountByIban(request.getAccountIban());
        if (!doesAccountBelongsToRequester(authentication, account)){
            throw new PermissionDeniedException(UNAUTHORIZED_ACCOUNT_ERROR);
        }
        BigDecimal amountToWithdraw = request.getAmount();
        BigDecimal currentAccountBalance = account.getBalance();
        BigDecimal newAccountBalance = currentAccountBalance.subtract(amountToWithdraw);
        if(newAccountBalance.compareTo(amountToWithdraw) < 0) {
            throw new NegativeAmountException(INSUFFICIENT_BALANCE_ERROR);
        }
        account.setBalance(newAccountBalance);
        accountService.updateAccount(account);

        return new AccountResponseDto(account);
    }

    @Override
    public TransferResult transferBetweenAccounts (Authentication authentication, TransferRequest transferRequest){
        Account sourceAccount = accountService.getAccountByIban(transferRequest.getSourceAccountNumber());
        if (!doesAccountBelongsToRequester(authentication, sourceAccount)){
            return new TransferResult(TransferStatus.FAILED, UNAUTHORIZED_ACCOUNT_ERROR);
        }

        Account destinationAccount = accountService.getAccountByIban(transferRequest.getDestinationAccountNumber());
        if (!doesCurrencyMatchesAccount(destinationAccount, transferRequest.getDestinationCurrency())){
            return new TransferResult(TransferStatus.FAILED, INVALID_CURRENCY_ERROR);
        }

        Transaction transaction = generateTransaction(sourceAccount, destinationAccount, transferRequest);
        return executeAccountTransfer(transaction);
    }

    @Override
    public Page<Transaction> getTransactionsForAccount(Authentication authentication, UUID accountId, Pageable pageable) {
        Optional<Account> optionalAccount = accountService.getAccountById(accountId);
        Account account = optionalAccount.orElseThrow(() -> new EntityNotFoundException(ACCOUNT_NOT_FOUND_ERROR));
        if (!doesAccountBelongsToRequester(authentication, account)){
            throw new PermissionDeniedException(UNAUTHORIZED_ACCOUNT_ERROR);
        }
        Page<Transaction> transactions = transactionRepository.findBySourceOrDestinationAccount(accountId, pageable);
        transactions.getContent().forEach(transaction -> {
            if (transaction.getSourceAccount().getId().equals(accountId)) {
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

    private void updateAccountBalance(Account account, BigDecimal accountBalance) {
        account.setBalance(accountBalance);
        accountService.updateAccount(account);
    }

    private Transaction generateTransaction(Account sourceAccount, Account destinationAccount, TransferRequest transferRequest) {
        Transaction transaction = new Transaction();

        BigDecimal amountToTransferInSourceCurrency;
        if (doesCurrencyMatchesAccount(sourceAccount, transferRequest.getDestinationCurrency())) {
            amountToTransferInSourceCurrency = transferRequest.getAmount();
        } else {
            Map<CurrencyCode, BigDecimal> exchangeRates = currencyExchangeService.getExchangeRates(transferRequest.getDestinationCurrency());
            CurrencyCode sourceAccountCurrencyCode = sourceAccount.getCurrency();
            BigDecimal sourceCurrencyExchangeRate = exchangeRates.get(sourceAccountCurrencyCode);
            amountToTransferInSourceCurrency = transferRequest.getAmount().multiply(sourceCurrencyExchangeRate);
        }

        transaction.setDescription(transferRequest.getDescription());
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setSourceCurrencyCode(sourceAccount.getCurrency());
        transaction.setDestinationCurrencyCode(transferRequest.getDestinationCurrency());
        transaction.setSourceAmountDebited(amountToTransferInSourceCurrency);
        transaction.setDestinationAmountCredited(transferRequest.getAmount());
        transaction.setTransactionOperation(TransactionOperation.TRANSFER);
        return transaction;
    }

    private TransferResult executeAccountTransfer(Transaction transaction){
        Account sourceAccount = transaction.getSourceAccount();
        Account destinationAccount = transaction.getDestinationAccount();
        BigDecimal sourceAccountBalance = sourceAccount.getBalance();
        BigDecimal amountToTransferInSourceCurrency = transaction.getSourceAmountDebited();
        BigDecimal newSourceAccountBalance = sourceAccountBalance.subtract(amountToTransferInSourceCurrency);

        if(newSourceAccountBalance.compareTo(amountToTransferInSourceCurrency) < 0) {
            return new TransferResult(TransferStatus.FAILED, INSUFFICIENT_BALANCE_ERROR);
        }
        BigDecimal destinationAccountBalance = destinationAccount.getBalance();
        BigDecimal amountToTransferInDestinationCurrency = transaction.getDestinationAmountCredited();
        BigDecimal newDestinationAccountBalance = destinationAccountBalance.add(amountToTransferInDestinationCurrency);
        transaction.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));

        updateAccountBalance(sourceAccount, newSourceAccountBalance);
        updateAccountBalance(destinationAccount, newDestinationAccountBalance);

        transactionRepository.save(transaction);
        return new TransferResult(TransferStatus.SUCCESSFUL, TRANSFER_SUCCEEDED);
    }
}
