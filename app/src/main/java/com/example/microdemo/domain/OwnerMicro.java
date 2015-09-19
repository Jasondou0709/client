package com.example.microdemo.domain;

public class OwnerMicro {

	private String ownerid;
	private String ownername;
	private String owneravatar;
	private String ownergroupid;
	private String[] ownerselect; 
	
	public String getOwnergroupid() {
		return ownergroupid;
	}
	public void setOwnergroupid(String ownergroupid) {
		this.ownergroupid = ownergroupid;
	}

	public String getOwnerid() {
		return ownerid;
	}
	public void setOwnerid(String ownerid) {
		this.ownerid = ownerid;
	}	

	public String getOwnername() {
		return ownername;
	}
	public void setOwnername(String ownername) {
		this.ownername = ownername;
	}	
	
	public String getOwneravatar() {
		return owneravatar;
	}
	public void setOwneravatar(String owneravatar) {
		this.owneravatar = owneravatar;
	}
	
	public String[] getOwnerselect() {
		return ownerselect;
	}
	public void setOwnerselect(String[] ownerselect) {
		this.ownerselect = ownerselect;
	}	
}
