<?php
session_start();
header('location: visualization.html');
$lastId = $_SESSION['latestId'];
file_put_contents("log.txt", $lastId.'\\n', FILE_APPEND);


#echo "Hello";

$url = "52.33.74.131:8080/ToolRest/api/runArchitecture";

class apk {
	public $id;
	public function setId($id) {
        $this->id = $id;
        return $this;
    }
}
class apks {
	public $apks;
	public function setList($list) {
		$this->apks = $list;
		return $this;
	}
}
// new APKs object
	$apks = new Apks();
// new APK array
	$apkArray = array();
// create apk with id 1
	
	for($i = 1; $i <= $lastId; $i = $i+1){
			array_push($apkArray, (new apk)->setId($i));
		}

		$apks->setList($apkArray);
		$data = json_encode($apks);
		echo $data;
#$_SESSION['jsonInput'] = $data;
$file = fopen("jsonInput.txt","w");
echo fwrite($file, $data);
fclose($file);

file_put_contents("log.txt", $data.'\\n', FILE_APPEND);

$items = http_build_query($apks);

$client = curl_init();

curl_setopt($client, CURLOPT_URL, $url);
curl_setopt($client, CURLOPT_CUSTOMREQUEST, "POST");
//curl_setopt($client, CURLOPT_POST, count($data));
curl_setopt($client, CURLOPT_RETURNTRANSFER, true);
curl_setopt($client, CURLOPT_POSTFIELDS, $data);
curl_setopt($client, CURLOPT_HTTPHEADER, array(
	'Content-Type: application/json',
	'Content-Length: '.strlen($data))
);

$response = curl_exec($client);
 if(curl_errno($client)) {
 	file_put_contents("log.txt", "Error in sending Request",FILE_APPEND);
 	die('Couldn\'t send request: '.curl_error($client));
 }

file_put_contents("log.txt", $response.'\\n', FILE_APPEND);

$file = fopen("arch.js","w");
echo fwrite($file, "var result = ".$response.";");
fclose($file);
curl_close($client);

exit(0);
#echo $response;

?>

