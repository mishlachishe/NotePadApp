package ru.mihailkl.notepadapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import ru.mihailkl.notepadapp.R;
import ru.mihailkl.notepadapp.utils.DataExporter;
import ru.mihailkl.notepadapp.utils.DataImporter;

public class SettingsActivity extends AppCompatActivity {
	private static final int REQUEST_WRITE_STORAGE = 1;
	private static final int REQUEST_READ_STORAGE = 2;
	//private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_settings);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.actset), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		setupExportButton();
		setupImportButton();
	}

	private void setupExportButton() {
		findViewById(R.id.export_button).setOnClickListener(v -> {
			// Проверка разрешения
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.WRITE_EXTERNAL_STORAGE
			)
					!= PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						REQUEST_WRITE_STORAGE
				);
			} else {
				// Вот этот вызов должен показывать диалог выбора
				showExportOptionsDialog();
			}
		});
	}
	private void setupImportButton() {
		findViewById(R.id.import_button).setOnClickListener(v -> {
			// Запрос на чтение файлов
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.READ_EXTERNAL_STORAGE
			)
					!= PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
						REQUEST_READ_STORAGE
				);
			} else {
				startFilePicker(false);
			}
		});
	}

	private void showExportOptionsDialog() {
		new AlertDialog.Builder(this)
				.setTitle("Куда экспортировать?")
				.setItems(new String[]{
						"Автоматически в папку Documents",
						"Выбрать место вручную"
				}, (dialog, which) -> {
					if (which == 0) {
						try {
							new DataExporter(this).exportToDefaultLocation();
						} catch (JSONException e) {
							throw new RuntimeException(e);
						}
						Toast.makeText(this, "Экспорт в папку Documents", Toast.LENGTH_SHORT).show();
					} else {
						// Ручной выбор
						startFilePicker(true);
					}
				})
				.setNegativeButton("Отмена", null)
				.show();
	}
	private void startFilePicker(boolean isExport) {
		Intent intent = new Intent(isExport ?
				Intent.ACTION_CREATE_DOCUMENT :
				Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("application/json");

		if (isExport) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
			String fileName = "notepad_export_" + sdf.format(new Date()) + ".json";
			intent.putExtra(Intent.EXTRA_TITLE, fileName);
		}

		startActivityForResult(intent, isExport ? REQUEST_WRITE_STORAGE : REQUEST_READ_STORAGE);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && data != null) {
			Uri uri = data.getData();
			try {
				if (requestCode == REQUEST_WRITE_STORAGE) {
					// Экспорт
					String jsonData = new DataExporter(this).getExportData();
					OutputStream outputStream = getContentResolver().openOutputStream(uri);
					outputStream.write(jsonData.getBytes());
					outputStream.close();
					Toast.makeText(this, "Данные успешно экспортированы", Toast.LENGTH_SHORT).show();
				} else if (requestCode == REQUEST_READ_STORAGE) {
					// Импорт
					InputStream inputStream = getContentResolver().openInputStream(uri);
					new DataImporter(this).importData(inputStream);
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(this, "Ошибка при работе с файлом", Toast.LENGTH_SHORT).show();
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
	}
	// Обработка результата запроса разрешений
	@Override
	public void onRequestPermissionsResult(
			int requestCode,
			@NonNull String[] permissions,
			@NonNull int[] grantResults
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == REQUEST_WRITE_STORAGE) {
			if (grantResults.length > 0 &&
					grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				new DataExporter(this).exportData();
			} else {
				Toast.makeText(this,
						"Для экспорта необходимо разрешение на запись",
						Toast.LENGTH_SHORT
				).show();
			}
		}
	}
}