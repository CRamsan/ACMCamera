<?php
$db = new SQLite3('acmcaemra.db');

$db->exec('CREATE TABLE IF NOT EXISTS photos (id INTEGER, contributor TEXT, date INTEGER)');

$stmt = $db->prepare('SELECT * FROM photos');

$result = $stmt->execute();
var_dump($result->fetchArray());
?>
