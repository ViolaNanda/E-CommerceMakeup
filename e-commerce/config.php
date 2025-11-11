<?php
$host = "localhost";
$user = "root";       // default user XAMPP
$pass = "";           // default password kosong
$db   = "e_commerce"; // nama database kamu

$conn = mysqli_connect($host, $user, $pass, $db);

if (!$conn) {
    die(json_encode([
        "status" => "error",
        "message" => "Koneksi database gagal: " . mysqli_connect_error()
    ]));
}
?>
