<?php
header("Content-Type: application/json; charset=utf-8");

// Folder penyimpanan gambar
$targetDir = __DIR__ . "/../uploads/";

if (!is_dir($targetDir)) {
    mkdir($targetDir, 0777, true);
}

$response = [];

if (isset($_FILES['image'])) {
    $fileName = time() . "_" . basename($_FILES['image']['name']);
    $targetFilePath = $targetDir . $fileName;

    // cek ekstensi
    $fileType = strtolower(pathinfo($targetFilePath, PATHINFO_EXTENSION));
    $allowTypes = ['jpg', 'jpeg', 'png'];

    if (in_array($fileType, $allowTypes)) {
        if (move_uploaded_file($_FILES['image']['tmp_name'], $targetFilePath)) {
            $url = "http://10.0.2.2/e_commerce/uploads/" . $fileName;
            $response = [
                "status" => "success",
                "message" => "Upload berhasil",
                "image_url" => $url
            ];
        } else {
            $response = ["status" => "error", "message" => "Gagal upload file"];
        }
    } else {
        $response = ["status" => "error", "message" => "Format tidak valid (hanya JPG/PNG)"];
    }
} else {
    $response = ["status" => "error", "message" => "Tidak ada file dikirim"];
}

echo json_encode($response);

?>