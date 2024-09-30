package br.com.age.converter.prontuarios.pdf.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database {

    private static final HikariDataSource dataSource;
    
    static {
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setMaximumPoolSize(1);
        setConfigSecret(config);
        
        dataSource = new HikariDataSource(config);
    }

	private static void setConfigSecret(HikariConfig config) {
    	try {
    		AwsSecret awsSecret = new ObjectMapper().readValue(getSecret(), AwsSecret.class);
    		
    		config.setJdbcUrl("jdbc:mariadb://" + awsSecret.getHost() + ":" + awsSecret.getPort() + "/"+awsSecret.getDbname()+"?characterEncoding=latin1&allowPublicKeyRetrieval=true");
	        config.setUsername(awsSecret.getUsername());
	        config.setPassword(awsSecret.getPassword());
	        
    	} catch (Exception e) {
    		e.printStackTrace();
		}
    }
    
    private static String getSecret() {
    	AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
    									.withRegion(System.getenv("REGION"))
    									.build();
        
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                        .withSecretId(System.getenv("DB_SECRET_SOC"));
        
        GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        
    	if (getSecretValueResult.getSecretString() != null) {
    		return getSecretValueResult.getSecretString();
    	}
    	
		return new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
    }

    public Connection connection() throws SQLException {
        return dataSource.getConnection();
    }
	
}
