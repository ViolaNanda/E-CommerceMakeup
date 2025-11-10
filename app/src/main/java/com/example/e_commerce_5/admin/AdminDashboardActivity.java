package com.example.e_commerce_5.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.e_commerce.R;
// Import lain yang diperlukan (tidak terlihat, diabaikan)

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Contoh inisialisasi tombol dan listener (disimpulkan dari tujuan aplikasi)
        Button btnProductList = findViewById(R.id.btnProductList);
        Button btnAddProduct = findViewById(R.id.btnAddProduct);
        Button btnManageUsers = findViewById(R.id.btnManageUsers);

        btnProductList.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminProductListActivity.class));
        });

        btnAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AddProductActivity.class));
        });

        btnManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageUsersActivity.class));
        });

        // Logika tambahan untuk Toolbar/Drawer (jika ada, diabaikan karena tidak terlihat)
    }
}