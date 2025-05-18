package com.example.listview_sinhvien;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem; // Import MenuItem
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Import NonNull
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class ChiTiet extends AppCompatActivity {

    private static final String TAG = "ChiTietActivity";

    private ImageView imgAvatarChiTiet;
    private TextView tvHoTenChiTiet, tvMssvChiTiet, tvChuyenNganh, tvNgaySinh, tvKhoaHoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Thông Tin Sinh Viên");
        }

        imgAvatarChiTiet = findViewById(R.id.imgAvatarChiTiet);
        tvHoTenChiTiet = findViewById(R.id.tvHoTenChiTiet);
        tvMssvChiTiet = findViewById(R.id.tvMssvChiTiet);
        tvChuyenNganh = findViewById(R.id.tvChuyenNganh);
        tvNgaySinh = findViewById(R.id.tvNgaySinh);
        tvKhoaHoc = findViewById(R.id.tvKhoaHoc);

        SinhVien sv = (SinhVien) getIntent().getSerializableExtra("sinhvien_chitiet");

        if (sv != null) {
            // Hiển thị thông tin văn bản
            tvHoTenChiTiet.setText(sv.getHoTen() != null ? sv.getHoTen() : "N/A");
            tvMssvChiTiet.setText(sv.getMssv() != null ? "MSSV: " + sv.getMssv() : "N/A");
            tvChuyenNganh.setText(sv.getChuyenNganh() != null ? "Chuyên ngành: " + sv.getChuyenNganh() : "N/A");
            tvNgaySinh.setText(sv.getNgaySinh() != null ? "Ngày sinh: " + sv.getNgaySinh() : "N/A");
            tvKhoaHoc.setText(sv.getKhoaHoc() != null ? "Khóa học: " + sv.getKhoaHoc() : "N/A");

            // Lấy đường dẫn ảnh và hiển thị ảnh
            String avatarPath = sv.getAvatarPath(); // <<=== ĐÃ THAY ĐỔI: sử dụng getAvatarPath()
            if (avatarPath != null && !avatarPath.isEmpty()) {
                File imgFile = new File(avatarPath);
                if (imgFile.exists() && imgFile.isFile()) {
                    try {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        if (myBitmap != null) {
                            imgAvatarChiTiet.setImageBitmap(myBitmap); // <<=== ĐÃ THAY ĐỔI: dùng setImageBitmap
                        } else {
                            Log.e(TAG, "Không thể giải mã ảnh từ: " + avatarPath);
                            imgAvatarChiTiet.setImageResource(R.mipmap.ic_launcher_round); // Ảnh mặc định
                        }
                    } catch (OutOfMemoryError e) {
                        Log.e(TAG, "OutOfMemoryError khi tải ảnh: " + avatarPath, e);
                        Toast.makeText(this, "Ảnh quá lớn để hiển thị", Toast.LENGTH_SHORT).show();
                        imgAvatarChiTiet.setImageResource(R.mipmap.ic_launcher_round);
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi không xác định khi tải ảnh: " + avatarPath, e);
                        imgAvatarChiTiet.setImageResource(R.mipmap.ic_launcher_round);
                    }
                } else {
                    Log.w(TAG, "File ảnh không tồn tại hoặc không phải file: " + avatarPath);
                    imgAvatarChiTiet.setImageResource(R.mipmap.ic_launcher_round);
                }
            } else {
                Log.w(TAG, "Đường dẫn ảnh null hoặc rỗng cho sinh viên: " + sv.getHoTen());
                imgAvatarChiTiet.setImageResource(R.mipmap.ic_launcher_round); // Ảnh mặc định nếu không có đường dẫn
            }
        } else {
            Toast.makeText(this, "Không nhận được dữ liệu sinh viên.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Đối tượng SinhVien (sv) là null.");
        }
    }

    // Xử lý khi nhấn nút back trên ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // Thêm @NonNull
        if (item.getItemId() == android.R.id.home) {
            finish(); // Quay lại Activity trước đó
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
