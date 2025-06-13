package ru.mihailkl.notepadapp.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task {
	private int id;
	private String title;
	private String description;
	private boolean completed;
	private Date createdAt;
	private Date dueDate;
	private boolean isPinned;
	private Date pinnedDate;

	public Task() {
		this.isPinned = false;
	}

	public Task(String title, String description, Date dueDate) {
		this.title = title;
		this.description = description;
		this.completed = false;
		this.createdAt = new Date();
		this.dueDate = dueDate;
		this.isPinned = false;
	}

	// Геттеры и сеттеры
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public void togglePin() {
		this.isPinned = !this.isPinned;
		this.pinnedDate = isPinned ? new Date() : null;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getFormattedCreatedDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
		return sdf.format(createdAt);
	}

	public String getFormattedDueDate() {
		if (dueDate == null) {
			return "Нет срока";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
		return sdf.format(dueDate);
	}
}