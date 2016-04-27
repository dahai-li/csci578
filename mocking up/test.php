<?php
	if ($_GET):
		shell_exec('/var/www/html/test.sh');
		shell_exec('chmod 777 -R /home/ubuntu/COVERT/covert_dist/app_repo/bundle/analysis/merged');
		shell_exec('python /var/www/html/homework3.py');
		echo "OK";
	endif;
?>
