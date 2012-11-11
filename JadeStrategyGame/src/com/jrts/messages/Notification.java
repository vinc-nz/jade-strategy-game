package com.jrts.messages;

import java.io.Serializable;

/**
 * Used to send notification like "there is an enemy here: 10,30" or
 * "there is no more food in our known world"
 * 
 */
public class Notification implements Serializable {
	private static final long serialVersionUID = -4103939700986977091L;
	
	public static final String ENEMY_SIGHTED = "enemy_sighted";
	public static final String NO_MORE_RESOURCE = "no_more_resource";
	public static final String RESOURCES_UPDATE = "resources_update";

	String subject;
	Serializable contentObject;

	public Notification(String messageSubject, Serializable contentObject) {
		this.subject = messageSubject;
		this.contentObject = contentObject;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Serializable getContentObject() {
		return contentObject;
	}

	public void setContentObject(Serializable contentObject) {
		this.contentObject = contentObject;
	}
}