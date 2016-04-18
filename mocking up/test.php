<?php
	if ($_GET):
		shell_exec('/home/jjooyoun/Project/test.sh');
		shell_exec('chmod 777 -R /home/jjooyoun/COVERT/covert_dist/app_repo/bundle/analysis/merged');
		shell_exec('python /home/jjooyoun/Project/homework3.py');
		echo "OK";
	endif;
?>
