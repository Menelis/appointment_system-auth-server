package co.appointment;

import co.appointment.config.AppConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@SpringBootApplication
@EnableConfigurationProperties({AppConfigProperties.class})
@SuppressWarnings({"HideUtilityClassConstructor"})
@EnableJdbcAuditing
public class AuthServerApplication {
    static void main(final String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
