package ru.mihailkl.notepadapp.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.mihailkl.notepadapp.R;
import ru.mihailkl.notepadapp.models.Task;
import ru.mihailkl.notepadapp.repositories.TaskRepository;

public class TaskActivity extends AppCompatActivity {

	private EditText titleEditText;
	private EditText descriptionEditText;
	private CheckBox completedCheckBox;
	private TextView dueDateTextView;
	private TaskRepository tasksRepository;
	private Task currentTask;
	private Calendar calendar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_task);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.acttask), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		ConstraintLayout saveButton = findViewById(R.id.save_container);
		saveButton.setOnClickListener(v -> saveTask());
		titleEditText = findViewById(R.id.task_title);
		descriptionEditText = findViewById(R.id.task_description);
		completedCheckBox = findViewById(R.id.task_completed);
		dueDateTextView = findViewById(R.id.task_due_date);

		tasksRepository = new TaskRepository(this);
		tasksRepository.open();
		calendar = Calendar.getInstance();

		// Проверяем, передана ли задача для редактирования
		if (getIntent().hasExtra("task_id")) {
			int taskId = getIntent().getIntExtra("task_id", -1);
			if (taskId != -1) {
				for (Task task : tasksRepository.getAllTasks()) {
					if (task.getId() == taskId) {
						currentTask = task;
						break;
					}
				}

				if (currentTask != null) {
					titleEditText.setText(currentTask.getTitle());
					descriptionEditText.setText(currentTask.getDescription());
					completedCheckBox.setChecked(currentTask.isCompleted());

					if (currentTask.getDueDate() != null) {
						calendar.setTime(currentTask.getDueDate());
						updateDueDateText();
					}
				}
			}
		}

		dueDateTextView.setOnClickListener(v -> showDatePicker());
	}

	private void showDatePicker() {
		DatePickerDialog datePickerDialog = new DatePickerDialog(
				this,
				(view, year, month, dayOfMonth) -> {
					calendar.set(Calendar.YEAR, year);
					calendar.set(Calendar.MONTH, month);
					calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					updateDueDateText();
				},
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH)
		);
		datePickerDialog.show();
	}

	private void updateDueDateText() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
		dueDateTextView.setText(sdf.format(calendar.getTime()));
	}

	private void saveTask() {
		String title = titleEditText.getText().toString().trim();
		String description = descriptionEditText.getText().toString().trim();
		boolean completed = completedCheckBox.isChecked();

		if (title.isEmpty()) {
			Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show();
			return;
		}

		Date dueDate = null;
		if (!dueDateTextView.getText().toString().equals("Выберите дату")) {
			dueDate = calendar.getTime();
		}

		if (currentTask == null) {
			// Создание новой задачи
			Task task = new Task(title, description, dueDate);
			task.setCompleted(completed);
			tasksRepository.addTask(task);
			Toast.makeText(this, "Задача сохранена", Toast.LENGTH_SHORT).show();
		} else {
			// Обновление существующей задачи
			currentTask.setTitle(title);
			currentTask.setDescription(description);
			currentTask.setCompleted(completed);
			currentTask.setDueDate(dueDate);
			tasksRepository.updateTask(currentTask);
			Toast.makeText(this, "Задача обновлена", Toast.LENGTH_SHORT).show();
		}

		finish();
	}

	@Override
	protected void onDestroy() {
		tasksRepository.close();
		super.onDestroy();
	}
}