package com.example.e_commerce_5.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.e_commerce_5.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.example.e_commerce_5.R;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_admin_dashboard);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        // Drawer & Navigation
        drawerLayout = findViewById(R.id.drawer_layout_admin_dashboard);
        NavigationView navigationView = findViewById(R.id.navigation_view_admin_dashboard);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_product_list) {
                startActivity(new Intent(this, AdminProductListActivity.class));
                finish();
            } else if (id == R.id.nav_admin_add_product) {
                startActivity(new Intent(this, AddProductActivity.class));
                finish();
            } else if (id == R.id.nav_admin_manage_users) {
                startActivity(new Intent(this, ManageUsersActivity.class));
                finish();
            } else if (id == R.id.nav_logout) {
                // Hapus session
                getSharedPreferences("MyCartPrefs", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();

                // Pindah ke LoginActivity dengan clear history
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // OnBackPressedDispatcher untuk back gesture
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish(); // keluar activity jika drawer tertutup
                }
            }
        });
    }
}
