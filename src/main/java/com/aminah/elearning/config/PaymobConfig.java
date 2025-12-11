package com.aminah.elearning.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "paymob")
public class PaymobConfig {
    private String apiKey;
    private String integrationId;
    private String iframeId;
    private String hmacSecret;
    private String baseUrl = "https://accept.paymob.com/api";
}
