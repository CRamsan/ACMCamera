<?php

$code = "12345";
$name = "";

if($_POST["code"] != $code)
{
	echo "Incorrect code";
	exit;
}

if (!isset($_POST["name"]) || $_POST["name"] == "" )
{
	$name = "anonymous";
}
else
{
	$name = $_POST["name"];
}

if (!isset($_FILES["file"]))
{
	echo "Nothing to upload";
	exit;
}

$allowedExts = array("jpg", "jpeg", "gif", "png", "zip", "rar", "tar", "gzip", "bzip");
$extension = end(explode(".", $_FILES["file"]["name"]));

if ( (($_FILES["file"]["type"] == "image/gif")
|| ($_FILES["file"]["type"] == "image/jpeg")
|| ($_FILES["file"]["type"] == "image/png")
|| ($_FILES["file"]["type"] == "image/pjpeg"))
&& in_array($extension, $allowedExts) )
{
	if( ($_FILES["file"]["size"] < 20000))
	{
		echo "File too big";
	}

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
		if (file_exists("uploads/original" . $_FILES["file"]["name"]))
		{
			echo $_FILES["file"]["name"] . " already exists. ";
		}
		else
		{
			move_uploaded_file($_FILES["file"]["tmp_name"], "uploads/original/" . $_FILES["file"]["name"]);
			echo "Stored in: " . "uploads/original/" . $_FILES["file"]["name"];
		}

		$orig_file = "uploads/original/" . $_FILES["file"]["name"];
		$thumb_file = "uploads/".$_FILES["file"]["name"];

		$thumb = new Imagick();
		$thumb->readImage($orig_file);
		$thumb->resizeImage(640,480,Imagick::FILTER_LANCZOS,1);
		$thumb->writeImage($thumb_file);
		$thumb->clear();
		$thumb->destroy();


		$index = "uploads/index.html";
		$fh = fopen($index, 'a') or die("can't open file");
	        $newLine = '<img src="$thumb_file" alt="random image" />'."<br /><br />";
		fwrite($fh, $newLine);
		fclose($fh);
	}
}
else
{
	echo "Invalid file";
}
?>
