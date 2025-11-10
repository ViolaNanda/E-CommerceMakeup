package com.example.e_commerce_5;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RegisterActivity extends AppCompatActivity {

    EditText etRegName, etRegEmail, etRegPassword, etRegConfirm, etRegPhone, etRegAddress;
    Button btnDaftar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegName = findViewById(R.id.etRegName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirm = findViewById(R.id.etRegConfirm);
        etRegPhone = findViewById(R.id.etRegPhone);
        etRegAddress = findViewById(R.id.etRegAddress);
        btnDaftar = findViewById(R.id.btnDaftar);

        btnDaftar.setOnClickListener(view -> {
            String name = etRegName.getText().toString().trim();
            String email = etRegEmail.getText().toString().trim();
            String password = etRegPassword.getText().toString().trim();
            String confirm = etRegConfirm.getText().toString().trim();
            String phone = etRegPhone.getText().toString().trim();
            String address = etRegAddress.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm) ||
                    TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {

                Toast.makeText(RegisterActivity.this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirm)) {
                Toast.makeText(RegisterActivity.this, "Password tidak sama", Toast.LENGTH_SHORT).show();
            } else {
                new Thread(() -> registerUser(name, email, password, phone, address)).start();
            }
        });
    }

    private void registerUser(String name, String email, String password, String phone, String address) {
        HttpURLConnection conn = null;
        try {
            // Gunakan 10.0.2.2 jika pakai emulator Android Studio
            URL url = new URL("http://10.0.2.2/e_commerce/access/register.php");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            String data = "name=" + URLEncoder.encode(name, StandardCharsets.UTF_8.name()) +
                    "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8.name()) +
                    "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8.name()) +
                    "&phone=" + URLEncoder.encode(phone, StandardCharsets.UTF_8.name()) +
                    "&address=" + URLEncoder.encode(address, StandardCharsets.UTF_8.name());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }

            String response = sb.toString().trim();

            runOnUiThread(() -> {
                try {
                    JSONObject json = new JSONObject(response);
                    String status = json.optString("status", "error");
                    String message = json.optString("message", "Terjadi kesalahan");

                    if ("success".equalsIgnoreCase(status)) {
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish(); // Tutup activity setelah sukses
                    } else {
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(RegisterActivity.this, "Response server tidak valid: " + response, Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            runOnUiThread(() ->
                    Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
