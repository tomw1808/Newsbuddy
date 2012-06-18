package com.tom.newsbuddy.classes;


public class Groups {
	private int id;
	private String name;
	private String high;
	private String low;
	private String permission;
	private int server_fkid;
	public int getId() {
		return id;
	}
	
	public Groups(int id, String name, String high, String low, String permission, int server_fkid) {
		this.id =id;
		this.name = name;
		this.high = high;
		this.low = low;
		this.permission = permission;
		this.server_fkid = server_fkid;
	}
	public Groups() {
	}

	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHigh() {
		return high;
	}
	public void setHigh(String high) {
		this.high = high;
	}
	public String getLow() {
		return low;
	}
	public void setLow(String low) {
		this.low = low;
	}
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}
	public int getServer_fkid() {
		return server_fkid;
	}
	public void setServer_fkid(int server_fkid) {
		this.server_fkid = server_fkid;
	}
	
	



	
}
