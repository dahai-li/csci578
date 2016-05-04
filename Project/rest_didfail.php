<?php
session_start();
header('location: vulner_didfail.html');
#echo $_SESSION['latestId'];

#echo "Hello";

$url = "52.39.121.47:8080/ToolRest/api/runAnalysis";

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
	$newId = (new apk)->setId("1");
// add to the end of the array
	array_push($apkArray, $newId);
// create apk with id 2 and add to the end of the array
	array_push($apkArray, (new apk)->setId("2"));
	$apks->setList($apkArray);
	$data = json_encode($apks);
	#echo $data;

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


$file = fopen("vul_didfail.js","w");
echo fwrite($file, "var result = ".$response.";");
fclose($file);
curl_close($client);
exit(0);
#echo $response;

?>

