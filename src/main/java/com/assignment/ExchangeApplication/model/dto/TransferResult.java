package com.assignment.ExchangeApplication.model.dto;

import com.assignment.ExchangeApplication.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferResult {

    private TransferStatus transferStatus;

    private String message;
}
