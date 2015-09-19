package com.example.microdemo.domain;

public class FirstMicroListDatasFirendcomment extends MyBaseBean{

	private String replyId;//回复人id
	private String replyName;//回复人姓名
	private String isReplyId;//被回复人id
	private String isReplyName;//被回复人姓名
	private String comment;

	public String getReplyId() {
		return replyId;
	}
	public void setReplyId(String replyId) {
		this.replyId = replyId;
	}
	public String getReplyName() {
		return replyName;
	}
	public void setReplyName(String replyName) {
		this.replyName = replyName;
	}
	public String getIsReplyId() {
		return isReplyId;
	}
	public void setIsReplyId(String isReplyId) {
		this.isReplyId = isReplyId;
	}
	public String getIsReplyName() {
		return isReplyName;
	}
	public void setIsReplyName(String isReplyName) {
		this.isReplyName = isReplyName;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
