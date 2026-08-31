package exam.db.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "mongo")
@PropertySource("classpath:${spring.profiles.active:dev}_mongo.properties")
@Getter
@Setter
public class MongoSettings {
	private String host;
	private String port;
	private String database;
	private String username;
	private String password;
}
