<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

include './config.php';

$name     = $_POST['name'] ?? '';
$email    = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$phone    = $_POST['phone'] ?? '';
$address  = $_POST['address'] ?? '';

if (empty($name) || empty($email) || empty($password)) {
    echo json_encode([
        "status" => "error",
        "message" => "Semua field wajib diisi"
    ]);
    exit;
}

// cek apakah email sudah terdaftar
$check = $conn->prepare("SELECT id FROM users WHERE email = ?");
$check->bind_param("s", $email);
$check->execute();
$result = $check->get_result();

if ($result->num_rows > 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Email sudah terdaftar"
    ]);
    exit;
}

// simpan user baru
$stmt = $conn->prepare("INSERT INTO users (name, email, password, phone, address) VALUES (?, ?, ?, ?, ?)");
$stmt->bind_param("sssss", $name, $email, $password, $phone, $address);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Registrasi berhasil"
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Gagal menyimpan data"
    ]);
}
?>
