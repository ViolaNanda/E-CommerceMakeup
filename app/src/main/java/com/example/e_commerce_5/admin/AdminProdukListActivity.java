package com.example.e_commerce_5.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
import com.example.e_commerce_5.adapters.AdminProductAdapter;
import com.example.e_commerce_5.models.Product;
import com.example.e_commerce_5.R;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminProductListActivity extends AppCompatActivity {
    private static final String TAG = "AdminProductList";
    private static final String URL_GET_PRODUCTS = "http://10.0.2.2/e_commerce/product/get_products.php";
    private static final String URL_DELETE_PRODUCT = "http://10.0.2.2/e_commerce/product/delete_product.php";

    private RecyclerView recyclerView;
    private ProgressBar progressLoading;
    private AdminProductAdapter adapter;
    private List<Product> productList;
    private RequestQueue requestQueue;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_list);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_admin_products);
        setSupportActionBar(toolbar);

        // DrawerLayout & NavigationView
        drawerLayout = findViewById(R.id.drawer_layout_admin_products);
        NavigationView navigationView = findViewById(R.id.navigation_view_admin_products);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_admin_product_list) {
                // Stay here
            } else if (id == R.id.nav_admin_add_product) {
                startActivity(new Intent(AdminProductListActivity.this, AddProductActivity.class));
            } else if (id == R.id.nav_admin_manage_users) {
                startActivity(new Intent(AdminProductListActivity.this, ManageUsersActivity.class));
            } else if (id == R.id.nav_logout) {
                // Logika logout (Hapus SharedPreferences)
                startActivity(new Intent(AdminProductListActivity.this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // RecyclerView & ProgressBar
        recyclerView = findViewById(R.id.recyclerViewAdminProducts);
        progressLoading = findViewById(R.id.progressLoading);

        productList = new ArrayList<>();
        // Asumsi AdminProductAdapter.OnProductActionListener sudah ada
        adapter = new AdminProductAdapter(this, productList, new AdminProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                AdminProductListActivity.this.onEdit(product);
            }

            @Override
            public void onDelete(Product product) {
                AdminProductListActivity.this.onDelete(product);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        // Handle back gesture modern
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

    public void onEdit(Product product) {
        Intent intent = new Intent(AdminProductListActivity.this, EditProductActivity.class);
        intent.putExtra("id", product.getId());
        intent.putExtra("name", product.getName());
        intent.putExtra("description", product.getDescription());
        intent.putExtra("price", product.getPrice());
        intent.putExtra("old_price", product.getOldPrice());
        intent.putExtra("category", product.getCategory());
        intent.putExtra("image_url", product.getImageUrl());
        startActivity(intent);
    }

    public void onDelete(Product product) {
        new AlertDialog.Builder(AdminProductListActivity.this)
                .setTitle("Hapus Produk")
                .setMessage("Apakah Anda yakin ingin menghapus \"" + product.getName() + "\" ?")
                .setPositiveButton("Ya", (dialog, which) -> deleteProduct(product.getId()))
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProductsFromApi(); // refresh saat kembali dari Edit/Add
    }

    private void fetchProductsFromApi() {
        progressLoading.setVisibility(android.view.View.VISIBLE);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL_GET_PRODUCTS, null,
                response -> {
                    progressLoading.setVisibility(android.view.View.GONE);
                    try {
                        if ("success".equalsIgnoreCase(response.optString("status", "fallback"))) {
                            JSONArray arr = response.optJSONArray("products");
                            productList.clear();
                            if (arr != null) {
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject o = arr.getJSONObject(i);
                                    String id = o.optString("id");
                                    String name = o.optString("name");
                                    String desc = o.optString("description");
                                    String price = o.optString("price");
                                    String oldPrice = o.optString("old_price");
                                    String imageUrl = o.optString("image_url");
                                    String category = o.optString("category");

                                    if (imageUrl.isEmpty() || imageUrl.startsWith("http")) {
                                        // URL sudah lengkap atau kosong
                                    } else {
                                        // Tambahkan base URL jika hanya path
                                        imageUrl = "http://10.0.2.2/e_commerce/" + imageUrl;
                                    }

                                    // Asumsi Product model sudah didefinisikan
                                    productList.add(new Product(id, name, desc, price, oldPrice, imageUrl, category));
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AdminProductListActivity.this, response.optString("message", "Gagal memuat produk"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "parse error", e);
                        Toast.makeText(AdminProductListActivity.this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressLoading.setVisibility(android.view.View.GONE);
                    NetworkResponse nr = error.networkResponse;
                    if (nr != null && nr.data != null) {
                        String body = new String(nr.data);
                        Log.e(TAG, "Volley error: " + nr.statusCode + " - " + body);
                        Toast.makeText(AdminProductListActivity.this, "Server error: " + nr.statusCode, Toast.LENGTH_LONG).show();
                    } else {
                        Log.e(TAG, "Volley error: " + error.getMessage());
                        Toast.makeText(AdminProductListActivity.this, "Volley error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(req);
    }

    private void deleteProduct(String productId) {
        progressLoading.setVisibility(android.view.View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, URL_DELETE_PRODUCT,
                response -> {
                    progressLoading.setVisibility(android.view.View.GONE);
                    Toast.makeText(AdminProductListActivity.this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show();
                    fetchProductsFromApi();
                },
                error -> {
                    progressLoading.setVisibility(android.view.View.GONE);
                    Toast.makeText(AdminProductListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", productId);
                return params;
            }
        };

        requestQueue.add(request);
    }
}