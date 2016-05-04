<?php
	if ($_GET):
		shell_exec('rm -r /home/ubuntu/didfail/toyapps/out');
		shell_exec('sh /var/www/html/test.sh');
		shell_exec('chmod 777 -R /home/ubuntu/didfail/toyapps/out');
		shell_exec('mkdir /home/ubuntu/didfail/toyapps/out/xml');
		shell_exec('cp /home/ubuntu/didfail/toyapps/out/*.xml /home/ubuntu/didfail/toyapps/out/xml');
		shell_exec('rm /var/www/html/InteractionsOutput.json');
		shell_exec('python /var/www/html/didfail.py');
		echo "OK";
	endif;
?>
