package co.appointment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.appointment.shared.config.SharedAppConfigProperties;

@Configuration
public class AppConfig {
    @Bean
    public SharedAppConfigProperties sharedAppConfigProperties(final AppConfigProperties appConfigProperties) {
        SharedAppConfigProperties sharedAppConfigProperties = new SharedAppConfigProperties();
        sharedAppConfigProperties.setEncryptionKey(appConfigProperties.getEncryptionKey());
        return sharedAppConfigProperties;
    }
}
