package ch.dboeckli.spring.datarest.config;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenTelemetryAppenderInitializerTest {

    @Test
    void skipsInstallWhenNoOpenTelemetryBeanIsAvailable() {
        ObjectProvider<OpenTelemetry> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        OpenTelemetryAppenderInitializer initializer = new OpenTelemetryAppenderInitializer(provider);

        assertThatCode(initializer::afterPropertiesSet).doesNotThrowAnyException();
    }

}
