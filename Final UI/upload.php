<?php
session_start();
header("Location:index.html");

#$_SESSION['latestId'] = $lastID;
#header('Content-Type: application/json');


if(isset($_POST['upload']) && $_FILES['filename']['size']>0) {
	$servername = "csci.cshcysv7ih6f.us-west-2.rds.amazonaws.com";
	$username = "root";
	$dbname = "csci";
	$password = "123456789";
	$conn = new mysqli($servername, $username, $password, $dbname);
	mysqli_select_db($conn, $dbname);

	if($conn->connect_error) {
		die("Connection failed: ". $conn->connect_error);
	}

	#echo "Connected successfully";

	$conn->select_db("csci");
	$fileName = $_FILES['filename']['name'];
	#echo $fileName;
	$apkData = addslashes(file_get_contents($_FILES['filename']['tmp_name']));
	$sql = "INSERT INTO apks(name,data)"."VALUES('$fileName', '$apkData')";
	$success = mysqli_query($conn, $sql) or die("Error while uploading file.</br>" . mysqli_error($conn));
	if(isset($success)){
		$lastID = $conn->insert_id;
		$_SESSION['latestId'] = $lastID;
		echo "Uploaded successfully";

	}
}
exit(0);	
	
	
?>
