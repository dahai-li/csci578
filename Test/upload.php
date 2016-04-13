<?php
header('Content-Type: application/json');

$uploaded = array();

if(!empty($_FILES['file']['name'][0])) {
	
	foreach($_FILES['file']['name'] as $position => $name){
		if(move_uploaded_file($_FILES['file']['tmp_name'][$position], '/Applications/XAMPP/xamppfiles/htdocs/Test/uploads/' . $name)) {
			$uploaded[] = array(
				'name' => $name,
				'file' => '/Applications/XAMPP/xamppfiles/htdocs/Test/uploads/' .$name
				);
		}
	}
}

echo json_encode($uploaded);

?>