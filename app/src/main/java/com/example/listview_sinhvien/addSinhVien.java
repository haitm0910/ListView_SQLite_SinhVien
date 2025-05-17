package com.example.listview_sinhvien;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class addSinhVien extends AppCompatActivity {

    private static final String TAG = "AddEditSinhVienActivity"; // Đổi tên TAG cho rõ ràng

    private ImageView imgAvatar;
    private TextView tvChooseAvatar;
    private EditText edtHoTen, edtMssv, edtChuyenNganh, edtNgaySinh, edtKhoaHoc;
    private Button btnLuu; // Sẽ đổi text thành "Cập nhật" nếu ở chế độ sửa

    private String currentAvatarPath = null; // Lưu đường dẫn file ảnh hiện tại (mới chọn hoặc từ sinh viên đang sửa)
    private ActivityResultLauncher<Intent> pickImageLauncher;

    private Database databaseHelper;
    private boolean isEditMode = false; // Cờ xác định chế độ: true = sửa, false = thêm mới
    private int studentIdToEdit = -1; // ID của sinh viên cần sửa, -1 nếu là thêm mới
    private SinhVien currentEditingStudent = null; // Giữ tham chiếu đến đối tượng sinh viên đang được chỉnh sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sinh_vien); // Đảm bảo layout này tồn tại

        // Xử lý WindowInsets nếu bạn dùng EdgeToEdge
        // Đảm bảo R.id.main là ID của layout gốc trong activity_add_sinh_vien.xml
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Tiêu đề sẽ được đặt lại dựa trên chế độ (thêm/sửa)
        }

        // Ánh xạ Views
        imgAvatar = findViewById(R.id.imgAvatar);
        tvChooseAvatar = findViewById(R.id.tvChooseAvatar);
        edtHoTen = findViewById(R.id.edtHoTen);
        edtMssv = findViewById(R.id.edtMssv);
        edtChuyenNganh = findViewById(R.id.edtChuyenNganh);
        edtNgaySinh = findViewById(R.id.edtNgaySinh);
        edtKhoaHoc = findViewById(R.id.edtKhoaHoc);
        btnLuu = findViewById(R.id.btnLuu);

        databaseHelper = new Database(this);

        // Khởi tạo ActivityResultLauncher để chọn ảnh
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                // Lưu ảnh mới vào bộ nhớ trong và cập nhật currentAvatarPath
                                currentAvatarPath = saveImageToInternalStorage(imageUri);

                                // Hiển thị ảnh đã chọn
                                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                                imgAvatar.setImageBitmap(selectedImage);
                                if (imageStream != null) imageStream.close();

                                imgAvatar.setVisibility(View.VISIBLE);
                                tvChooseAvatar.setText("Đổi ảnh");
                                Log.i(TAG, "Ảnh đã được chọn và hiển thị. Đường dẫn mới: " + currentAvatarPath);

                            } catch (IOException e) {
                                Log.e(TAG, "Lỗi khi xử lý hoặc lưu ảnh mới: " + e.getMessage(), e);
                                Toast.makeText(this, "Không thể tải hoặc lưu ảnh mới", Toast.LENGTH_SHORT).show();
                                // Nếu đang sửa và có lỗi khi chọn ảnh mới, khôi phục path ảnh cũ (nếu có)
                                if (isEditMode && currentEditingStudent != null) {
                                    currentAvatarPath = currentEditingStudent.getAvatarPath();
                                } else {
                                    currentAvatarPath = null;
                                }
                            }
                        }
                    }
                }
        );

        // Sự kiện click để chọn ảnh
        View.OnClickListener chooseImageListener = v -> openImageChooser();
        tvChooseAvatar.setOnClickListener(chooseImageListener);
        imgAvatar.setOnClickListener(chooseImageListener);

        // Kiểm tra Intent để xác định chế độ Thêm mới hay Chỉnh sửa
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("STUDENT_ID_TO_EDIT")) {
            studentIdToEdit = intent.getIntExtra("STUDENT_ID_TO_EDIT", -1);
            if (studentIdToEdit != -1) {
                isEditMode = true;
                if (actionBar != null) actionBar.setTitle("Sửa Thông Tin Sinh Viên");
                btnLuu.setText("Cập nhật");
                loadStudentDataForEditing(studentIdToEdit); // Tải dữ liệu sinh viên lên form
            } else {
                // Trường hợp STUDENT_ID_TO_EDIT là -1 (không hợp lệ), coi như thêm mới
                isEditMode = false;
                if (actionBar != null) actionBar.setTitle("Thêm Sinh Viên Mới");
                btnLuu.setText("Lưu");
            }
        } else {
            // Không có STUDENT_ID_TO_EDIT -> chế độ thêm mới
            isEditMode = false;
            if (actionBar != null) actionBar.setTitle("Thêm Sinh Viên Mới");
            btnLuu.setText("Lưu");
        }

        // Sự kiện click cho nút Lưu/Cập nhật
        btnLuu.setOnClickListener(v -> saveOrUpdateStudent());
    }

    // Tải dữ liệu của sinh viên lên form khi ở chế độ chỉnh sửa
    private void loadStudentDataForEditing(int studentId) {
        Log.d(TAG, "Đang tải dữ liệu để sửa cho sinh viên ID: " + studentId);
        currentEditingStudent = databaseHelper.getStudentById(studentId); // Cần có phương thức này trong Database.java
        if (currentEditingStudent != null) {
            edtHoTen.setText(currentEditingStudent.getHoTen());
            edtMssv.setText(currentEditingStudent.getMssv());
            // edtMssv.setEnabled(false); // Tùy chọn: không cho phép sửa MSSV
            edtChuyenNganh.setText(currentEditingStudent.getChuyenNganh());
            edtNgaySinh.setText(currentEditingStudent.getNgaySinh());
            edtKhoaHoc.setText(currentEditingStudent.getKhoaHoc());

            currentAvatarPath = currentEditingStudent.getAvatarPath(); // Lấy đường dẫn ảnh hiện tại của sinh viên

            if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
                File imgFile = new File(currentAvatarPath);
                if (imgFile.exists() && imgFile.isFile()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imgAvatar.setImageBitmap(myBitmap);
                    imgAvatar.setVisibility(View.VISIBLE);
                    tvChooseAvatar.setText("Đổi ảnh");
                } else {
                    Log.w(TAG, "File ảnh không tồn tại để sửa: " + currentAvatarPath);
                    imgAvatar.setImageResource(R.mipmap.ic_launcher_round); // Ảnh mặc định
                    imgAvatar.setVisibility(View.VISIBLE);
                    tvChooseAvatar.setText("Chọn ảnh");
                }
            } else {
                imgAvatar.setImageResource(R.mipmap.ic_launcher_round); // Ảnh mặc định
                imgAvatar.setVisibility(View.VISIBLE);
                tvChooseAvatar.setText("Chọn ảnh");
            }
            Log.i(TAG, "Đã tải dữ liệu để sửa cho sinh viên: " + currentEditingStudent.getHoTen());
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin sinh viên để sửa.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Không tìm thấy sinh viên với ID: " + studentId + " để sửa.");
            finish(); // Đóng activity nếu không tìm thấy sinh viên
        }
    }

    // Xử lý việc lưu sinh viên mới hoặc cập nhật sinh viên hiện tại
    private void saveOrUpdateStudent() {
        String hoTen = edtHoTen.getText().toString().trim();
        String mssv = edtMssv.getText().toString().trim();
        String chuyenNganh = edtChuyenNganh.getText().toString().trim();
        String ngaySinh = edtNgaySinh.getText().toString().trim();
        String khoaHoc = edtKhoaHoc.getText().toString().trim();

        if (TextUtils.isEmpty(hoTen) || TextUtils.isEmpty(mssv)) {
            Toast.makeText(this, "Vui lòng nhập Họ tên và MSSV.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode && currentEditingStudent != null) {
            // CHẾ ĐỘ CẬP NHẬT
            currentEditingStudent.setHoTen(hoTen);
            currentEditingStudent.setMssv(mssv); // Nếu cho phép sửa MSSV
            currentEditingStudent.setChuyenNganh(chuyenNganh);
            currentEditingStudent.setNgaySinh(ngaySinh);
            currentEditingStudent.setKhoaHoc(khoaHoc);
            currentEditingStudent.setAvatarPath(currentAvatarPath); // currentAvatarPath đã được cập nhật nếu người dùng chọn ảnh mới

            int rowsAffected = databaseHelper.updateSinhVien(currentEditingStudent); // Cần có phương thức này trong Database.java
            if (rowsAffected > 0) {
                Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK); // Báo cho MainActivity biết để làm mới danh sách
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật thông tin. MSSV có thể bị trùng hoặc không có thay đổi.", Toast.LENGTH_LONG).show();
            }
        } else {
            // CHẾ ĐỘ THÊM MỚI
            SinhVien sv = new SinhVien(0, hoTen, mssv, currentAvatarPath, chuyenNganh, ngaySinh, khoaHoc);
            long newRowId = databaseHelper.addSinhVien(sv);
            if (newRowId != -1) {
                Toast.makeText(this, "Đã thêm sinh viên thành công!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK); // Báo cho MainActivity biết để làm mới danh sách
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi thêm sinh viên. MSSV có thể đã tồn tại.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Mở trình chọn ảnh của hệ thống
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*"); // Chỉ cho phép chọn các loại file ảnh
        pickImageLauncher.launch(intent);
    }

    // Lưu ảnh được chọn vào bộ nhớ trong của ứng dụng
    private String saveImageToInternalStorage(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Không thể mở InputStream cho Uri: " + uri);
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime());
        String fileName = "AVATAR_" + timeStamp + ".jpg";
        File internalStorageDir = getFilesDir(); // Thư mục private với ứng dụng của bạn
        File imageFile = new File(internalStorageDir, fileName);

        try (OutputStream outputStream = new FileOutputStream(imageFile)) {
            byte[] buffer = new byte[1024 * 4]; // Buffer 4KB
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            inputStream.close();
        }
        Log.i(TAG, "Ảnh đã được lưu vào bộ nhớ trong tại: " + imageFile.getAbsolutePath());
        return imageFile.getAbsolutePath();
    }

    // Xử lý khi nhấn nút back trên ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Hoặc onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
