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

public class MainActivity extends AppCompatActivity {
    private EditText nrp, nama;
    private SQLiteDatabase dbku;
    private SQLiteOpenHelper openDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nrp = findViewById(R.id.nrp);
        nama = findViewById(R.id.nama);

        findViewById(R.id.btnSimpan).setOnClickListener(v -> simpan());
        findViewById(R.id.btnCari).setOnClickListener(v -> cari());
        findViewById(R.id.btnUpdate).setOnClickListener(v -> update());
        findViewById(R.id.btnHapus).setOnClickListener(v -> hapus());

        openDb = new SQLiteOpenHelper(this, "db_mahasiswa", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) { }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
        };

        dbku = openDb.getWritableDatabase();
        dbku.execSQL("create table if not exists mhs(nrp TEXT, nama TEXT);");

        // Memuat NRP terakhir yang dicari dari SharedPreferences
        SharedPreferences sp = getSharedPreferences("pref_mhs", MODE_PRIVATE);
        String lastNrp = sp.getString("last_nrp", "");
        nrp.setText(lastNrp);
    }

    @Override
    protected void onStop() {
        if (dbku != null && dbku.isOpen()) {
            dbku.close();
        }
        super.onStop();
    }

    private void simpan() {
        String strNrp = nrp.getText().toString().trim();
        String strNama = nama.getText().toString().trim();

        if (strNrp.isEmpty()) {
            Toast.makeText(this, "NRP tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues data = new ContentValues();
        data.put("nrp", strNrp);
        data.put("nama", strNama);
        dbku.insert("mhs", null, data);
        Toast.makeText(this, "Data Tersimpan", Toast.LENGTH_LONG).show();
    }

    private void cari() {
        String strNrp = nrp.getText().toString().trim();

        // Simpan NRP terakhir yang dicari ke SharedPreferences
        SharedPreferences sp = getSharedPreferences("pref_mhs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("last_nrp", strNrp);
        editor.apply();

        Cursor cur = dbku.rawQuery("select * from mhs where nrp='" + strNrp + "'", null);
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            int indexNama = cur.getColumnIndex("nama");
            if (indexNama != -1) {
                nama.setText(cur.getString(indexNama));
            } else {
                nama.setText(cur.getString(1));
            }
            Toast.makeText(this, "Data Ditemukan", Toast.LENGTH_LONG).show();
        } else {
            nama.setText("");
            Toast.makeText(this, "Data Tidak Ditemukan", Toast.LENGTH_LONG).show();
        }
        cur.close();
    }

    private void update() {
        String strNrp = nrp.getText().toString().trim();
        String strNama = nama.getText().toString().trim();

        ContentValues data = new ContentValues();
        data.put("nrp", strNrp);
        data.put("nama", strNama);
        dbku.update("mhs", data, "nrp='" + strNrp + "'", null);
        Toast.makeText(this, "Data Terupdate", Toast.LENGTH_LONG).show();
    }

    private void hapus() {
        String strNrp = nrp.getText().toString().trim();

        dbku.delete("mhs", "nrp='" + strNrp + "'", null);
        nama.setText("");
        Toast.makeText(this, "Data Terhapus", Toast.LENGTH_LONG).show();
    }
}
