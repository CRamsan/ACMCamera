<?php
$db = new SQLite3('acmcamera.db');

$db->exec('CREATE TABLE IF NOT EXISTS photos (uuid TEXT, contributor TEXT, date INTEGER)');

$stmt = $db->prepare('SELECT * FROM photos');

$result = $stmt->execute();
header('Content-Type:application/json');
echo "{\"photos\":[";
$first = TRUE;
while($res = $result->fetchArray())
{
  if(!isset($res['uuid']))
  {
    continue;
  }
  if($first)
  {
    $first=FALSE;
  }
  else
  {
    echo ",";
  }
  echo "{\"uuid\":\"".$res['uuid']."\",";
  echo "\"contributor\":\"".$res['contributor']."\",";
  echo "\"uploaded\":\"".$res['date']."\"}";
}
echo "]}";
?>
