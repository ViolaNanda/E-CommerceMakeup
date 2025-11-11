<?php
header("Content-Type: application/json; charset=utf-8");
include __DIR__ . "/../config.php";

// Ambil data dari request
$name = $_POST['name'] ?? '';
$description = $_POST['description'] ?? '';
$price = $_POST['price'] ?? '';
$old_price = $_POST['old_price'] ?? '';
$category = $_POST['category'] ?? '';
$image_url = $_POST['image_url'] ?? ''; // opsional

// Validasi input wajib
if (empty($name) || empty($price) || empty($category)) {
    echo json_encode([
        "status" => "error",
        "message" => "Data tidak lengkap. Pastikan Nama, Harga, dan Kategori terisi."
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Siapkan query Insert
$sql = "INSERT INTO products (name, description, price, old_price, category, image_url)
        VALUES (?, ?, ?, ?, ?, ?)";

$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode([
        "status" => "error",
        "message" => "Prepare failed: " . $conn->error
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Binding parameter
$stmt->bind_param("ssssss", $name, $description, $price, $old_price, $category, $image_url);

// Eksekusi query
if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Produk berhasil ditambahkan",
        "id" => $stmt->insert_id
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Gagal menambahkan produk: " . $stmt->error
    ], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>