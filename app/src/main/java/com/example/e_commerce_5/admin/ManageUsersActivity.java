package com.example.e_commerce_5.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.e_commerce_5.LoginActivity;
import com.example.e_commerce_5.R;
import com.example.e_commerce_5.adapters.UserAdapter;
import com.example.e_commerce_5.models.User;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageUsersActivity extends AppCompatActivity {
    private static final String TAG = "ManageUsers";
    private static final String URL_GET_USERS = "http://10.0.2.2/e_commerce/user/get_users.php";
    private static final String URL_DELETE_USER = "http://10.0.2.2/e_commerce/user/delete_user.php";
    private static final String PREFS_NAME = "EcommercePrefs";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView recyclerViewUsers;
    private ProgressBar progressLoading;
    private List<User> userList;
    private UserAdapter userAdapter;
    private RequestQueue requestQueue;
    private String currentUserId = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = String.valueOf(prefs.getInt("user_id", -1));
        String currentRole = prefs.getString("role", "");

        // Validasi Role
        if (!"admin".equals(currentRole)) {
            Toast.makeText(this, "Akses ditolak. Silakan login sebagai admin.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Toolbar
        toolbar = findViewById(R.id.toolbar_manage_users);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.nav_admin_manage_users);
        }

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout_manage_users);
        navigationView = findViewById(R.id.navigation_view_manage_users);
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
                // Tetap di sini
            } else if (id == R.id.nav_logout) {
                // Hapus sesi
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        progressLoading = findViewById(R.id.progressLoading);

        userList = new ArrayList<>();
        // Asumsi UserAdapter memiliki konstruktor dan listener yang sesuai dengan deleteUser
        userAdapter = new UserAdapter(this, userList, userId -> deleteUser(userId));

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);

        requestQueue = Volley.newRequestQueue(this);
        fetchUsersFromApi();

        // Tombol back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
    }

    private void fetchUsersFromApi() {
        progressLoading.setVisibility(View.VISIBLE);
        // Tambahkan ID pengguna saat ini sebagai parameter (meskipun tidak diperlukan untuk get_users.php, kita tetap mengikutinya)
        String urlWithParam = URL_GET_USERS;
        Log.d(TAG, "fetching users with url: " + urlWithParam);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, urlWithParam, null,
                response -> {
                    progressLoading.setVisibility(View.GONE);
                    try {
                        if ("success".equalsIgnoreCase(response.optString("status"))) {
                            JSONArray arr = response.optJSONArray("users");
                            userList.clear();
                            if (arr != null) {
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject o = arr.getJSONObject(i);
                                    // Tambahkan pengguna ke list (menggunakan konstruktor User yang disimpulkan)
                                    userList.add(new User(
                                            o.optString("id"),
                                            o.optString("name"),
                                            o.optString("email"),
                                            o.optString("role")
                                    ));
                                }
                            }
                            userAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, response.optString("message", "Gagal memuat user"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parse error", e);
                        Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressLoading.setVisibility(View.GONE);
                    NetworkResponse nr = error.networkResponse;
                    if (nr != null && nr.data != null) {
                        String body = new String(nr.data);
                        Log.e(TAG, "Volley error: " + nr.statusCode + " - " + body);
                        Toast.makeText(this, "Server error: " + nr.statusCode, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Volley error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(req);
    }

    private void deleteUser(String userId) {
        progressLoading.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, URL_DELETE_USER,
                response -> {
                    progressLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "User berhasil dihapus", Toast.LENGTH_SHORT).show();
                    fetchUsersFromApi();
                },
                error -> {
                    progressLoading.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", userId);
                // Menambahkan admin_id sebagai validasi di sisi server (disimpulkan)
                params.put("admin_id", String.valueOf(currentUserId));
                return params;
            }
        };

        requestQueue.add(request);
    }
}