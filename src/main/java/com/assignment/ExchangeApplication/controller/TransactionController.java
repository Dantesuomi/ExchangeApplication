package com.assignment.ExchangeApplication.controller;

import com.assignment.ExchangeApplication.enums.TransferStatus;
import com.assignment.ExchangeApplication.exceptions.CurrencyExchangeException;
import com.assignment.ExchangeApplication.exceptions.FailedAccountUpdateException;
import com.assignment.ExchangeApplication.exceptions.NegativeAmountException;
import com.assignment.ExchangeApplication.exceptions.PermissionDeniedException;
import com.assignment.ExchangeApplication.model.ErrorResponse;
import com.assignment.ExchangeApplication.model.Transaction;
import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.model.dto.TransactionRequest;
import com.assignment.ExchangeApplication.model.dto.TransferRequest;
import com.assignment.ExchangeApplication.model.dto.TransferResult;
import com.assignment.ExchangeApplication.service.interfaces.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.RETRIEVE_EXCHANGE_RATE_ERROR;
import static com.assignment.ExchangeApplication.helpers.StatusMessages.TRANSFER_ERROR;

@RestController
@RequestMapping("api/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponseDto> depositToAccount(Authentication authentication, @Valid @RequestBody TransactionRequest request) {
        try {
            AccountResponseDto updatedAccount = transactionService.depositAccount(authentication, request);
            log.info("Deposit successful to account {}", updatedAccount.getIban());

            return ResponseEntity.status(HttpStatus.OK).body(updatedAccount);
        } catch (PermissionDeniedException e) {
            log.warn("Permission denied: client tried to deposit funds to an unauthorized account {}", request.getAccountIban());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdrawFromAccount(Authentication authentication, @Valid @RequestBody TransactionRequest request) {
        try {
            AccountResponseDto updatedAccount = transactionService.withdrawAccount(authentication, request);
            log.info("Withdrawal successful from account {} ", updatedAccount.getIban());
            return ResponseEntity.ok(updatedAccount);
        } catch (PermissionDeniedException e) {
            log.warn("Permission denied: client tried to withdraw funds to an unauthorized account {}", request.getAccountIban());

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(HttpStatus.FORBIDDEN, e.getMessage()));
        } catch (NegativeAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/transfer")
    public  ResponseEntity<TransferResult> transferFundsBetweenAccounts(Authentication authentication, @Valid @RequestBody TransferRequest transferRequest){
        try {
            TransferResult transferResult = transactionService.transferBetweenAccounts(authentication, transferRequest);
            if (transferResult.getTransferStatus().equals(TransferStatus.SUCCESSFUL)){
                log.info("Transfer successful from {} to {}", transferRequest.getSourceAccountNumber(), transferRequest.getDestinationAccountNumber());
                return ResponseEntity.status(HttpStatus.OK).body(transferResult);
            }
            else {
                log.info("Transfer failed from {} to {}", transferRequest.getSourceAccountNumber(), transferRequest.getDestinationAccountNumber());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(transferResult);
            }
        }
        catch (CurrencyExchangeException e) {
            log.error("Currency exchange rate retrieval failed during transfer from {} to {}", transferRequest.getSourceAccountNumber(), transferRequest.getDestinationAccountNumber());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new TransferResult(TransferStatus.FAILED, RETRIEVE_EXCHANGE_RATE_ERROR));
        }
        catch (FailedAccountUpdateException e) {
            log.error("Account update failure during transfer");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TransferResult(TransferStatus.FAILED, TRANSFER_ERROR));
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Page<Transaction>> getTransactionsById(Authentication authentication, @PathVariable UUID accountId, @RequestParam(defaultValue = "10") int limit,
                                                                 @RequestParam(defaultValue = "0") int offset) {

        log.info("Getting transactions for account {} ", accountId);

        Pageable pageable = PageRequest.of(offset, limit, Sort.by("timestamp").descending());
        Page<Transaction> page = transactionService.getTransactionsForAccount(authentication, accountId, pageable);

        log.info("Got transactions for account {}", accountId);
        return ResponseEntity.status(HttpStatus.OK).body(page);
    }
}
