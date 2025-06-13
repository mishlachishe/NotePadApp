package ru.mihailkl.notepadapp.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.mihailkl.notepadapp.models.Note;
import ru.mihailkl.notepadapp.models.Task;
import ru.mihailkl.notepadapp.repositories.NoteRepository;
import ru.mihailkl.notepadapp.repositories.TaskRepository;

public class DataExporter {
	private Context context;

	public DataExporter(Context context) {
		this.context = context;
	}

	public void exportData() {
		// Получаем данные из репозиториев
		NoteRepository noteRepository = new NoteRepository(context);
		noteRepository.open();
		List<Note> notes = noteRepository.getAllNotes();
		noteRepository.close();

		TaskRepository taskRepository = new TaskRepository(context);
		taskRepository.open();
		List<Task> tasks = taskRepository.getAllTasks();
		taskRepository.close();

		// Преобразуем в JSON
		try {
			JSONObject exportData = new JSONObject();

			// Добавляем заметки
			JSONArray notesArray = new JSONArray();
			for (Note note : notes) {
				JSONObject noteObj = new JSONObject();
				noteObj.put("id", note.getId());
				noteObj.put("title", note.getTitle());
				noteObj.put("content", note.getContent());
				noteObj.put("created_at", note.getCreatedAt().getTime());
				noteObj.put("pinned", note.isPinned());
				if (note.getPinnedDate() != null) {
					noteObj.put("pinned_date", note.getPinnedDate().getTime());
				}
				notesArray.put(noteObj);
			}
			exportData.put("notes", notesArray);

			// Добавляем задачи
			JSONArray tasksArray = new JSONArray();
			for (Task task : tasks) {
				JSONObject taskObj = new JSONObject();
				taskObj.put("id", task.getId());
				taskObj.put("title", task.getTitle());
				taskObj.put("description", task.getDescription());
				taskObj.put("completed", task.isCompleted());
				taskObj.put("created_at", task.getCreatedAt().getTime());
				if (task.getDueDate() != null) {
					taskObj.put("due_date", task.getDueDate().getTime());
				}
				taskObj.put("pinned", task.isPinned());
				if (task.getPinnedDate() != null) {
					taskObj.put("pinned_date", task.getPinnedDate().getTime());
				}
				tasksArray.put(taskObj);
			}
			exportData.put("tasks", tasksArray);

			// Сохраняем в файл
			saveToFile(exportData.toString());

		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(context, "Ошибка при формировании данных", Toast.LENGTH_SHORT).show();
		}
	}
	public String getExportData() throws JSONException {
		// Создаем основной JSON объект для экспорта
		JSONObject exportData = new JSONObject();

		// Получаем данные из репозиториев
		NoteRepository noteRepository = new NoteRepository(context);
		noteRepository.open();
		List<Note> notes = noteRepository.getAllNotes();
		noteRepository.close();

		TaskRepository taskRepository = new TaskRepository(context);
		taskRepository.open();
		List<Task> tasks = taskRepository.getAllTasks();
		taskRepository.close();

		// Добавляем заметки в JSON
		JSONArray notesArray = new JSONArray();
		for (Note note : notes) {
			JSONObject noteObj = new JSONObject();
			noteObj.put("id", note.getId());
			noteObj.put("title", note.getTitle());
			noteObj.put("content", note.getContent());
			noteObj.put("created_at", note.getCreatedAt().getTime());
			noteObj.put("pinned", note.isPinned());
			if (note.getPinnedDate() != null) {
				noteObj.put("pinned_date", note.getPinnedDate().getTime());
			}
			notesArray.put(noteObj);
		}
		exportData.put("notes", notesArray);

		// Добавляем задачи в JSON
		JSONArray tasksArray = new JSONArray();
		for (Task task : tasks) {
			JSONObject taskObj = new JSONObject();
			taskObj.put("id", task.getId());
			taskObj.put("title", task.getTitle());
			taskObj.put("description", task.getDescription());
			taskObj.put("completed", task.isCompleted());
			taskObj.put("created_at", task.getCreatedAt().getTime());
			if (task.getDueDate() != null) {
				taskObj.put("due_date", task.getDueDate().getTime());
			}
			taskObj.put("pinned", task.isPinned());
			if (task.getPinnedDate() != null) {
				taskObj.put("pinned_date", task.getPinnedDate().getTime());
			}
			tasksArray.put(taskObj);
		}
		exportData.put("tasks", tasksArray);

		// Метаданные о экспорте
		JSONObject meta = new JSONObject();
		meta.put("export_date", System.currentTimeMillis());
		meta.put("app_version", "v1.0.0");
		meta.put("item_count", notes.size() + tasks.size());
		exportData.put("meta", meta);

		return exportData.toString();
	}
	public void exportToCustomLocation(Uri uri, Context context) {
		try {
			String jsonData = getExportData();
			OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
			outputStream.write(jsonData.getBytes());
			outputStream.close();
			Toast.makeText(context, "Данные успешно экспортированы", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Ошибка при экспорте", Toast.LENGTH_SHORT).show();
		}
	}
	public void exportToDefaultLocation() throws JSONException {
		String jsonData = getExportData();
		saveToFile(jsonData); // Метод из предыдущего примера
	}
	private void saveToFile(String data) {
		try {
			// Создаем директорию, если ее нет
			File exportDir = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOCUMENTS), "NotepadApp");
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}

			// Формируем имя файла с текущей датой
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
			String fileName = "notepad_export_" + sdf.format(new Date()) + ".json";

			// Записываем данные в файл
			File exportFile = new File(exportDir, fileName);
			FileWriter writer = new FileWriter(exportFile);
			writer.write(data);
			writer.close();

			Toast.makeText(context,
					"Данные экспортированы в: " + exportFile.getAbsolutePath(),
					Toast.LENGTH_LONG
			).show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(context, "Ошибка при экспорте данных", Toast.LENGTH_SHORT).show();
		}
	}
}