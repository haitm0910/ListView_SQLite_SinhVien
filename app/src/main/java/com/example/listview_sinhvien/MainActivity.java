package com.example.listview_sinhvien;

import android.app.Activity;
import android.app.AlertDialog; // Import AlertDialog
import android.content.DialogInterface; // Import DialogInterface
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ListView listViewSinhVien;
    private Database databaseHelper;
    private ListAdapter sinhVienAdapter;
    private ArrayList<SinhVien> sinhVienList;

    private ImageButton btnAddSinhVien;

    // ActivityResultLauncher để xử lý kết quả từ addSinhVien Activity (cho cả thêm mới và sửa)
    private ActivityResultLauncher<Intent> addOrEditSinhVienLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Activity được tạo.");

        listViewSinhVien = findViewById(R.id.lv);
        btnAddSinhVien = findViewById(R.id.btnAddSinhVien);

        databaseHelper = new Database(this);
        sinhVienList = new ArrayList<>();

        sinhVienAdapter = new ListAdapter(this, R.layout.list_item, sinhVienList);
        listViewSinhVien.setAdapter(sinhVienAdapter);

        // Đăng ký ActivityResultLauncher cho việc thêm hoặc sửa sinh viên
        addOrEditSinhVienLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "ActivityResultLauncher: Nhận kết quả với resultCode = " + result.getResultCode());
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.i(TAG, "Nhận RESULT_OK, làm mới danh sách sinh viên.");
                        loadDataAndUpdateListView();
                    } else {
                        Log.w(TAG, "Kết quả không phải RESULT_OK hoặc bị hủy.");
                    }
                }
        );

        btnAddSinhVien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Nút Thêm Sinh Viên được nhấn.");
                Intent intent = new Intent(MainActivity.this, addSinhVien.class);
                // Khi thêm mới, không cần gửi ID
                addOrEditSinhVienLauncher.launch(intent);
            }
        });

        // Sự kiện click cho item trong ListView để hiển thị dialog Sửa/Xóa
        listViewSinhVien.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < sinhVienList.size()) {
                    SinhVien selectedSinhVien = sinhVienList.get(position);
                    Log.d(TAG, "Item clicked: " + selectedSinhVien.getHoTen());
                    showEditDeleteDialog(selectedSinhVien);
                } else {
                    Log.e(TAG, "Vị trí item không hợp lệ khi click: " + position);
                }
            }
        });

        Log.d(TAG, "onCreate: Gọi loadDataAndUpdateListView lần đầu.");
        loadDataAndUpdateListView();
    }

    // Hiển thị Dialog với lựa chọn Sửa hoặc Xóa
    private void showEditDeleteDialog(final SinhVien sinhVien) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn hành động cho: " + sinhVien.getHoTen());
        builder.setItems(new CharSequence[]{"Xem chi tiết", "Chỉnh sửa", "Xóa"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Xem chi tiết
                        Log.d(TAG, "Chọn Xem chi tiết cho: " + sinhVien.getHoTen());
                        Intent detailIntent = new Intent(MainActivity.this, ChiTiet.class);
                        detailIntent.putExtra("sinhvien_chitiet", sinhVien);
                        startActivity(detailIntent);
                        break;
                    case 1: // Chỉnh sửa
                        Log.d(TAG, "Chọn Chỉnh sửa cho: " + sinhVien.getHoTen());
                        Intent editIntent = new Intent(MainActivity.this, addSinhVien.class);
                        // Gửi ID của sinh viên cần sửa sang addSinhVien Activity
                        // addSinhVien Activity cần được thiết kế để nhận ID này và tải dữ liệu sinh viên lên form
                        editIntent.putExtra("STUDENT_ID_TO_EDIT", sinhVien.getId());
                        addOrEditSinhVienLauncher.launch(editIntent);
                        break;
                    case 2: // Xóa
                        Log.d(TAG, "Chọn Xóa cho: " + sinhVien.getHoTen());
                        showDeleteConfirmationDialog(sinhVien);
                        break;
                }
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.create().show();
    }

    // Hiển thị Dialog xác nhận xóa
    private void showDeleteConfirmationDialog(final SinhVien sinhVien) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sinh viên '" + sinhVien.getHoTen() + "' không?")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.i(TAG, "Xác nhận xóa sinh viên: " + sinhVien.getHoTen());
                        // Gọi phương thức xóa trong DatabaseHelper
                        // Giả sử Database.java có phương thức deleteSinhVien(int id)
                        databaseHelper.deleteSinhVien(sinhVien.getId());
                        Toast.makeText(MainActivity.this, "Đã xóa: " + sinhVien.getHoTen(), Toast.LENGTH_SHORT).show();
                        loadDataAndUpdateListView(); // Làm mới danh sách sau khi xóa
                    }
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert) // Icon cảnh báo
                .show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Gọi loadDataAndUpdateListView.");
        loadDataAndUpdateListView();
    }

    private void loadDataAndUpdateListView() {
        Log.d(TAG, "loadDataAndUpdateListView: Bắt đầu tải dữ liệu.");
        ArrayList<SinhVien> updatedList = databaseHelper.getAllSinhViens();
        Log.d(TAG, "loadDataAndUpdateListView: Lấy được " + updatedList.size() + " sinh viên từ database.");

        this.sinhVienList.clear();
        this.sinhVienList.addAll(updatedList);

        if (sinhVienAdapter != null) {
            sinhVienAdapter.updateData(updatedList);
            Log.d(TAG, "loadDataAndUpdateListView: Đã gọi updateData trên adapter.");
        } else {
            Log.w(TAG, "loadDataAndUpdateListView: Adapter là null, tạo mới adapter.");
            sinhVienAdapter = new ListAdapter(this, R.layout.list_item, updatedList);
            listViewSinhVien.setAdapter(sinhVienAdapter);
        }

        if (this.sinhVienList.isEmpty()) {
            // Chỉ hiển thị Toast nếu danh sách thực sự trống và adapter đã được thông báo
            // Tránh hiển thị Toast khi Activity mới khởi tạo và adapter chưa có dữ liệu
            if (sinhVienAdapter.getCount() == 0) { // Kiểm tra số lượng item trong adapter
                Toast.makeText(this, "Chưa có sinh viên nào. Hãy thêm mới!", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "loadDataAndUpdateListView: Danh sách trống.");
        }
    }
}
