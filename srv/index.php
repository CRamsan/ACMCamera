<?php

$pass = "sigmobi";

if($_POST["password"] != $pass)
{
  echo "Not authorized";
  return;
}

if($_POST["name"] === null)
{
  $contributor = "ACM Member";
}else{
  $contributor = $_POST["name"];
}


$allowedExts = array("jpg", "jpeg", "gif", "png");
$extension = end(explode(".", $_FILES["file"]["name"]));
if ((($_FILES["file"]["type"] == "image/gif")
|| ($_FILES["file"]["type"] == "image/jpeg")
|| ($_FILES["file"]["type"] == "image/png")
|| ($_FILES["file"]["type"] == "image/pjpeg"))
&& ($_FILES["file"]["size"] < 2000000)
&& in_array($extension, $allowedExts))
{
  if ($_FILES["file"]["error"] > 0)
  {
    echo "Return Code: " . $_FILES["file"]["error"] . "<br />";
  }
  else
  {
    echo "Upload: " . $_FILES["file"]["name"] . "<br />";
    echo "Type: " . $_FILES["file"]["type"] . "<br />";
    echo "Size: " . ($_FILES["file"]["size"] / 1024) . " Kb<br />";
    echo "Temp file: " . $_FILES["file"]["tmp_name"] . "<br />";

    var_dump($_FILES);
    $uploadUUID = uniqid();
    $uploadedFile = "uploads/original/" . $uploadUUID;
    if (file_exists($uploadedFile))
    {
      echo $_FILES["file"]["name"] . " Collision detected, try uploading again.. ";
    }
    else
    {
      move_uploaded_file($_FILES["file"]["tmp_name"],$uploadedFile);
      echo "Stored in: " . $uploadedFile;
      $upload = new Imagick(glob($uploadedFile));
      $upload->thumbnailImage(200, 0);
      $upload->writeImage("uploads/" . $uploadUUID);

      $db = new SQLite3('acmcamera.db');
      $db->exec('CREATE TABLE IF NOT EXISTS photos (uuid TEXT, contributor TEXT, date INTEGER)');
      $db->exec("INSERT INTO photos (uuid,contributor,date) VALUES ('".$uploadUUID."', '".$contributor."',".time().")");
    }
  }
}
else
{
  echo "Invalid file";
}
?>
