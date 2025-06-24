package com.assignment.ExchangeApplication.service;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.exceptions.CurrencyExchangeException;
import com.assignment.ExchangeApplication.model.CurrencyExchangeResponse;
import com.assignment.ExchangeApplication.service.interfaces.CurrencyExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.EXCHANGE_UNAVAILABLE_ERROR;
import static com.assignment.ExchangeApplication.helpers.StatusMessages.RETRIEVE_EXCHANGE_RATE_ERROR;

@Service
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {
    private final RestTemplate restTemplate;
    private final Logger log = LoggerFactory.getLogger(CurrencyExchangeServiceImpl.class);

    public CurrencyExchangeServiceImpl(RestTemplateBuilder restTemplateBuilder, @Value("${exchange.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.rootUri(baseUrl).build();
    }

    @Override
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 5000))
    @Cacheable(value = "exchangeRatesCache", key = "#currencyCode")
    public Map<CurrencyCode, BigDecimal> getExchangeRates(CurrencyCode currencyCode) {
        ResponseEntity<CurrencyExchangeResponse> response;
        try {
            log.info("Retrieving exchange rates for {} currency", currencyCode);
            response = restTemplate.getForEntity("/v6/latest/{currencyCode}", CurrencyExchangeResponse.class, currencyCode);
            // Api on errors returns 200 status with a body but without rates
            Map<CurrencyCode, BigDecimal> rates = response.getBody().getRates();
            if (rates == null) {
                log.error("Failed to retrieve exchange rates: rates are null");
                throw new CurrencyExchangeException(EXCHANGE_UNAVAILABLE_ERROR);
            }
            return rates;
        } catch (Exception e) {
            log.error("Failed to retrieve exchange rates: {}", e.getMessage());
            throw new CurrencyExchangeException(RETRIEVE_EXCHANGE_RATE_ERROR);
        }
    }
}
