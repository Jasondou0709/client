package com.example.microdemo.domain;

import java.util.List;

public class FirendsMicro {

	private FirendMicroList friendPager;
	private String error;
	private String tip;
	
	public FirendMicroList getFriendPager() {
		return friendPager;
	}
	public void setFriendPager(FirendMicroList friendPager) {
		this.friendPager = friendPager;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getTip() {
		return tip;
	}
	public void setTip(String tip) {
		this.tip = tip;
	}
	
}
