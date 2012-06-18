package com.tom.newsbuddy.classes;

public class Server {
	private String address = null;
	private String port = null;
	private String username = null;
	private String password = null;
	private int ssl = 0;
	private int id = 0;
	
	public Server() {
		
	}
	
	public Server(int id, String address, String port, String username, String password, String ssl) {
		this.setAddress(address);
		this.setPort(port);
		this.setId(id);
		this.setUsername(username);
		this.setPassword(password);
		this.setSsl(Integer.parseInt(ssl));
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

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

	public int getSsl() {
		return ssl;
	}

	public void setSsl(int ssl) {
		this.ssl = ssl;
	}
	



	
}
