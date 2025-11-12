package co.appointment.config;

import co.appointment.shared.util.OpenApiUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(final AppConfigProperties appConfigProperties) {
        return OpenApiUtils.openAPI(appConfigProperties.getOpenApi());
    }
}
