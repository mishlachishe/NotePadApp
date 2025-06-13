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

import ru.mihailkl.notepadapp.R;
import ru.mihailkl.notepadapp.activities.TaskActivity;
import ru.mihailkl.notepadapp.adapters.TaskAdapter;
import ru.mihailkl.notepadapp.models.Task;
import ru.mihailkl.notepadapp.repositories.TaskRepository;

public class TaskFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
	private RecyclerView tasksRecyclerView;
	private TaskAdapter tasksAdapter;
	private TaskRepository tasksRepository;
	private List<Task> tasks;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_task, container, false);

		tasksRepository = new TaskRepository(getContext());
		tasksRepository.open();
		tasks = tasksRepository.getAllTasks();

		tasksRecyclerView = view.findViewById(R.id.tasks_recycler_view);
		tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		tasksAdapter = new TaskAdapter(tasks, this);
		tasksRecyclerView.setAdapter(tasksAdapter);

		FloatingActionButton fab = view.findViewById(R.id.fab_add_task);
		fab.setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), TaskActivity.class);
			startActivity(intent);
		});

		return view;
	}
	@Override
	public void onTogglePin(Task task) {
		tasksRepository.togglePin(task);
		refreshTasks();

		Toast.makeText(getContext(),
				task.isPinned() ? "Задача закреплена" : "Задача откреплена",
				Toast.LENGTH_SHORT
		).show();
		if (task.isPinned()) {
			tasksRecyclerView.smoothScrollToPosition(0);
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		refreshTasks();
	}

	@Override
	public void onDestroyView() {
		tasksRepository.close();
		super.onDestroyView();
	}

	private void refreshTasks() {
		tasks = tasksRepository.getAllTasks();
		tasksAdapter.updateTasks(tasks);
	}

	@Override
	public void onTaskClick(Task task) {
		Intent intent = new Intent(getActivity(), TaskActivity.class);
		intent.putExtra("task_id", task.getId());
		startActivity(intent);
	}

	@Override
	public void onTaskLongClick(Task task) {
		new AlertDialog.Builder(requireContext())
				.setTitle("Удаление заметки")
				.setMessage("Вы уверены, что хотите удалить эту заметку?")
				.setPositiveButton("Удалить", (dialog, which) -> {
					tasksRepository.deleteTask(task);
					refreshTasks();
				})
				.setNegativeButton("Отмена", null)
				.show();
	}

	@Override
	public void onTaskCheckedChange(Task task, boolean isChecked) {
		task.setCompleted(isChecked);
		tasksRepository.updateTask(task);
	}
}