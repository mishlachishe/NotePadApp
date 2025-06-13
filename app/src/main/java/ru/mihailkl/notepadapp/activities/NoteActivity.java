package ru.mihailkl.notepadapp.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ru.mihailkl.notepadapp.R;
import ru.mihailkl.notepadapp.models.Note;
import ru.mihailkl.notepadapp.repositories.NoteRepository;

public class NoteActivity extends AppCompatActivity {

	private EditText titleEditText;
	private EditText contentEditText;
	private NoteRepository notesRepository;
	private Note currentNote;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_note);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.actnote), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		titleEditText = findViewById(R.id.note_title);
		contentEditText = findViewById(R.id.note_content);
		ConstraintLayout saveButton = findViewById(R.id.save_container);
		saveButton.setOnClickListener(v -> saveNote());
		notesRepository = new NoteRepository(this);
		notesRepository.open();

		// Проверяем, передана ли заметка для редактирования
		if (getIntent().hasExtra("note_id")) {
			int noteId = getIntent().getIntExtra("note_id", -1);
			if (noteId != -1) {
				for (Note note : notesRepository.getAllNotes()) {
					if (note.getId() == noteId) {
						currentNote = note;
						break;
					}
				}

				if (currentNote != null) {
					titleEditText.setText(currentNote.getTitle());
					contentEditText.setText(currentNote.getContent());
				}
			}
		}
	}

	private void saveNote() {
		String title = titleEditText.getText().toString().trim();
		String content = contentEditText.getText().toString().trim();

		if (title.isEmpty()) {
			Toast.makeText(this, "Введите заголовок", Toast.LENGTH_SHORT).show();
			return;
		}

		if (currentNote == null) {
			// Создание новой заметки
			Note note = new Note(title, content);
			notesRepository.addNote(note);
			Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
		} else {
			// Обновление существующей заметки
			currentNote.setTitle(title);
			currentNote.setContent(content);
			notesRepository.updateNote(currentNote);
			Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
		}

		finish();
	}

	@Override
	protected void onDestroy() {
		notesRepository.close();
		super.onDestroy();
	}
}