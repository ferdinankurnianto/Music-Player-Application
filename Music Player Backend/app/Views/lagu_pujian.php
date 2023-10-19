<?php
	$json = json_encode($laguPujian);
	echo json_encode($laguPujian);
	echo nl2br("\n\n");
	echo $laguPujian[0]['file_url'];
	echo nl2br("\n\n");
	echo json_encode($laguPujian[0]['file_url']);
	echo nl2br("\n\n");
	echo print_r($laguPujian);
	echo nl2br("\n\n");
	print_r(json_decode($json));
?>