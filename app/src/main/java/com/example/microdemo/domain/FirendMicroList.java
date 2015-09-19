package com.example.microdemo.domain;

import java.util.List;

public class FirendMicroList extends MyBaseBean{
	private String offset;
	private String showNum;
	private String total;
	private List<FirendMicroListDatas> datas;
	
	public String getOffset() {
		return offset;
	}
	public void setOffset(String offset) {
		this.offset = offset;
	}
	public String getShowNum() {
		return showNum;
	}
	public void setShowNum(String showNum) {
		this.showNum = showNum;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public List<FirendMicroListDatas> getDatas() {
		return datas;
	}
	public void setDatas(List<FirendMicroListDatas> datas) {
		this.datas = datas;
	}
}
