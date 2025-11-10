package com.example.e_commerce_5.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Disimpulkan

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.e_commerce_5.adapters.VolleyMultipartRequest;
import com.example.e_commerce_5.adapters.VolleyMultipartRequest.DataPart;
import com.example.e_commerce_5.R;
// Import lain yang diperlukan (diabaikan)

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {
    private static final String URL_UPDATE = "http://10.0.2.2/e_commerce/product/update_product.php";
    private static final String URL_UPLOAD = "http://10.0.2.2/e_commerce/product/upload_image.php";
    private static final String TAG = "EditProductActivity";

    private EditText etName, etDescription, etPrice, etOldPrice, etCategory;
    private Button btnSave, btnChooseImage;
    private ImageView imgPreview;
    private ProgressBar progressBar;
    private String productId;
    private Uri imageUri;
    private String uploadedImageUrl = "";

    // Menggunakan ActivityResultLauncher yang lebih modern
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
        setContentView(R.layout.activity_edit_product);

        // Inisialisasi Toolbar (Disimpulkan)
        Toolbar toolbar = findViewById(R.id.toolbar_edit_product);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.nav_edit_product);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etOldPrice = findViewById(R.id.etOldPrice);
        etCategory = findViewById(R.id.etCategory);
        btnSave = findViewById(R.id.btnSave);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        imgPreview = findViewById(R.id.imgPreview);
        progressBar = findViewById(R.id.progressBar);

        // Ambil data dari Intent
        Intent intent = getIntent();
        productId = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        String price = intent.getStringExtra("price");
        String oldPrice = intent.getStringExtra("old_price");
        String category = intent.getStringExtra("category");
        String imageUrl = intent.getStringExtra("image_url");

        // Set data ke field input
        etName.setText(name);
        etDescription.setText(description);
        etPrice.setText(price);
        etOldPrice.setText(oldPrice);
        etCategory.setText(category);
        uploadedImageUrl = imageUrl; // Simpan URL yang ada
        // Load gambar lama ke imgPreview menggunakan library seperti Glide/Picasso (jika digunakan)

        btnChooseImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> updateProduct());
    }

    // Menggantikan fungsi lama openFileChooser()
    private void uploadImageToServer(Uri uri) {
        progressBar.setVisibility(View.VISIBLE);
        try {
            InputStream iStream = getContentResolver().openInputStream(uri);
            if (iStream == null) {
                Toast.makeText(this, "Gagal membaca file", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            byte[] inputData = getBytes(iStream);

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, URL_UPLOAD, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    progressBar.setVisibility(View.GONE);
                    try {
                        String result = new String(response.data);
                        JSONObject obj = new JSONObject(result);
                        if ("success".equals(obj.getString("status"))) {
                            uploadedImageUrl = obj.getString("image_url");
                            Toast.makeText(EditProductActivity.this, "Upload Berhasil", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProductActivity.this, "Upload gagal: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "JSON Parse error", e);
                        Toast.makeText(EditProductActivity.this, "Response Parse Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    String msg = (error.getMessage() != null) ? error.getMessage() : "Terjadi kesalahan jaringan";
                    Toast.makeText(EditProductActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    params.put("image", new DataPart("file.jpg", inputData, "image/jpeg"));
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(multipartRequest);
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
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

    private void updateProduct() {
        final String name = etName.getText().toString().trim();
        final String description = etDescription.getText().toString().trim();
        final String price = etPrice.getText().toString().trim();
        final String oldPrice = etOldPrice.getText().toString().trim();
        final String category = etCategory.getText().toString().trim();
        final String imageUrl = uploadedImageUrl;

        if (name.isEmpty() || description.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Nama, Deskripsi, dan Harga wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, URL_UPDATE,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditProductActivity.this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    String msg = (error.getMessage() != null) ? error.getMessage() : "Terjadi kesalahan jaringan";
                    Toast.makeText(EditProductActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", productId);
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