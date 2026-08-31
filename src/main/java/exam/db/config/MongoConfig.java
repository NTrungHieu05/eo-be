package exam.db.config;

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongoV3Driver;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import exam.ultis.PackageScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;

@Configuration
@PropertySource("classpath:${spring.profiles.active:dev}_mongo.properties")
public class MongoConfig extends AbstractMongoClientConfiguration {

	@Value("${mongo.prefix}")
	private String mongoPrefix;

	@Value("${mongo.database}")
	private String databaseName;

	@Value("${mongo.host}")
	private String host;

	@Value("${mongo.port}")
	private String port;

	@Value("${mongo.username}")
	private String username;

	@Value("${mongo.password}")
	private String password;

	@Override
	public MongoClient mongoClient() {
		ConnectionString connectionString = new ConnectionString(getConnectionURI());
		MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.build();
		return MongoClients.create(mongoClientSettings);
	}

	private String getConnectionURI() {
		StringBuilder sb = new StringBuilder(mongoPrefix + "://");
		if (StringUtils.hasText(username)) {
			sb.append(username);
			if (StringUtils.hasText(password)) {
				sb.append(":").append(password);
			}
			sb.append("@");
		}
		sb.append(host);
		if (StringUtils.hasText(port)) {
			sb.append(":").append(port);
		}
		return sb.toString();
	}

	@Override
	protected String getDatabaseName() {
		return databaseName;
	}

	@Bean
	public MongockSpring5.MongockInitializingBeanRunner mongockApplicationRunner(ApplicationContext springContext,
			MongoTemplate mongoTemplate) {
		return MongockSpring5.builder()
				.setDriver(SpringDataMongoV3Driver.withDefaultLock(mongoTemplate))
				.addChangeLogsScanPackage(PackageScan.PACKAGE_CHANGELOG)
				.setSpringContext(springContext)
				.buildInitializingBeanRunner();
	}
}
