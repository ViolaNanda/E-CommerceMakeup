package com.example.e_commerce_5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_commerce_5.admin.AdminDashboardActivity;
import com.example.e_commerce_5.user.UserDashboardActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnRegister, btnAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        // Tombol login user
        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show();
            } else {
                new Thread(() -> loginUser(email, password, false)).start();
            }
        });

        // Tombol register
        btnRegister.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Tombol login admin
        btnAdminLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show();
            } else {
                new Thread(() -> loginUser(email, password, true)).start();
            }
        });
    }

    private void loginUser(String email, String password, boolean forceAdmin) {
        try {
            URL url = new URL("http://10.0.2.2/e_commerce/access/login.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String data = "email=" + URLEncoder.encode(email, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            runOnUiThread(() -> {
                try {
                    JSONObject json = new JSONObject(response.toString());
                    if ("success".equals(json.getString("status"))) {
                        String role = json.optString("role", "user");
                        String name = json.optString("name", "User");
                        String userId = json.optString("user_id", "");

                        // Jika user pakai tombol admin tapi bukan admin
                        if (forceAdmin && !"admin".equals(role)) {
                            Toast.makeText(LoginActivity.this, "Akun ini bukan admin!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Simpan session sementara (dihapus saat app ditutup)
                        SharedPreferences prefs = getSharedPreferences("ECommercePrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("user_id", userId);
                        editor.putString("user_name", name);
                        editor.putString("role", role);
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Login berhasil: " + name, Toast.LENGTH_SHORT).show();

                        // Redirect sesuai role
                        if ("admin".equals(role)) {
                            Intent i = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                            i.putExtra("admin_name", name);
                            startActivity(i);
                        } else {
                            Intent i = new Intent(LoginActivity.this, UserDashboardActivity.class);
                            i.putExtra("user_name", name);
                            startActivity(i);
                        }

                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, json.optString("message", "Login gagal"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "Response error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            runOnUiThread(() ->
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hapus session ketika aplikasi ditutup
        SharedPreferences prefs = getSharedPreferences("ECommercePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}
