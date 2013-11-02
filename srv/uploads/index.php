<?php
$files = glob("*");
for ($i=0; $i<count($files); $i++)
{
	$num = $files[$i];
	if($num == "index.php" || $num == "index.html" || $num == "original")
	{
		continue;
	}
	print $num."<br />";
	echo '<img src="'.$num.'" alt="random image" />'."<br /><br />";
}
?>
