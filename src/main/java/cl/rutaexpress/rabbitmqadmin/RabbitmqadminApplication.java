package cl.rutaexpress.rabbitmqadmin;

import cl.rutaexpress.rabbitmqadmin.config.rabbitAdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(rabbitAdminProperties.class)
public class RabbitmqadminApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqadminApplication.class, args);
	}

}
