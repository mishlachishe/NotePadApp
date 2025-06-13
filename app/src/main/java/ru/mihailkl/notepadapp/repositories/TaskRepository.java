package ru.mihailkl.notepadapp.repositories;

import static ru.mihailkl.notepadapp.database.DatabaseHelper.COLUMN_NOTE_CREATED_AT;
import static ru.mihailkl.notepadapp.database.DatabaseHelper.COLUMN_PINNED;
import static ru.mihailkl.notepadapp.database.DatabaseHelper.COLUMN_PINNED_DATE;
import static ru.mihailkl.notepadapp.database.DatabaseHelper.COLUMN_TASK_CREATED_AT;
import static ru.mihailkl.notepadapp.database.DatabaseHelper.COLUMN_TASK_ID;
import static ru.mihailkl.notepadapp.database.DatabaseHelper.TABLE_TASKS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.mihailkl.notepadapp.database.DatabaseHelper;
import ru.mihailkl.notepadapp.models.Task;

public class TaskRepository {
	private DatabaseHelper dbHelper;
	private SQLiteDatabase database;

	public TaskRepository(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Task addTask(Task task) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_TASK_TITLE, task.getTitle());
		values.put(DatabaseHelper.COLUMN_TASK_DESCRIPTION, task.getDescription());
		values.put(DatabaseHelper.COLUMN_TASK_COMPLETED, task.isCompleted() ? 1 : 0);
		if (task.getDueDate() != null) {
			values.put(DatabaseHelper.COLUMN_TASK_DUE_DATE, task.getDueDate().getTime());
		}

		long insertId = database.insert(TABLE_TASKS, null, values);

		Cursor cursor = database.query(TABLE_TASKS,
				null, COLUMN_TASK_ID + " = " + insertId, null,
				null, null, null
		);
		cursor.moveToFirst();
		Task newTask = cursorToTask(cursor);
		cursor.close();
		return newTask;
	}
	public void togglePin(Task task) {
		task.togglePin();
		ContentValues values = new ContentValues();
		values.put(COLUMN_PINNED, task.isPinned() ? 1 : 0);
		values.put(COLUMN_PINNED_DATE, task.isPinned() ? task.getPinnedDate().getTime() : null);

		database.update(TABLE_TASKS, values,
				COLUMN_TASK_ID + " = ?",
				new String[]{String.valueOf(task.getId())}
		);
	}
	public void updateTask(Task task) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_TASK_TITLE, task.getTitle());
		values.put(DatabaseHelper.COLUMN_TASK_DESCRIPTION, task.getDescription());
		values.put(DatabaseHelper.COLUMN_TASK_COMPLETED, task.isCompleted() ? 1 : 0);
		if (task.getDueDate() != null) {
			values.put(DatabaseHelper.COLUMN_TASK_DUE_DATE, task.getDueDate().getTime());
		}

		database.update(TABLE_TASKS, values,
				COLUMN_TASK_ID + " = ?",
				new String[]{String.valueOf(task.getId())}
		);
	}

	public void deleteTask(Task task) {
		database.delete(TABLE_TASKS,
				COLUMN_TASK_ID + " = ?",
				new String[]{String.valueOf(task.getId())}
		);
	}

	public List<Task> getAllTasks() {
		List<Task> tasks = new ArrayList<>();
		Cursor cursor = database.query(
				TABLE_TASKS,
				null, // Все колонки
				null, null, null, null,
				COLUMN_PINNED + " DESC, " +
						COLUMN_PINNED_DATE + " DESC, " +
						COLUMN_TASK_CREATED_AT + " DESC"
		);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Task task = cursorToTask(cursor);
			tasks.add(task);
			cursor.moveToNext();
		}
		cursor.close();
		return tasks;
	}

	private Task cursorToTask(Cursor cursor) {
		Task task = new Task();
		task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)));
		task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_TITLE)));
		task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DESCRIPTION)));
		task.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_COMPLETED)) == 1);
		// Добавляем обработку закрепления
		task.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PINNED)) == 1);
		if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PINNED_DATE))) {
			long pinnedDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PINNED_DATE));
			task.setPinnedDate(new Date(pinnedDate));
		}
		long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TASK_CREATED_AT));
		task.setCreatedAt(new Date(createdAt));

		if (!cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DUE_DATE))) {
			long dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DUE_DATE));
			task.setDueDate(new Date(dueDate));
		}

		return task;
	}
}