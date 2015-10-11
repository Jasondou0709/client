package io.rong.app.model;

public class RequestInfo {
	private String userid;
	private String username;
	private String portrait;
	private int status;
	private String classId;
	private String className;
	
	public RequestInfo() {
		
	}
	
	public RequestInfo(String Id, String Name, String Portrait, int Status) {
		userid = Id;
		username = Name;
		portrait = Portrait;
		status = Status;
	}
	
	public RequestInfo(String Id, String Name, String Portrait, int Status, String ClassID, String ClassName) {
		userid = Id;
		username = Name;
		portrait = Portrait;
		status = Status;
		classId = ClassID;
		className = ClassName;
	}
	
	public void setId(String Id) {
		userid = Id;
	}
	
	public String getId() {
		return userid;
	}
	
	public void setName(String Name) {
		username = Name;
	}
	
	public String getName() {
		return username;
	}
	
	public void setPortrait(String Portrait) {
		portrait = Portrait;
	}
	
	public String getPortrait() {
		return portrait;
	}
	
	public void setStatus(int Status) {
		status = Status;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setClassId(String ClassId) {
		classId = ClassId;
	}
	
	public String getClassId() {
		return classId;
	}
	
	public void setClassName(String ClassName) {
		className = ClassName;
	}
	
	public String getClassName() {
		return className;
	}
	
}

