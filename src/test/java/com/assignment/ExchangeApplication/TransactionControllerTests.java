package com.assignment.ExchangeApplication;

import com.assignment.ExchangeApplication.controller.TransactionController;
import com.assignment.ExchangeApplication.exceptions.NegativeAmountException;
import com.assignment.ExchangeApplication.exceptions.PermissionDeniedException;
import com.assignment.ExchangeApplication.model.ErrorResponse;
import com.assignment.ExchangeApplication.model.dto.AccountResponseDto;
import com.assignment.ExchangeApplication.model.dto.TransactionRequest;
import com.assignment.ExchangeApplication.service.interfaces.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.INSUFFICIENT_BALANCE_ERROR;
import static com.assignment.ExchangeApplication.helpers.StatusMessages.UNAUTHORIZED_ACCOUNT_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTests {

    @Mock
    private TransactionService transactionService;

    @Mock
    private Authentication authenticationMock;

    @InjectMocks
    private TransactionController transactionController;

    private TransactionRequest getTestTransactionRequest() {
        TransactionRequest request = new TransactionRequest();
        request.setAccountIban("LV23HABASAXMQ749DHCA1");
        request.setAmount(BigDecimal.valueOf(100.00));
        return request;
    }

    @Test
    void depositToAccount_successfulDeposit_returnsOk() {
        TransactionRequest request = getTestTransactionRequest();
        AccountResponseDto responseDto = mock(AccountResponseDto.class);
        when(transactionService.depositAccount(authenticationMock, request)).thenReturn(responseDto);

        ResponseEntity<AccountResponseDto> response = transactionController.depositToAccount(authenticationMock, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void depositToAccount_permissionDenied_returnsForbidden() {
        TransactionRequest request = getTestTransactionRequest();
        when(transactionService.depositAccount(authenticationMock, request)).thenThrow(new PermissionDeniedException("Denied"));

        ResponseEntity<AccountResponseDto> response = transactionController.depositToAccount(authenticationMock, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void withdrawFromAccount_successfulWithdrawal_returnsOk() {
        TransactionRequest request = getTestTransactionRequest();
        AccountResponseDto responseDto = mock(AccountResponseDto.class);
        when(transactionService.withdrawAccount(authenticationMock, request)).thenReturn(responseDto);

        ResponseEntity<Object> response = transactionController.withdrawFromAccount(authenticationMock, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void withdrawFromAccount_permissionDenied_returnsForbidden() {
        TransactionRequest request = getTestTransactionRequest();
        when(transactionService.withdrawAccount(authenticationMock, request)).thenThrow(new PermissionDeniedException(UNAUTHORIZED_ACCOUNT_ERROR));

        ResponseEntity<Object> response = transactionController.withdrawFromAccount(authenticationMock, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
        assertEquals(UNAUTHORIZED_ACCOUNT_ERROR, error.getError());
    }

    @Test
    void withdrawFromAccount_negativeAmount_returnsBadRequest() {
        TransactionRequest request = getTestTransactionRequest();
        when(transactionService.withdrawAccount(authenticationMock, request)).thenThrow(new NegativeAmountException(INSUFFICIENT_BALANCE_ERROR));

        ResponseEntity<Object> response = transactionController.withdrawFromAccount(authenticationMock, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        assertEquals(INSUFFICIENT_BALANCE_ERROR, error.getError());
    }
}
