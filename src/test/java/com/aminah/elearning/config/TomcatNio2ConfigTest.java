package com.aminah.elearning.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TomcatNio2ConfigTest {

    @Test
    void selectsNio2WhenCustomizerIsEnabled() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

        new TomcatNio2Config().tomcatNio2ProtocolCustomizer().customize(factory);

        assertThat(ReflectionTestUtils.getField(factory, "protocol"))
                .isEqualTo(TomcatNio2Config.NIO2_PROTOCOL);
    }
}
