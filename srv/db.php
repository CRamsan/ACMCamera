<?php
$db = new SQLite3('acmcamera.db');

$db->exec('CREATE TABLE IF NOT EXISTS photos (uuid TEXT, contributor TEXT, date INTEGER)');

$stmt = $db->prepare('SELECT * FROM photos');

$result = $stmt->execute();
var_dump($result->fetchArray());
?>
