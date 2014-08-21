package com.denisigo.moments;

import java.util.Date;


/**
 * Represents a Moment.
 */
public class Moment{

	private long mId;
	private String mText;
	private String mAuthorName;
	private Date mAdded;	

	public Moment() {

	}

	public Moment(String text, String authorName) {
		this.setText(text);
		this.setAuthorName(authorName);
	}

	public Moment(long id, String text, String authorName, Date added) {
		this.setId(id);
		this.setText(text);
		this.setAuthorName(authorName);
		this.setAdded(added);
	}

	public String getText() {
		return mText;
	}

	public void setText(String text) {
		mText = text;
	}

	public String getAuthorName() {
		return mAuthorName;
	}

	public void setAuthorName(String authorName) {
		mAuthorName = authorName;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public Date getAdded() {
		return mAdded;
	}

	public void setAdded(Date added) {
		mAdded = added;
	}
}
