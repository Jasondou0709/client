package io.rong.app.model;

public class User1 {
	private String id;
	private String name;
	private String portrait;
	private String email;
	private String mobile;
	
	public User1() {
		
	}
	
	public User1(String Id, String Name, String Portrait) {
		id = Id;
		name = Name;
		portrait = Portrait;
	}
	
	public User1(String Id, String Name, String Portrait, String Email, String PhoneNumber) {
		id = Id;
		name = Name;
		portrait = Portrait;
		email = Email;
		mobile = PhoneNumber;
	}
	
	public void setId(String Id) {
		id = Id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setName(String Name) {
		name = Name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setPortrait(String Portrait) {
		portrait = Portrait;
	}
	
	public String getPortrait() {
		return portrait;
	}
	
	public void setEmail(String Email) {
		email = Email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setMobile(String Number) {
		mobile = Number;
	}
	
	public String getMobile() {
		return mobile;
	}
	
}

