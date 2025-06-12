package com.assignment.ExchangeApplication.controller;

import com.assignment.ExchangeApplication.enums.TransferStatus;
import com.assignment.ExchangeApplication.exceptions.NegativeAmountException;
import com.assignment.ExchangeApplication.exceptions.PermissionDeniedException;
import com.assignment.ExchangeApplication.model.ErrorResponse;
import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.model.dto.TransactionRequest;
import com.assignment.ExchangeApplication.model.dto.TransferRequest;
import com.assignment.ExchangeApplication.model.dto.TransferResult;
import com.assignment.ExchangeApplication.service.interfaces.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponseDto> depositToAccount(Authentication authentication, @Valid @RequestBody TransactionRequest request) {
        try {
            AccountResponseDto updatedAccount = transactionService.depositAccount(authentication, request);
            return ResponseEntity.ok(updatedAccount);
        } catch (PermissionDeniedException e) {
            throw new AccessDeniedException(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdrawFromAccount(Authentication authentication, @Valid @RequestBody TransactionRequest request) {
        try {
            AccountResponseDto updatedAccount = transactionService.withdrawAccount(authentication, request);
            return ResponseEntity.ok(updatedAccount);
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(HttpStatus.FORBIDDEN, e.getMessage()));
        } catch (NegativeAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/transfer")
    public  ResponseEntity<TransferResult> transferFundsBetweenAccounts(Authentication authentication, @Valid @RequestBody TransferRequest transferRequest){
        TransferResult transferResult = transactionService.transferBetweenAccounts(authentication, transferRequest);
        if (transferResult.getTransferStatus().equals(TransferStatus.SUCCESSFUL)){
            return ResponseEntity.ok(transferResult);
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(transferResult);
        }
    }
}
