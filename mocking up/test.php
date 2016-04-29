<?php
	if ($_GET):
		shell_exec('/var/www/html/test.sh');
		shell_exec('chmod 777 -R /home/ubuntu/COVERT/covert_dist/app_repo/bundle/analysis/merged');
		shell_exec('cp /home/ubuntu/COVERT/covert_dist/app_repo/bundle/analysis/merged/*.xml /var/www/html');
		shell_exec('python /var/www/html/Parser.py');
		shell_exec('python /var/www/html/Interactions.py');
		echo "OK";
	endif;
?>
