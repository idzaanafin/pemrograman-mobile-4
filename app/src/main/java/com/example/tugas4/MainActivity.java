package com.example.tugas4;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private EditText edtSearchNrp, nrp, nama, jurusan, fakultas, noHp;
    private TextInputLayout layoutSearchNrp, layoutNrp, layoutNama, layoutJurusan, layoutFakultas, layoutNoHp;
    private SQLiteDatabase dbku;
    private SQLiteOpenHelper openDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding View Elements Pencarian
        edtSearchNrp = findViewById(R.id.edtSearchNrp);
        layoutSearchNrp = findViewById(R.id.layoutSearchNrp);

        // Binding View Elements Form
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

        // Button Click Listeners & IME Search Action
        findViewById(R.id.btnSearch).setOnClickListener(v -> cari());
        findViewById(R.id.btnSimpan).setOnClickListener(v -> simpan());
        findViewById(R.id.btnUpdate).setOnClickListener(v -> update());
        findViewById(R.id.btnHapus).setOnClickListener(v -> hapus());
        findViewById(R.id.btnClear).setOnClickListener(v -> clearForm());

        edtSearchNrp.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                cari();
                return true;
            }
            return false;
        });

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

        // Memuat NRP terakhir yang dicari dari SharedPreferences saat aplikasi dibuka
        SharedPreferences sp = getSharedPreferences("pref_mhs", MODE_PRIVATE);
        String lastNrp = sp.getString("last_nrp", "");
        if (!lastNrp.isEmpty()) {
            edtSearchNrp.setText(lastNrp);
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

    private boolean validateNrp(EditText editText, TextInputLayout inputLayout) {
        String strNrp = editText.getText().toString().trim();
        if (strNrp.isEmpty()) {
            inputLayout.setError("NRP tidak boleh kosong!");
            return false;
        } else if (strNrp.length() != 10 || !strNrp.matches("\\d{10}")) {
            inputLayout.setError("NRP harus berupa 10 digit angka!");
            return false;
        } else {
            inputLayout.setError(null);
            return true;
        }
    }

    private boolean validateAllFields() {
        boolean isValid = validateNrp(nrp, layoutNrp);

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
        layoutSearchNrp.setError(null);
        layoutNrp.setError(null);
        layoutNama.setError(null);
        layoutJurusan.setError(null);
        layoutFakultas.setError(null);
        layoutNoHp.setError(null);
    }

    private void clearForm() {
        edtSearchNrp.setText("");
        nrp.setText("");
        nama.setText("");
        jurusan.setText("");
        fakultas.setText("");
        noHp.setText("");
        clearErrors();
        edtSearchNrp.requestFocus();
        showToast("🧹 Form berhasil dibersihkan");
    }

    private void cari() {
        clearErrors();

        String strSearchNrp = edtSearchNrp.getText().toString().trim();

        // Jika search bar kosong, coba ambil dari field nrp form
        if (strSearchNrp.isEmpty() && !nrp.getText().toString().trim().isEmpty()) {
            strSearchNrp = nrp.getText().toString().trim();
            edtSearchNrp.setText(strSearchNrp);
        }

        if (!validateNrp(edtSearchNrp, layoutSearchNrp)) {
            showToast("⚠️ Masukkan NRP 10 digit angka pada Search Bar!");
            return;
        }

        // Simpan NRP terakhir yang dicari ke SharedPreferences
        SharedPreferences sp = getSharedPreferences("pref_mhs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("last_nrp", strSearchNrp);
        editor.apply();

        Cursor cur = dbku.rawQuery("SELECT * FROM mhs WHERE nrp = ?", new String[]{strSearchNrp});
        if (cur.moveToFirst()) {
            int idxNrp = cur.getColumnIndex("nrp");
            int idxNama = cur.getColumnIndex("nama");
            int idxJurusan = cur.getColumnIndex("jurusan");
            int idxFakultas = cur.getColumnIndex("fakultas");
            int idxNoHp = cur.getColumnIndex("no_hp");

            // Memasukkan hasil pencarian langsung ke form
            nrp.setText(idxNrp != -1 ? cur.getString(idxNrp) : strSearchNrp);
            nama.setText(idxNama != -1 ? cur.getString(idxNama) : "");
            jurusan.setText(idxJurusan != -1 ? cur.getString(idxJurusan) : "");
            fakultas.setText(idxFakultas != -1 ? cur.getString(idxFakultas) : "");
            noHp.setText(idxNoHp != -1 ? cur.getString(idxNoHp) : "");

            layoutSearchNrp.setError(null);
            showToast("🔍 Data Mahasiswa (" + strSearchNrp + ") Ditemukan & Dimuat ke Form");
        } else {
            layoutSearchNrp.setError("Data Mahasiswa tidak ditemukan!");
            showToast("❌ Data Mahasiswa dengan NRP " + strSearchNrp + " Tidak Ditemukan");
        }
        cur.close();
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
            edtSearchNrp.setText(strNrp);
            showToast("✅ Data Mahasiswa (" + strNrp + ") Berhasil Disimpan");
        } else {
            showToast("❌ Gagal menyimpan data mahasiswa!");
        }
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
        if (!validateNrp(nrp, layoutNrp)) {
            showToast("⚠️ Masukkan NRP 10 digit dengan benar di form untuk menghapus!");
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
