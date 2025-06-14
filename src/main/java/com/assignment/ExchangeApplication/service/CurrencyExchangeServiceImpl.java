package com.assignment.ExchangeApplication.service;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.assignment.ExchangeApplication.exceptions.CurrencyExchangeException;
import com.assignment.ExchangeApplication.model.CurrencyExchangeResponse;
import com.assignment.ExchangeApplication.service.interfaces.CurrencyExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

import static com.assignment.ExchangeApplication.helpers.StatusMessages.EXCHANGE_UNAVAILABLE_ERROR;
import static com.assignment.ExchangeApplication.helpers.StatusMessages.RETRIEVE_EXCHANGE_RATE_ERROR;

@Service
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {
    private final RestClient restClient;
    private final Logger log = LoggerFactory.getLogger(CurrencyExchangeServiceImpl.class);
    public CurrencyExchangeServiceImpl(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://open.er-api.com").build();
    }

    @Override
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 5000))
    @Cacheable(value = "exchangeRatesCache", key = "#currencyCode")
    public Map<CurrencyCode, BigDecimal> getExchangeRates(CurrencyCode currencyCode) {
        try {
            log.info("Retrieving exchange rates for {} currency", currencyCode);
            CurrencyExchangeResponse response = this.restClient.get().uri("/v6/latest/{currencyCode}", currencyCode).retrieve().body(CurrencyExchangeResponse.class);
            assert response != null;
            Map<CurrencyCode, BigDecimal> rates = response.getRates();
            // Api on errors returns 200 status with a body but without rates
            if (rates==null) {
                throw new CurrencyExchangeException(EXCHANGE_UNAVAILABLE_ERROR);
            }
            return rates;
        } catch (Exception e) {
            log.error("Failed to retrieve exchange rates: {}",e.getMessage());
            throw new CurrencyExchangeException(RETRIEVE_EXCHANGE_RATE_ERROR);
        }
    }
}
