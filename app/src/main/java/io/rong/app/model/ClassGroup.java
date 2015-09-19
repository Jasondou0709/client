package io.rong.app.model;

public class ClassGroup {
	private String id;
	private String className;
	private String portrait;
	private String introduce;
	private String number;
	private String maxNumber;
	
	public ClassGroup() {
		
	}
	
	public ClassGroup(String Id, String ClassName, String Portrait, String Introduce) {
		id = Id;
		className = ClassName;
		portrait = Portrait;
		introduce = Introduce;
	}
	
	public ClassGroup(String Id, String ClassName, String Portrait, String Introduce, String Number, String MaxNumber) {
		id = Id;
		className = ClassName;
		portrait = Portrait;
		introduce = Introduce;
		number = Number;
		maxNumber = MaxNumber;
	}
	
	public void setId(String Id) {
		id = Id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setClassName(String ClassName) {
		className = ClassName;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setPortrait(String Portrait) {
		portrait = Portrait;
	}
	
	public String getPortrait() {
		return portrait;
	}
	
	public void setIntroduce(String Introduce) {
		introduce = Introduce;
	}
	
	public String getIntroduce() {
		return introduce;
	}
	
	public void setNumber(String Number) {
		number = Number;
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setMaxNumber(String MaxNumber) {
		maxNumber = MaxNumber;
	}
	
	public String getMaxNumber() {
		return maxNumber;
	}
}
