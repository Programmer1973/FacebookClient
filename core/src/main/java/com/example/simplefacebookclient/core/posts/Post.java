package com.example.simplefacebookclient.core.posts;

import java.util.Date;

public class Post {

	private String mId;
	private String mMessage;
	private Date mCreatedTime;

	public Post() {
		// do nothing
	}

	public Post(final String message) {
		mMessage = message;
	}

	public String getId() {
		return mId;
	}

	public void setId(final String id) {
		mId = id;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(final String message) {
		mMessage = message;
	}

	public Date getCreatedTime() {
		return mCreatedTime;
	}

	public void setCreatedTime(final Date createdTime) {
		mCreatedTime = createdTime;
	}
}
