package br.com.age.converter.prontuarios.pdf.db;

public class AwsSecret {
	
	private String username;
	private String password;
	private String host;
	private String engine;
	private String dbname;
	private String dbClusterIdentifier;
	private int port;
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getEngine() {
		return engine;
	}
	
	public void setEngine(String engine) {
		this.engine = engine;
	}
	
	public String getDbname() {
		return dbname;
	}
	
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getDbClusterIdentifier() {
		return dbClusterIdentifier;
	}

	public void setDbClusterIdentifier(String dbClusterIdentifier) {
		this.dbClusterIdentifier = dbClusterIdentifier;
	}

	
}
