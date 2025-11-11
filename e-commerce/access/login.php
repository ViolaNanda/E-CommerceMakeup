<?php
header("Content-Type: application/json");
include __DIR__ . '/config.php';

$email    = isset($_POST['email']) ? trim($_POST['email']) : '';
$password = isset($_POST['password']) ? trim($_POST['password']) : '';

if (empty($email) || empty($password)) {
    echo json_encode([
        "status" => "error",
        "message" => "Email dan password wajib diisi"
    ]);
    exit;
}

$stmt = $conn->prepare("SELECT id, name, email, role FROM users WHERE email = ? AND password = ?");
$stmt->bind_param("ss", $email, $password);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    echo json_encode([
        "status" => "success",
        "message" => "Login berhasil",
        "user" => [
            "id"    => $row['id'],      // kirim user_id
            "name"  => $row['name'],
            "email" => $row['email'],
            "role"  => $row['role']
        ]
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Email atau password salah"
    ]);
}

$stmt->close();
$conn->close();
?>
