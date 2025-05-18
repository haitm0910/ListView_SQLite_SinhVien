package com.example.listview_sinhvien;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<SinhVien> {

    private static final String TAG = "ListAdapter"; // Tag cho Log
    Activity context;
    int idLayoutResource; // Đổi tên từ idlayout cho rõ nghĩa hơn
    ArrayList<SinhVien> sinhVienList; // Đổi tên từ mylist cho rõ nghĩa hơn

    public ListAdapter(Activity context, int idLayoutResource, ArrayList<SinhVien> sinhVienList) {
        super(context, idLayoutResource, sinhVienList);
        this.context = context;
        this.idLayoutResource = idLayoutResource;
        this.sinhVienList = sinhVienList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // Nếu convertView chưa được tạo, inflate layout và tạo ViewHolder mới
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(idLayoutResource, parent, false); // Gắn vào parent và không attachToRoot ngay

            viewHolder = new ViewHolder();
            // Đảm bảo các ID này khớp với ID trong file XML layout item của bạn (ví dụ: list_item.xml)
            viewHolder.imgAvatar = convertView.findViewById(R.id.imgAvatar);
            viewHolder.tvHoTen = convertView.findViewById(R.id.tvHoTen);
            viewHolder.tvMssv = convertView.findViewById(R.id.tvMssv);

            convertView.setTag(viewHolder); // Lưu ViewHolder vào tag của View
        } else {
            // Nếu convertView đã tồn tại, lấy ViewHolder từ tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Lấy đối tượng SinhVien tại vị trí hiện tại
        // Kiểm tra xem danh sách có rỗng hoặc position có hợp lệ không để tránh IndexOutOfBoundsException
        if (sinhVienList != null && position < sinhVienList.size()) {
            SinhVien sinhVien = sinhVienList.get(position);

            if (sinhVien != null) {
                // Hiển thị thông tin văn bản
                viewHolder.tvHoTen.setText(sinhVien.getHoTen() != null ? sinhVien.getHoTen() : "N/A");
                viewHolder.tvMssv.setText(sinhVien.getMssv() != null ? "MSSV: " + sinhVien.getMssv() : "N/A");

                // Lấy đường dẫn ảnh và hiển thị ảnh
                String avatarPath = sinhVien.getAvatarPath(); // <<=== ĐÃ THAY ĐỔI: sử dụng getAvatarPath()
                if (avatarPath != null && !avatarPath.isEmpty()) {
                    File imgFile = new File(avatarPath);
                    if (imgFile.exists() && imgFile.isFile()) {
                        try {
                            // Cân nhắc việc resize ảnh ở đây nếu ảnh quá lớn để tránh OutOfMemoryError
                            // Ví dụ: sử dụng BitmapFactory.Options
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            if (myBitmap != null) {
                                viewHolder.imgAvatar.setImageBitmap(myBitmap); // <<=== ĐÃ THAY ĐỔI: dùng setImageBitmap
                            } else {
                                Log.e(TAG, "Không thể giải mã ảnh từ: " + avatarPath + " cho sinh viên: " + sinhVien.getHoTen());
                                viewHolder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round); // Ảnh mặc định
                            }
                        } catch (OutOfMemoryError e) {
                            Log.e(TAG, "OutOfMemoryError khi tải ảnh cho ListView: " + avatarPath + " cho sinh viên: " + sinhVien.getHoTen(), e);
                            viewHolder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi không xác định khi tải ảnh cho ListView: " + avatarPath + " cho sinh viên: " + sinhVien.getHoTen(), e);
                            viewHolder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
                        }
                    } else {
                        Log.w(TAG, "File ảnh không tồn tại hoặc không phải file: " + avatarPath + " cho sinh viên: " + sinhVien.getHoTen());
                        viewHolder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
                    }
                } else {
                    Log.w(TAG, "Đường dẫn ảnh null hoặc rỗng cho sinh viên: " + sinhVien.getHoTen());
                    viewHolder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round); // Ảnh mặc định
                }
            } else {
                // Xử lý trường hợp sinhVien object là null (dù ít khi xảy ra nếu danh sách được quản lý tốt)
                Log.w(TAG, "Đối tượng SinhVien tại vị trí " + position + " là null.");
                viewHolder.tvHoTen.setText("N/A");
                viewHolder.tvMssv.setText("N/A");
                viewHolder.imgAvatar.setImageResource(R.mipmap.ic_launcher_round);
            }
        } else {
            // Xử lý trường hợp danh sách rỗng hoặc position không hợp lệ
            Log.e(TAG, "Danh sách sinh viên rỗng hoặc vị trí không hợp lệ: " + position);
            // Có thể ẩn view hoặc hiển thị một thông báo lỗi trên item view nếu cần
        }
        return convertView;
    }

    // Lớp ViewHolder để giữ các tham chiếu đến View, giúp tối ưu ListView
    private static class ViewHolder {
        ImageView imgAvatar;
        TextView tvHoTen;
        TextView tvMssv;
    }

    // Phương thức để cập nhật dữ liệu cho adapter
    public void updateData(ArrayList<SinhVien> newSinhVienList) {

        super.clear(); // Xóa các item hiện tại của ArrayAdapter

        // Thêm tất cả dữ liệu mới vào
        if (newSinhVienList != null) {
            // this.sinhVienList.addAll(newSinhVienList);
            super.addAll(newSinhVienList); // Thêm các item mới vào ArrayAdapter
        }
        // Thông báo cho ListView rằng dữ liệu đã thay đổi và cần vẽ lại
        // notifyDataSetChanged(); // super.addAll() và super.clear() đã gọi notifyDataSetChanged()
        Log.d(TAG, "updateData: Dữ liệu đã được cập nhật, số lượng mới: " + (newSinhVienList != null ? newSinhVienList.size() : 0));
    }
}
