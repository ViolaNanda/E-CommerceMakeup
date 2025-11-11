<?php
header("Content-Type: application/json; charset=utf-8");
include __DIR__ . "/../config.php";

// Ambil data dari request
$id = $_POST['id'] ?? '';
$name = $_POST['name'] ?? '';
$description = $_POST['description'] ?? '';
$price = $_POST['price'] ?? '';
$old_price = $_POST['old_price'] ?? '';
$category = $_POST['category'] ?? '';
$image_url = $_POST['image_url'] ?? ''; // opsional

// Validasi input wajib
if (empty($id) || empty($name) || empty($price) || empty($category)) {
    echo json_encode([
        "status" => "error",
        "message" => "Data tidak lengkap. Pastikan ID, Nama, Harga, dan Kategori terisi."
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Siapkan query update
$sql = "UPDATE products
        SET name=?, description=?, price=?, old_price=?, category=?, image_url=?
        WHERE id=?";

$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode([
        "status" => "error",
        "message" => "Prepare failed: " . $conn->error
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// Binding parameter
$stmt->bind_param("ssssssi", $name, $description, $price, $old_price, $category, $image_url, $id);

// Eksekusi query
if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode([
            "status" => "success",
            "message" => "Produk berhasil diupdate"
        ], JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Tidak ada perubahan atau ID tidak ditemukan"
        ], JSON_UNESCAPED_UNICODE);
    }
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Gagal update produk: " . $stmt->error
    ], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>