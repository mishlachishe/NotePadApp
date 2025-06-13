package ru.mihailkl.notepadapp.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import ru.mihailkl.notepadapp.R;
import ru.mihailkl.notepadapp.models.Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
	private List<Note> notes;
	private OnNoteClickListener listener;
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
				if (notes.get(from).isPinned() || notes.get(to).isPinned()) {
					return false;
				}
				// Проверка границ
				{
					if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
						return false;
					}
				}

				// Ограничиваем перемещение в пределах списка
				to = Math.max(0, Math.min(to, notes.size() - 1));

				// Перемещаем данные
				if (from < to) {
					for (int i = from; i < to; i++) {
						Collections.swap(notes, i, i + 1);
					}
				} else {
					for (int i = from; i > to; i--) {
						Collections.swap(notes, i, i - 1);
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

	public interface OnNoteClickListener {
		void onNoteClick(Note note);
		void onNoteLongClick(Note note);
		void onTogglePin(Note note);
	}

	public NoteAdapter(List<Note> notes, OnNoteClickListener listener) {
		this.notes = notes;
		this.listener = listener;
	}

	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		touchHelper.attachToRecyclerView(recyclerView);
	}

	@NonNull
	@Override
	public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
								  .inflate(R.layout.item_note, parent, false);
		return new NoteViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
		Note note = notes.get(position);
		holder.bind(note, listener);
		ImageView pinIcon = holder.itemView.findViewById(R.id.pin_icon);
		pinIcon.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);
		holder.itemView.setOnLongClickListener(v -> {
			PopupMenu popup = new PopupMenu(v.getContext(), v);
			popup.inflate(R.menu.context_menu);

			// Обновляем текст пункта меню
			MenuItem pinItem = popup.getMenu().findItem(R.id.action_pin);
			pinItem.setTitle(note.isPinned() ? "Открепить" : "Закрепить");

			popup.setOnMenuItemClickListener(item -> {
				if (item.getItemId() == R.id.action_delete) {
					listener.onNoteLongClick(note);
					return true;
				} else if (item.getItemId() == R.id.action_pin) {
					listener.onTogglePin(note);
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
		return notes.size();
	}

	public void updateNotes(List<Note> newNotes) {
		notes = newNotes;
		notifyDataSetChanged();
	}

	class NoteViewHolder extends RecyclerView.ViewHolder {
		private CardView cardView;
		private TextView titleTextView;
		private TextView contentTextView;
		private TextView dateTextView;

		public NoteViewHolder(@NonNull View itemView) {
			super(itemView);
			cardView = itemView.findViewById(R.id.note_card);
			titleTextView = itemView.findViewById(R.id.note_title);
			contentTextView = itemView.findViewById(R.id.note_content);
			dateTextView = itemView.findViewById(R.id.note_date);

		}

		@SuppressLint("ClickableViewAccessibility")
		public void bind(final Note note, final OnNoteClickListener listener) {

			titleTextView.setText(note.getTitle());
			contentTextView.setText(note.getContent());
			dateTextView.setText(note.getFormattedDate());
			itemView.setOnClickListener(v -> listener.onNoteClick(note));

			itemView.setOnTouchListener((v, event) -> {
				if (note.isPinned()) {
					return false;
				}
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					// Добавляем небольшую задержку для лучшего UX
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