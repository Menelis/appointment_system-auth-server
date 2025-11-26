package co.appointment.config;

import co.appointment.shared.model.CorsSettings;
import co.appointment.shared.model.OpenApiSettings;
import lombok.Data;
import org.hibernate.sql.ast.tree.expression.Collation;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

@ConfigurationProperties(prefix = "app")
@Data
public class AppConfigProperties {
    private OpenApiSettings openApi;
    private String[] whiteList;
    private String[] customExposedEndpoints;
    private List<RegisteredClientSetting> registeredClients = new ArrayList<>();
    private String encryptionKey;
    private CorsSettings cors;
    private KafkaSettings kafka;
    private VerificationTokenSettings verificationToken;
    private String clientUrl;
    private EmailTemplateSetting emailTemplate;


    @Data
    public static class KafkaSettings {
        private String notificationTopic;
    }
    @Data
    public static class VerificationTokenSettings {
        private Integer expirationMs;
    }

    @Data
    public static class ClientSetting {
        private boolean requireProofOfKey = false;
    }
    @Data
    public static class RegisteredClientSetting {
        private String clientId;
        private String clientSecret;
        private Set<String> scopes = new HashSet<>();
        private Collection<String> authorizationGrantTypes = new HashSet<>();
        private Set<String> clientAuthenticationMethods = new HashSet<>();
        private Set<String> redirectUris = new HashSet<>();
        private Set<String> postLogoutRedirectUris = new HashSet<>();
        private ClientSetting clientSettings = new ClientSetting();
        private TokenSetting tokenSetting = new TokenSetting();
    }
    @Data
    public static class TokenSetting {
        private long accessTokenTimeToLive = 120;
        private long refreshTokenTimeToLive = 120;
    }
    @Data
    public static class EmailTemplateSetting {
        private String verifyEmailTemplate;
        private String resetPasswordEmailTemplate;
    }
}
