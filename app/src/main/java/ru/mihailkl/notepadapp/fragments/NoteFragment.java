package ru.mihailkl.notepadapp.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import ru.mihailkl.notepadapp.activities.NoteActivity;
import ru.mihailkl.notepadapp.R;
import ru.mihailkl.notepadapp.adapters.NoteAdapter;
import ru.mihailkl.notepadapp.models.Note;
import ru.mihailkl.notepadapp.repositories.NoteRepository;

public class NoteFragment extends Fragment implements NoteAdapter.OnNoteClickListener {
	private RecyclerView notesRecyclerView;
	private NoteAdapter notesAdapter;
	private NoteRepository notesRepository;
	private List<Note> notes;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_note, container, false);
		notesRepository = new NoteRepository(getContext());
		notesRepository.open();
		notes = notesRepository.getAllNotes();

		notesRecyclerView = view.findViewById(R.id.notes_recycler_view);
		notesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		notesAdapter = new NoteAdapter(notes, this);
		notesRecyclerView.setAdapter(notesAdapter);

		FloatingActionButton fab = view.findViewById(R.id.fab_add_note);
		fab.setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), NoteActivity.class);
			startActivity(intent);
		});

		return view;
	}

	@Override
	public void onTogglePin(Note note) {
		notesRepository.togglePin(note);
		refreshNotes();
		Toast.makeText(getContext(),
				note.isPinned() ? "Задача закреплена" : "Задача откреплена",
				Toast.LENGTH_SHORT
		).show();
		if (note.isPinned()) {
			notesRecyclerView.smoothScrollToPosition(0);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshNotes();
	}

	@Override
	public void onDestroyView() {
		notesRepository.close();
		super.onDestroyView();
	}

	private void refreshNotes() {
		notes = notesRepository.getAllNotes();
		notesAdapter.updateNotes(notes);
	}

	@Override
	public void onNoteClick(Note note) {
		Intent intent = new Intent(getActivity(), NoteActivity.class);
		intent.putExtra("note_id", note.getId());
		startActivity(intent);
	}

	@Override
	public void onNoteLongClick(Note note) {
		new AlertDialog.Builder(requireContext())
				.setTitle("Удаление заметки")
				.setMessage("Вы уверены, что хотите удалить эту заметку?")
				.setPositiveButton("Удалить", (dialog, which) -> {
					notesRepository.deleteNote(note);
					refreshNotes();
				})
				.setNegativeButton("Отмена", null)
				.show();
	}
}