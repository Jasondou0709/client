package com.example.microdemo.domain;

import java.util.List;

public class FirendMicroListDatas extends MyBaseBean{
	
	private String id;
	private String content;//内容
	private String uid;
	private String uname;
	private String sendtime;//发送时间
	private String usericon;//头像路径
	public String[] urls;//图片链接
	public synchronized String getUsericon() {
		return usericon;
	}
	public synchronized void setUsericon(String usericon) {
		this.usericon = usericon;
	}
	private List<FirstMicroListDatasFirendcomment> friendcomment;//评论
	private List<String> friendpraise;//点赞

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public String getSendtime() {
		return sendtime;
	}
	public void setSendtime(String sendtime) {
		this.sendtime = sendtime;
	}
	public List<FirstMicroListDatasFirendcomment> getFriendcomment() {
		return friendcomment;
	}
	public void setFriendcomment(
			List<FirstMicroListDatasFirendcomment> friendcomment) {
		this.friendcomment = friendcomment;
	}
	public List<String> getFriendpraise() {
		return friendpraise;
	}
	public void setFriendpraise(List<String> friendpraise) {
		this.friendpraise = friendpraise;
	}
}
