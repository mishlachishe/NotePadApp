package ru.mihailkl.notepadapp.repositories;

import static ru.mihailkl.notepadapp.database.DatabaseHelper.COLUMN_NOTE_CREATED_AT;
import static ru.mihailkl.notepadapp.database.DatabaseHelper.COLUMN_NOTE_ID;
import static ru.mihailkl.notepadapp.database.DatabaseHelper.COLUMN_PINNED;
import static ru.mihailkl.notepadapp.database.DatabaseHelper.COLUMN_PINNED_DATE;
import static ru.mihailkl.notepadapp.database.DatabaseHelper.TABLE_NOTES;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.mihailkl.notepadapp.database.DatabaseHelper;
import ru.mihailkl.notepadapp.models.Note;

public class NoteRepository {
	private DatabaseHelper dbHelper;
	private SQLiteDatabase database;

	public NoteRepository(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public void open() {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	public Note addNote(Note note) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_NOTE_TITLE, note.getTitle());
		values.put(DatabaseHelper.COLUMN_NOTE_CONTENT, note.getContent());

		values.put(COLUMN_NOTE_CREATED_AT, note.getCreatedAt().getTime());

		long insertId = database.insert(TABLE_NOTES, null, values);

		Cursor cursor = database.query(TABLE_NOTES,
				null, COLUMN_NOTE_ID + " = " + insertId, null,
				null, null, null
		);
		cursor.moveToFirst();
		Note newNote = cursorToNote(cursor);
		cursor.close();
		return newNote;
	}

	public void updateNote(Note note) {
		ContentValues values = new ContentValues();
		values.put(DatabaseHelper.COLUMN_NOTE_TITLE, note.getTitle());
		values.put(DatabaseHelper.COLUMN_NOTE_CONTENT, note.getContent());

		database.update(TABLE_NOTES, values,
				COLUMN_NOTE_ID + " = ?",
				new String[]{String.valueOf(note.getId())}
		);
	}

	public void deleteNote(Note note) {
		database.delete(TABLE_NOTES,
				COLUMN_NOTE_ID + " = ?",
				new String[]{String.valueOf(note.getId())}
		);
	}

	public List<Note> getAllNotes() {
		List<Note> notes = new ArrayList<>();
		Cursor cursor = database.query(
				TABLE_NOTES,
				null, // Все колонки
				null, null, null, null,
				COLUMN_PINNED + " DESC, " +
						COLUMN_PINNED_DATE + " DESC, " +
						COLUMN_NOTE_CREATED_AT + " DESC"
		);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Note note = cursorToNote(cursor);
			notes.add(note);
			cursor.moveToNext();
		}
		cursor.close();
		return notes;
	}
	public void togglePin(Note note) {
		note.togglePin();
		ContentValues values = new ContentValues();
		values.put(COLUMN_PINNED, note.isPinned() ? 1 : 0);
		values.put(COLUMN_PINNED_DATE, note.isPinned() ? note.getPinnedDate().getTime() : null);

		database.update(TABLE_NOTES, values,
				COLUMN_NOTE_ID + " = ?",
				new String[]{String.valueOf(note.getId())}
		);
	}
	private Note cursorToNote(Cursor cursor) {
		Note note = new Note();
		note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)));
		note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_TITLE)));
		note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE_CONTENT)));
		note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PINNED)) == 1);
		if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PINNED_DATE))) {
			long pinnedDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PINNED_DATE));
			note.setPinnedDate(new Date(pinnedDate));
		}
		long createdAtMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NOTE_CREATED_AT));
		note.setCreatedAt(new Date(createdAtMillis));

		return note;
	}
}