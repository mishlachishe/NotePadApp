package ru.mihailkl.notepadapp.utils;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;

import ru.mihailkl.notepadapp.models.Note;
import ru.mihailkl.notepadapp.models.Task;
import ru.mihailkl.notepadapp.repositories.NoteRepository;
import ru.mihailkl.notepadapp.repositories.TaskRepository;

public class DataImporter {
	private Context context;

	public DataImporter(Context context) {
		this.context = context;
	}

	public void importData(InputStream inputStream) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}
			String jsonData = stringBuilder.toString();
			parseAndImport(jsonData);
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(context, "Ошибка чтения файла", Toast.LENGTH_SHORT).show();
		}
	}

	private void parseAndImport(String jsonData) {
		try {
			JSONObject jsonObject = new JSONObject(jsonData);

			// Импорт заметок
			if (jsonObject.has("notes")) {
				JSONArray notesArray = jsonObject.getJSONArray("notes");
				NoteRepository noteRepo = new NoteRepository(context);
				noteRepo.open();

				for (int i = 0; i < notesArray.length(); i++) {
					JSONObject noteObj = notesArray.getJSONObject(i);
					Note note = new Note();
					note.setTitle(noteObj.getString("title"));
					note.setContent(noteObj.getString("content"));
					note.setCreatedAt(new Date(noteObj.getLong("created_at")));
					note.setPinned(noteObj.getBoolean("pinned"));
					if (noteObj.has("pinned_date")) {
						note.setPinnedDate(new Date(noteObj.getLong("pinned_date")));
					}
					noteRepo.addNote(note);
				}
				noteRepo.close();
			}

			// Импорт задач
			if (jsonObject.has("tasks")) {
				JSONArray tasksArray = jsonObject.getJSONArray("tasks");
				TaskRepository taskRepo = new TaskRepository(context);
				taskRepo.open();

				for (int i = 0; i < tasksArray.length(); i++) {
					JSONObject taskObj = tasksArray.getJSONObject(i);
					Task task = new Task();
					task.setTitle(taskObj.getString("title"));
					task.setDescription(taskObj.getString("description"));
					task.setCompleted(taskObj.getBoolean("completed"));
					task.setCreatedAt(new Date(taskObj.getLong("created_at")));
					if (taskObj.has("due_date")) {
						task.setDueDate(new Date(taskObj.getLong("due_date")));
					}
					task.setPinned(taskObj.getBoolean("pinned"));
					if (taskObj.has("pinned_date")) {
						task.setPinnedDate(new Date(taskObj.getLong("pinned_date")));
					}
					taskRepo.addTask(task);
				}
				taskRepo.close();
			}

			Toast.makeText(context, "Данные успешно импортированы", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Ошибка при импорте данных", Toast.LENGTH_SHORT).show();
		}
	}
}