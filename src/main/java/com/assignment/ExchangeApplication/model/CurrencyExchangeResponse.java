package com.assignment.ExchangeApplication.model;

import com.assignment.ExchangeApplication.enums.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CurrencyExchangeResponse {
    private String result;
    private String provider;
    private String documentation;
    @JsonProperty("terms_of_use")
    private String termsOfUse;
    @JsonProperty("time_last_update_unix")
    private int timeLastUpdateUnix;
    @JsonProperty("time_last_update_utc")
    private String timeLastUpdateUtc;
    @JsonProperty("time_next_update_unix")
    private int timeNextUpdateUnix;
    @JsonProperty("time_next_update_utc")
    private String timeNextUpdateUtc;
    @JsonProperty("time_eol_unix")
    private int timeEolUnix;
    @JsonProperty("base_code")
    private String baseCode;
    private Map<CurrencyCode, BigDecimal> rates;

}
