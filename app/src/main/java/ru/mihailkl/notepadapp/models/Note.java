package ru.mihailkl.notepadapp.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Note {
	private int id;
	private String title;
	private String content;
	private Date createdAt;
	private boolean isPinned;
	private Date pinnedDate;

	public Note() {
		this.createdAt = new Date();
		this.isPinned = false;
	}
	public void togglePin() {
		this.isPinned = !this.isPinned;
		this.pinnedDate = isPinned ? new Date() : null;
	}
	public Note(String title, String content) {
		this.title = title;
		this.content = content;
		this.createdAt = new Date();
		this.isPinned = false;
	}

	// Геттеры и сеттеры
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPinned(boolean pinned) {
		this.isPinned = pinned;
		if (pinned) {
			this.pinnedDate = new Date();
		} else {
			this.pinnedDate = null;
		}
	}
	public boolean isPinned() {
		return this.isPinned;
	}

	public Date getPinnedDate() {
		return pinnedDate;
	}

	public void setPinnedDate(Date date) {
		this.pinnedDate = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getFormattedDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
		return sdf.format(createdAt);
	}
}