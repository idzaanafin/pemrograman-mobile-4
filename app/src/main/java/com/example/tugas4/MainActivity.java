package com.example.tugas4;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private EditText nrp, nama, jurusan, fakultas, noHp;
    private TextInputLayout layoutNrp, layoutNama, layoutJurusan, layoutFakultas, layoutNoHp;
    private SQLiteDatabase dbku;
    private SQLiteOpenHelper openDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding View Elements
        nrp = findViewById(R.id.nrp);
        nama = findViewById(R.id.nama);
        jurusan = findViewById(R.id.jurusan);
        fakultas = findViewById(R.id.fakultas);
        noHp = findViewById(R.id.no_hp);

        layoutNrp = findViewById(R.id.layoutNrp);
        layoutNama = findViewById(R.id.layoutNama);
        layoutJurusan = findViewById(R.id.layoutJurusan);
        layoutFakultas = findViewById(R.id.layoutFakultas);
        layoutNoHp = findViewById(R.id.layoutNoHp);

        // Button Click Listeners
        findViewById(R.id.btnSimpan).setOnClickListener(v -> simpan());
        findViewById(R.id.btnCari).setOnClickListener(v -> cari());
        findViewById(R.id.btnUpdate).setOnClickListener(v -> update());
        findViewById(R.id.btnHapus).setOnClickListener(v -> hapus());
        findViewById(R.id.btnClear).setOnClickListener(v -> clearForm());

        // Inisialisasi SQLiteOpenHelper
        openDb = new SQLiteOpenHelper(this, "db_mahasiswa", null, 2) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE IF NOT EXISTS mhs (" +
                        "nrp TEXT PRIMARY KEY, " +
                        "nama TEXT, " +
                        "jurusan TEXT, " +
                        "fakultas TEXT, " +
                        "no_hp TEXT);");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS mhs;");
                onCreate(db);
            }
        };

        dbku = openDb.getWritableDatabase();

        // Skema tabel & migrasi aman jika aplikasi diperbarui dari versi sebelumnya
        dbku.execSQL("CREATE TABLE IF NOT EXISTS mhs (" +
                "nrp TEXT PRIMARY KEY, " +
                "nama TEXT, " +
                "jurusan TEXT, " +
                "fakultas TEXT, " +
                "no_hp TEXT);");

        try {
            dbku.execSQL("ALTER TABLE mhs ADD COLUMN jurusan TEXT;");
        } catch (Exception ignored) {}
        try {
            dbku.execSQL("ALTER TABLE mhs ADD COLUMN fakultas TEXT;");
        } catch (Exception ignored) {}
        try {
            dbku.execSQL("ALTER TABLE mhs ADD COLUMN no_hp TEXT;");
        } catch (Exception ignored) {}

        // Memuat NRP terakhir yang dicari dari SharedPreferences
        SharedPreferences sp = getSharedPreferences("pref_mhs", MODE_PRIVATE);
        String lastNrp = sp.getString("last_nrp", "");
        if (!lastNrp.isEmpty()) {
            nrp.setText(lastNrp);
            cari();
        }
    }

    @Override
    protected void onStop() {
        if (dbku != null && dbku.isOpen()) {
            dbku.close();
        }
        super.onStop();
    }

    private boolean validateNrp() {
        String strNrp = nrp.getText().toString().trim();
        if (strNrp.isEmpty()) {
            layoutNrp.setError("NRP tidak boleh kosong!");
            return false;
        } else if (strNrp.length() != 10 || !strNrp.matches("\\d{10}")) {
            layoutNrp.setError("NRP harus berupa 10 digit angka!");
            return false;
        } else {
            layoutNrp.setError(null);
            return true;
        }
    }

    private boolean validateAllFields() {
        boolean isValid = validateNrp();

        String strNama = nama.getText().toString().trim();
        if (strNama.isEmpty()) {
            layoutNama.setError("Nama tidak boleh kosong!");
            isValid = false;
        } else {
            layoutNama.setError(null);
        }

        String strJurusan = jurusan.getText().toString().trim();
        if (strJurusan.isEmpty()) {
            layoutJurusan.setError("Jurusan tidak boleh kosong!");
            isValid = false;
        } else {
            layoutJurusan.setError(null);
        }

        String strFakultas = fakultas.getText().toString().trim();
        if (strFakultas.isEmpty()) {
            layoutFakultas.setError("Fakultas tidak boleh kosong!");
            isValid = false;
        } else {
            layoutFakultas.setError(null);
        }

        String strNoHp = noHp.getText().toString().trim();
        if (strNoHp.isEmpty()) {
            layoutNoHp.setError("Nomor HP tidak boleh kosong!");
            isValid = false;
        } else {
            layoutNoHp.setError(null);
        }

        return isValid;
    }

    private void clearErrors() {
        layoutNrp.setError(null);
        layoutNama.setError(null);
        layoutJurusan.setError(null);
        layoutFakultas.setError(null);
        layoutNoHp.setError(null);
    }

    private void clearForm() {
        nrp.setText("");
        nama.setText("");
        jurusan.setText("");
        fakultas.setText("");
        noHp.setText("");
        clearErrors();
        nrp.requestFocus();
        showToast("🧹 Form berhasil dibersihkan");
    }

    private void simpan() {
        clearErrors();
        if (!validateAllFields()) {
            showToast("⚠️ Harap perbaiki input yang belum sesuai!");
            return;
        }

        String strNrp = nrp.getText().toString().trim();
        String strNama = nama.getText().toString().trim();
        String strJurusan = jurusan.getText().toString().trim();
        String strFakultas = fakultas.getText().toString().trim();
        String strNoHp = noHp.getText().toString().trim();

        Cursor cur = dbku.rawQuery("SELECT nrp FROM mhs WHERE nrp = ?", new String[]{strNrp});
        boolean exists = (cur.getCount() > 0);
        cur.close();

        if (exists) {
            layoutNrp.setError("NRP ini sudah terdaftar!");
            showToast("❌ Data dengan NRP " + strNrp + " sudah ada! Gunakan Update.");
            return;
        }

        ContentValues data = new ContentValues();
        data.put("nrp", strNrp);
        data.put("nama", strNama);
        data.put("jurusan", strJurusan);
        data.put("fakultas", strFakultas);
        data.put("no_hp", strNoHp);

        long result = dbku.insert("mhs", null, data);
        if (result != -1) {
            showToast("✅ Data Mahasiswa (" + strNrp + ") Berhasil Disimpan");
        } else {
            showToast("❌ Gagal menyimpan data mahasiswa!");
        }
    }

    private void cari() {
        clearErrors();
        if (!validateNrp()) {
            showToast("⚠️ Masukkan NRP 10 digit dengan benar untuk mencari!");
            return;
        }

        String strNrp = nrp.getText().toString().trim();

        // Simpan NRP terakhir yang dicari ke SharedPreferences
        SharedPreferences sp = getSharedPreferences("pref_mhs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("last_nrp", strNrp);
        editor.apply();

        Cursor cur = dbku.rawQuery("SELECT * FROM mhs WHERE nrp = ?", new String[]{strNrp});
        if (cur.moveToFirst()) {
            int idxNama = cur.getColumnIndex("nama");
            int idxJurusan = cur.getColumnIndex("jurusan");
            int idxFakultas = cur.getColumnIndex("fakultas");
            int idxNoHp = cur.getColumnIndex("no_hp");

            nama.setText(idxNama != -1 ? cur.getString(idxNama) : "");
            jurusan.setText(idxJurusan != -1 ? cur.getString(idxJurusan) : "");
            fakultas.setText(idxFakultas != -1 ? cur.getString(idxFakultas) : "");
            noHp.setText(idxNoHp != -1 ? cur.getString(idxNoHp) : "");

            showToast("🔍 Data Mahasiswa (" + strNrp + ") Ditemukan");
        } else {
            nama.setText("");
            jurusan.setText("");
            fakultas.setText("");
            noHp.setText("");
            showToast("❌ Data Mahasiswa dengan NRP " + strNrp + " Tidak Ditemukan");
        }
        cur.close();
    }

    private void update() {
        clearErrors();
        if (!validateAllFields()) {
            showToast("⚠️ Harap perbaiki input sebelum melakukan update!");
            return;
        }

        String strNrp = nrp.getText().toString().trim();
        String strNama = nama.getText().toString().trim();
        String strJurusan = jurusan.getText().toString().trim();
        String strFakultas = fakultas.getText().toString().trim();
        String strNoHp = noHp.getText().toString().trim();

        Cursor cur = dbku.rawQuery("SELECT nrp FROM mhs WHERE nrp = ?", new String[]{strNrp});
        boolean exists = (cur.getCount() > 0);
        cur.close();

        if (!exists) {
            layoutNrp.setError("NRP tidak ditemukan di database!");
            showToast("❌ Data dengan NRP " + strNrp + " tidak ditemukan untuk di-update.");
            return;
        }

        ContentValues data = new ContentValues();
        data.put("nama", strNama);
        data.put("jurusan", strJurusan);
        data.put("fakultas", strFakultas);
        data.put("no_hp", strNoHp);

        int rowsAffected = dbku.update("mhs", data, "nrp = ?", new String[]{strNrp});
        if (rowsAffected > 0) {
            showToast("✏️ Data Mahasiswa (" + strNrp + ") Berhasil Di-update");
        } else {
            showToast("❌ Gagal meng-update data mahasiswa!");
        }
    }

    private void hapus() {
        clearErrors();
        if (!validateNrp()) {
            showToast("⚠️ Masukkan NRP 10 digit dengan benar untuk menghapus!");
            return;
        }

        String strNrp = nrp.getText().toString().trim();

        Cursor cur = dbku.rawQuery("SELECT nrp FROM mhs WHERE nrp = ?", new String[]{strNrp});
        boolean exists = (cur.getCount() > 0);
        cur.close();

        if (!exists) {
            layoutNrp.setError("NRP tidak ditemukan di database!");
            showToast("❌ Data dengan NRP " + strNrp + " tidak ditemukan untuk dihapus.");
            return;
        }

        int rowsDeleted = dbku.delete("mhs", "nrp = ?", new String[]{strNrp});
        if (rowsDeleted > 0) {
            clearForm();
            showToast("🗑️ Data Mahasiswa (" + strNrp + ") Berhasil Dihapus");
        } else {
            showToast("❌ Gagal menghapus data mahasiswa!");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
