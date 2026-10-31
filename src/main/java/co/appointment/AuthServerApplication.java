package co.appointment;

import co.appointment.config.AppConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ AppConfigProperties.class })
@SuppressWarnings({ "HideUtilityClassConstructor" })
public class AuthServerApplication {
	static void main(final String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}
}
