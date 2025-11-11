<?php
header("Content-Type: application/json; charset=utf-8");
include __DIR__ . "/../config.php"; // Pastikan config.php ada dan koneksi $conn berhasil

$response = [
    "status" => "error",
    "products" => [],
    "message" => ""
];

$sql = "SELECT id, name, description, price, old_price, image_url, category
        FROM products
        ORDER BY id DESC";

$result = $conn->query($sql);

if ($result) {
    if ($result->num_rows > 0) {
        $products = [];
        while ($row = $result->fetch_assoc()) {
            // pastikan format harga string agar aman di JSON
            $row['price'] = (string)$row['price'];
            $row['old_price'] = (string)$row['old_price'];
            $products[] = $row;
        }
        $response['status'] = "success";
        $response['products'] = $products;
    } else {
        $response["message"] = "Tidak ada produk ditemukan";
    }
} else {
    $response["message"] = "Query error: " . $conn->error;
}

echo json_encode($response, JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT);

$conn->close();
?>