<?php
header("Content-Type: application/json; charset=utf-8");
include __DIR__ . "/../config.php";

// Debug: log semua data POST ke file
file_put_contents(__DIR__ . "/debug_delete.log", date("Y-m-d H:i:s") . " -> " . print_r($_POST, true) . "\n", FILE_APPEND);

$id = $_POST['id'] ?? '';

if (!$id) {
    echo json_encode([
        "status" => "error",
        "message" => "ID produk tidak ada",
        "debug" => $_POST // tampilkan isi POST juga
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// cek apakah id ada di tabel
$check = $conn->prepare("SELECT id FROM products WHERE id=? LIMIT 1");
$check->bind_param("i", $id);
$check->execute();
$check->store_result();

if ($check->num_rows === 0) {
    echo json_encode([
        "status" => "error",
        "message" => "Produk dengan ID $id tidak ditemukan",
        "debug" => $_POST
    ], JSON_UNESCAPED_UNICODE);
    $check->close();
    $conn->close();
    exit;
}

$check->close();

// hapus record
$sql = "DELETE FROM products WHERE id=?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $id);

if ($stmt->execute()) {
    echo json_encode([
        "status" => "success",
        "message" => "Produk berhasil dihapus",
        "deleted_id" => $id
    ], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Gagal menghapus produk",
        "error" => $stmt->error
    ], JSON_UNESCAPED_UNICODE);
}

$stmt->close();
$conn->close();
?>