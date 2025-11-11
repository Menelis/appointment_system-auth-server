package co.appointment.config;

import co.appointment.shared.model.CorsSettings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "app")
@Data
public class AppConfigProperties {
    private String[] whiteList;
    private ClientSettings registeredClient;
    private String encryptionKey;
    private CorsSettings cors;


    @Data
    public static class ClientSettings {
        private String clientId;
        private String clientSecret;
        private String clientUri;
    }
}
