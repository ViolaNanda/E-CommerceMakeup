package com.example.e_commerce_5.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.e_commerce_5.LoginActivity;
import com.example.e_commerce_5.adapters.VolleyMultipartRequest;
import com.example.e_commerce_5.adapters.VolleyMultipartRequest.DataPart;
import com.example.e_commerce_5.R;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private EditText etName, etDescription, etPrice, etOldPrice, etCategory, etImageUrl;
    private Button btnAddProduct, btnChooseImage;
    private ImageView imgPreview;
    private ProgressBar progressBar;
    private Uri imageUri;
    private String uploadedImageUrl = "";

    public static final String URL_ADD = "http://10.0.2.2/e_commerce/product/add_product.php";
    public static final String URL_UPLOAD = "http://10.0.2.2/e_commerce/product/upload_image.php";
    private static final String TAG = "AddProductActivity";


    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            imageUri = uri;
            imgPreview.setImageURI(imageUri);
            uploadImageToServer(imageUri);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // --- INIT NAVIGATION DRAWER ---
        drawerLayout = findViewById(R.id.drawer_layout_add_product);
        navigationView = findViewById(R.id.navigation_view_add_product);
        toolbar = findViewById(R.id.toolbar_add_product);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // --- INIT FORM ---
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etOldPrice = findViewById(R.id.etOldPrice);
        etCategory = findViewById(R.id.etCategory);
        etImageUrl = findViewById(R.id.etImageUrl);

        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        imgPreview = findViewById(R.id.imgPreview);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(android.view.View.GONE);

        btnChooseImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnAddProduct.setOnClickListener(v -> addProduct());
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_admin_product_list) {
            startActivity(new Intent(getApplicationContext(), AdminProductListActivity.class));
            finish();
        } else if (id == R.id.nav_admin_add_product) {
            // Stay here
        } else if (id == R.id.nav_admin_manage_users) {
            startActivity(new Intent(this, ManageUsersActivity.class));
            finish();
        } else if (id == R.id.nav_logout) {
            // Logika logout (Hapus SharedPreferences)
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void uploadImageToServer(Uri uri) {
        progressBar.setVisibility(android.view.View.VISIBLE);
        try {
            InputStream iStream = getContentResolver().openInputStream(uri);
            if (iStream == null) {
                Toast.makeText(this, "Gagal membaca file", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(android.view.View.GONE);
                return;
            }
            byte[] inputData = getBytes(iStream);

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, URL_UPLOAD, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    progressBar.setVisibility(android.view.View.GONE);
                    try {
                        String result = new String(response.data);
                        JSONObject obj = new JSONObject(result);
                        if ("success".equals(obj.getString("status"))) {
                            uploadedImageUrl = obj.getString("image_url");
                            Toast.makeText(AddProductActivity.this, "Upload Berhasil", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddProductActivity.this, "Upload gagal: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "JSON Parse error", e);
                        Toast.makeText(AddProductActivity.this, "Response Parse Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(android.view.View.GONE);
                    String msg = (error.getMessage() != null) ? error.getMessage() : "Terjadi kesalahan jaringan";
                    Toast.makeText(AddProductActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    // file name, data, mime_type
                    params.put("image", new DataPart("file.jpg", inputData, "image/jpeg"));
                    return params;
                }
            };
            Volley.newRequestQueue(this).add(multipartRequest);
        } catch (Exception e) {
            progressBar.setVisibility(android.view.View.GONE);
            Log.e(TAG, "Upload error", e);
            Toast.makeText(this, "Error selecting/uploading image", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void addProduct() {
        final String name = etName.getText().toString().trim();
        final String description = etDescription.getText().toString().trim();
        final String price = etPrice.getText().toString().trim();
        final String oldPrice = etOldPrice.getText().toString().trim();
        final String category = etCategory.getText().toString().trim();
        final String imageUrl = uploadedImageUrl.isEmpty() ? etImageUrl.getText().toString().trim() : uploadedImageUrl;

        if (name.isEmpty() || price.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Nama, Harga dan Kategori wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);
        btnAddProduct.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.POST, URL_ADD,
                response -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnAddProduct.setEnabled(true);
                    Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnAddProduct.setEnabled(true);
                    String msg = (error.getMessage() != null) ? error.getMessage() : "Terjadi kesalahan jaringan";
                    Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("description", description);
                params.put("price", price);
                params.put("old_price", oldPrice);
                params.put("category", category);
                params.put("image_url", imageUrl);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}