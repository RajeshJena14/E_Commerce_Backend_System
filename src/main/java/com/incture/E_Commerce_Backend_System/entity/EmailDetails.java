package com.incture.E_Commerce_Backend_System.entity;

public class EmailDetails {
	private String recipient;
	private String msgBody;
	private String subject;

	public EmailDetails() {
	}

	public EmailDetails(String recipient, String msgBody, String subject) {
		this.recipient = recipient;
		this.msgBody = msgBody;
		this.subject = subject;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public String getSubject() {
		return subject;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
