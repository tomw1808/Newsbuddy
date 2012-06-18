package com.tom.newsbuddy.classes;

import java.util.Date;
import java.util.HashMap;

public class Articles  implements Comparable<Articles>{
	private String subject = null;
	private int id;
	private String messageId = null;
	private String from = null;
	private String organization = null;
	private String referenceMap = null;
	private int articleNumber;
	private int groups_fkid;
	private Date date = null;
	private String content = null;
	private int level = 0;
	private HashMap<String, Articles> reply = null;
	private int sortOrder;
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public int getId() {
		return id;
	}

	public void setId(int i) {
		this.id = i;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public HashMap<String, Articles> getReply() {
		return reply;
	}

	public void setReply(HashMap<String, Articles> reply) {
		this.reply = reply;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getReferenceMap() {
		return referenceMap;
	}

	public void setReferenceMap(String referenceMap) {
		this.referenceMap = referenceMap;
	}

	public int getArticleNumber() {
		return articleNumber;
	}

	public void setArticleNumber(int articleId) {
		this.articleNumber = articleId;
	}

	public int getGroups_fkid() {
		return groups_fkid;
	}

	public void setGroups_fkid(int groups_fkid) {
		this.groups_fkid = groups_fkid;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	public String getFromShort() {
		if(getFrom().length() > 0 && getFrom().contains("<")) {
			return getFrom().substring(0,getFrom().indexOf('<')-1);
		} else {
			return getFrom();
		}
	}

	@Override
	public int compareTo(Articles another) {
		if(getSortOrder() < another.getSortOrder()) {
			return -1;
		}
		if(getSortOrder() > another.getSortOrder()) {
			return 1;
		}
		return 0;
	}



	
}
