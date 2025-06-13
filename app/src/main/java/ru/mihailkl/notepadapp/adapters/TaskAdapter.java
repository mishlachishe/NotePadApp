package ru.mihailkl.notepadapp.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import ru.mihailkl.notepadapp.models.Note;
import ru.mihailkl.notepadapp.models.Task;
import ru.mihailkl.notepadapp.R;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
	private List<Task> tasks;
	private OnTaskClickListener listener;
	private final ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
			ItemTouchHelper.UP | ItemTouchHelper.DOWN,
			0
	) {
		@Override
		public boolean onMove(
				@NonNull RecyclerView recyclerView,
				@NonNull RecyclerView.ViewHolder viewHolder,
				@NonNull RecyclerView.ViewHolder target
		) {
			try {
				int from = viewHolder.getAdapterPosition();
				int to = target.getAdapterPosition();
				if (tasks.get(from).isPinned() || tasks.get(to).isPinned()) {
					return false;
				}
				// Проверка границ
				if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
					return false;
				}

				// Ограничиваем перемещение в пределах списка
				to = Math.max(0, Math.min(to, tasks.size() - 1));

				// Перемещаем данные
				if (from < to) {
					for (int i = from; i < to; i++) {
						Collections.swap(tasks, i, i + 1);
					}
				} else {
					for (int i = from; i > to; i--) {
						Collections.swap(tasks, i, i - 1);
					}
				}

				notifyItemMoved(from, to);
				return true;
			} catch (Exception e) {
				Log.e("MoveError", "Error moving item", e);
				return false;
			}
		}

		@Override
		public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
			// Не используется
		}
	});

	public interface OnTaskClickListener {
		void onTaskClick(Task task);
		void onTaskLongClick(Task task);
		void onTaskCheckedChange(Task task, boolean isChecked);
		void onTogglePin(Task task);
	}

	public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
		this.tasks = tasks;
		this.listener = listener;
	}

	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		touchHelper.attachToRecyclerView(recyclerView);
	}

	@NonNull
	@Override
	public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
								  .inflate(R.layout.item_task, parent, false);
		return new TaskViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
		Task task = tasks.get(position);
		holder.bind(task, listener);
		ImageView pinIcon = holder.itemView.findViewById(R.id.pin_icon);
		pinIcon.setVisibility(task.isPinned() ? View.VISIBLE : View.GONE);

		holder.itemView.setOnLongClickListener(v -> {
			PopupMenu popup = new PopupMenu(v.getContext(), v);
			popup.inflate(R.menu.context_menu);

			// Обновляем текст пункта меню
			MenuItem pinItem = popup.getMenu().findItem(R.id.action_pin);
			pinItem.setTitle(task.isPinned() ? "Открепить" : "Закрепить");

			popup.setOnMenuItemClickListener(item -> {
				if (item.getItemId() == R.id.action_delete) {
					listener.onTaskLongClick(task);
					return true;
				} else if (item.getItemId() == R.id.action_pin) {
					listener.onTogglePin(task);
					return true;
				}
				return false;
			});
			popup.show();
			return true;
		});
	}

	@Override
	public int getItemCount() {
		return tasks.size();
	}

	public void updateTasks(List<Task> newTasks) {
		tasks = newTasks;
		notifyDataSetChanged();
	}

	class TaskViewHolder extends RecyclerView.ViewHolder {
		private CardView cardView;
		private CheckBox completedCheckBox;
		private TextView titleTextView;
		private TextView descriptionTextView;
		private TextView dueDateTextView;

		public TaskViewHolder(@NonNull View itemView) {
			super(itemView);
			cardView = itemView.findViewById(R.id.task_card);
			completedCheckBox = itemView.findViewById(R.id.task_completed);
			titleTextView = itemView.findViewById(R.id.task_title);
			descriptionTextView = itemView.findViewById(R.id.task_description);
			dueDateTextView = itemView.findViewById(R.id.task_due_date);
		}

		@SuppressLint("ClickableViewAccessibility")
		public void bind(final Task task, final OnTaskClickListener listener) {
			titleTextView.setText(task.getTitle());
			descriptionTextView.setText(task.getDescription());
			dueDateTextView.setText(task.getFormattedDueDate());
			completedCheckBox.setChecked(task.isCompleted());

			completedCheckBox.setOnCheckedChangeListener(null);
			completedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
				listener.onTaskCheckedChange(task, isChecked);
			});

			itemView.setOnClickListener(v -> listener.onTaskClick(task));

			itemView.setOnTouchListener((v, event) -> {
				if (task.isPinned()) {
					return false;
				}
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					v.postDelayed(() -> {
						try {
							if (getAdapterPosition() != RecyclerView.NO_POSITION) {
								touchHelper.startDrag(this);
							}
						} catch (Exception e) {
							Log.e("DragError", "Error starting drag", e);
						}
					}, 2000);
				}
				return false;
			});
		}
	}
}