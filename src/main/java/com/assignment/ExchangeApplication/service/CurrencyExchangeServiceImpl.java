package com.assignment.ExchangeApplication.service;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.model.CurrencyExchangeResponse;
import com.assignment.ExchangeApplication.service.interfaces.CurrencyExchangeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.RETRIEVE_EXCHANGE_RATE_ERROR;

@Service
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {
    private final RestClient restClient;
    public CurrencyExchangeServiceImpl(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://open.er-api.com").build();
    }

    @Override
    @Cacheable(value = "exchangeRatesCache", key = "#currencyCode")
    public Map<CurrencyCode, BigDecimal> getExchangeRates(CurrencyCode currencyCode) {
        try {
            CurrencyExchangeResponse response = this.restClient.get().uri("/v6/latest/{currencyCode}", currencyCode).retrieve().body(CurrencyExchangeResponse.class);
            assert response != null;
            return response.getRates();
        } catch (Exception e) {
            throw new RuntimeException(RETRIEVE_EXCHANGE_RATE_ERROR);
        }
    }
}
