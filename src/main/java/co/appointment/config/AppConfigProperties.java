package co.appointment.config;

import co.appointment.shared.model.CorsSettings;
import co.appointment.shared.model.OpenApiSettings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "app")
@Data
public class AppConfigProperties {
    private OpenApiSettings openApi;
    private String[] whiteList;
    private String[] customExposedEndpoints;
    private ClientSettings registeredClient;
    private String encryptionKey;
    private CorsSettings cors;
    private KafkaSettings kafka;
    private VerificationTokenSettings verificationToken;
    private String clientUrl;


    @Data
    public static class KafkaSettings {
        private String notificationTopic;
    }
    @Data
    public static class VerificationTokenSettings {
        private Integer expirationMs;
    }

    @Data
    public static class ClientSettings {
        private String clientId;
        private String clientSecret;
        private String clientUri;
    }
}
