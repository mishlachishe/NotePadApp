package ru.mihailkl.notepadapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "notes_tasks.db";
	private static final int DATABASE_VERSION = 2;

	// Таблица заметок
	public static final String TABLE_NOTES = "notes";
	public static final String COLUMN_NOTE_ID = "id";
	public static final String COLUMN_NOTE_TITLE = "title";
	public static final String COLUMN_NOTE_CONTENT = "content";
	public static final String COLUMN_NOTE_CREATED_AT = "created_at";
	public static final String COLUMN_PINNED = "pinned";
	public static final String COLUMN_PINNED_DATE = "pinned_date";

	// Таблица задач
	public static final String TABLE_TASKS = "tasks";
	public static final String COLUMN_TASK_ID = "id";
	public static final String COLUMN_TASK_TITLE = "title";
	public static final String COLUMN_TASK_DESCRIPTION = "description";
	public static final String COLUMN_TASK_COMPLETED = "completed";
	public static final String COLUMN_TASK_CREATED_AT = "created_at";
	public static final String COLUMN_TASK_DUE_DATE = "due_date";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Создание таблицы заметок
		String createNotesTable = "CREATE TABLE " + TABLE_NOTES + "("
				+ COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_NOTE_TITLE + " TEXT NOT NULL, "
				+ COLUMN_NOTE_CONTENT + " TEXT, "
				+ COLUMN_NOTE_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
				+ COLUMN_PINNED + " INTEGER DEFAULT 0, "
				+ COLUMN_PINNED_DATE + " DATETIME"
				+ ")";
		db.execSQL(createNotesTable);

		// Создание таблицы задач
		String createTasksTable = "CREATE TABLE " + TABLE_TASKS + "("
				+ COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_TASK_TITLE + " TEXT NOT NULL, "
				+ COLUMN_TASK_DESCRIPTION + " TEXT, "
				+ COLUMN_TASK_COMPLETED + " INTEGER DEFAULT 0, "
				+ COLUMN_TASK_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
				+ COLUMN_TASK_DUE_DATE + " DATETIME, "
				+ COLUMN_PINNED + " INTEGER DEFAULT 0, "
				+ COLUMN_PINNED_DATE + " DATETIME"
				+ ")";
		db.execSQL(createTasksTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
		onCreate(db);
	}
}