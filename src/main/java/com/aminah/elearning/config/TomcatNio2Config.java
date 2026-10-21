package com.aminah.elearning.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.tomcat.nio2.enabled", havingValue = "true")
public class TomcatNio2Config {

    static final String NIO2_PROTOCOL = "org.apache.coyote.http11.Http11Nio2Protocol";

    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatNio2ProtocolCustomizer() {
        return factory -> factory.setProtocol(NIO2_PROTOCOL);
    }
}
