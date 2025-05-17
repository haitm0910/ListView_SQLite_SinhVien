package com.example.listview_sinhvien;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "QuanLySinhVien.db";
    private static final int DATABASE_VERSION = 2; // Giữ nguyên hoặc tăng nếu có thay đổi schema khác

    private static final String TABLE_SINHVIEN = "SinhVien";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_HOTEN = "hoTen";
    private static final String COLUMN_MSSV = "mssv";
    private static final String COLUMN_AVATAR_PATH = "avatarPath";
    private static final String COLUMN_CHUYENNGANH = "chuyenNganh";
    private static final String COLUMN_NGAYSINH = "ngaySinh";
    private static final String COLUMN_KHOAHOC = "khoaHoc";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SINHVIEN_TABLE = "CREATE TABLE " + TABLE_SINHVIEN + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_HOTEN + " TEXT NOT NULL,"
                + COLUMN_MSSV + " TEXT NOT NULL UNIQUE,"
                + COLUMN_AVATAR_PATH + " TEXT,"
                + COLUMN_CHUYENNGANH + " TEXT,"
                + COLUMN_NGAYSINH + " TEXT,"
                + COLUMN_KHOAHOC + " TEXT"
                + ")";
        db.execSQL(CREATE_SINHVIEN_TABLE);
        Log.i(TAG, "Bảng SinhVien đã được tạo.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Nâng cấp database từ phiên bản " + oldVersion + " lên " + newVersion + ". Dữ liệu cũ sẽ bị xóa.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SINHVIEN);
        onCreate(db);
    }

    public long addSinhVien(SinhVien sinhVien) {
        SQLiteDatabase db = null;
        long id = -1;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(COLUMN_HOTEN, sinhVien.getHoTen());
            values.put(COLUMN_MSSV, sinhVien.getMssv());
            values.put(COLUMN_AVATAR_PATH, sinhVien.getAvatarPath());
            values.put(COLUMN_CHUYENNGANH, sinhVien.getChuyenNganh());
            values.put(COLUMN_NGAYSINH, sinhVien.getNgaySinh());
            values.put(COLUMN_KHOAHOC, sinhVien.getKhoaHoc());

            id = db.insertOrThrow(TABLE_SINHVIEN, null, values);
            db.setTransactionSuccessful();
            Log.i(TAG, "Đã thêm sinh viên: " + sinhVien.getHoTen() + " với ID: " + id);

        } catch (Exception e) {
            Log.e(TAG, "Lỗi Exception khi thêm sinh viên: " + sinhVien.getHoTen(), e);
            id = -1;
        } finally {
            if (db != null) {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
                db.close();
            }
        }
        return id;
    }

    public ArrayList<SinhVien> getAllSinhViens() {
        ArrayList<SinhVien> sinhViens = new ArrayList<>();
        String SELECT_QUERY = "SELECT * FROM " + TABLE_SINHVIEN + " ORDER BY " + COLUMN_HOTEN + " ASC";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(SELECT_QUERY, null);
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String hoTen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOTEN));
                    String mssv = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MSSV));
                    String avatarPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_PATH));
                    String chuyenNganh = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHUYENNGANH));
                    String ngaySinh = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NGAYSINH));
                    String khoaHoc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KHOAHOC));
                    sinhViens.add(new SinhVien(id, hoTen, mssv, avatarPath, chuyenNganh, ngaySinh, khoaHoc));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi truy vấn tất cả sinh viên: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        Log.d(TAG, "Lấy được " + sinhViens.size() + " sinh viên.");
        return sinhViens;
    }

    // Lấy một sinh viên theo ID (Cần cho chức năng sửa)
    public SinhVien getStudentById(int studentId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        SinhVien sinhVien = null;
        try {
            db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_SINHVIEN + " WHERE " + COLUMN_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});

            if (cursor.moveToFirst()) {
                String hoTen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOTEN));
                String mssv = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MSSV));
                String avatarPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_PATH));
                String chuyenNganh = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHUYENNGANH));
                String ngaySinh = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NGAYSINH));
                String khoaHoc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KHOAHOC));
                sinhVien = new SinhVien(studentId, hoTen, mssv, avatarPath, chuyenNganh, ngaySinh, khoaHoc);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi lấy sinh viên theo ID: " + studentId, e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return sinhVien;
    }


    // Cập nhật sinh viên (Cần cho chức năng sửa)
    public int updateSinhVien(SinhVien sinhVien) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(COLUMN_HOTEN, sinhVien.getHoTen());
            values.put(COLUMN_MSSV, sinhVien.getMssv());
            values.put(COLUMN_AVATAR_PATH, sinhVien.getAvatarPath());
            values.put(COLUMN_CHUYENNGANH, sinhVien.getChuyenNganh());
            values.put(COLUMN_NGAYSINH, sinhVien.getNgaySinh());
            values.put(COLUMN_KHOAHOC, sinhVien.getKhoaHoc());

            rowsAffected = db.update(TABLE_SINHVIEN, values, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(sinhVien.getId())});
            db.setTransactionSuccessful();
            Log.i(TAG, "Cập nhật sinh viên ID " + sinhVien.getId() + ". Số dòng bị ảnh hưởng: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi Exception khi cập nhật sinh viên ID " + sinhVien.getId(), e);
            rowsAffected = 0; // Hoặc -1 để biểu thị lỗi
        } finally {
            if (db != null) {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
                db.close();
            }
        }
        return rowsAffected;
    }

    // Xóa sinh viên (Cần cho chức năng xóa)
    public void deleteSinhVien(int id) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();
            int rowsDeleted = db.delete(TABLE_SINHVIEN, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            Log.i(TAG, "Xóa sinh viên ID " + id + ". Số dòng bị xóa: " + rowsDeleted);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi Exception khi xóa sinh viên ID " + id, e);
        } finally {
            if (db != null) {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
                db.close();
            }
        }
    }
}
