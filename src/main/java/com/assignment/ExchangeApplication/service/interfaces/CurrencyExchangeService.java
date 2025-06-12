package com.assignment.ExchangeApplication.service.interfaces;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public interface CurrencyExchangeService {
    Map<CurrencyCode, BigDecimal> getExchangeRates(CurrencyCode currencyCode);
}
