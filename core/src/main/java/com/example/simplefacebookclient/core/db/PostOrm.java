package com.example.simplefacebookclient.core.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

@DatabaseTable(tableName = "Post")
public class PostOrm implements Serializable {

	@DatabaseField(columnName = "id", /*id = true,*/ generatedId = true) // Отключил первичный ключ
	private /*String*/ int mId;  // Изначально было String

	@DatabaseField(columnName = "message")
	private String mMessage;

	@DatabaseField(columnName = "createdTime", dataType = DataType.DATE_LONG)
	private Date mCreatedTime;

	public int getId() {
		return mId;
	}

	public void setId(final int id) {
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
