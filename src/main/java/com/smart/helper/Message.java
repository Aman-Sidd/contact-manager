package com.smart.helper;

public class Message {
	private String content;
	private String type;
	public Message(String content,String type) {
		super();
		this.type = type;
		this.content = content;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "Message [type=" + type + ", content=" + content + "]";
	}
	
	
}
