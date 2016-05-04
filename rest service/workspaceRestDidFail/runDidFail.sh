/home/ubuntu/didfail/cert/run-didfail.sh /home/ubuntu/didfail/toyapps/out/ /home/ubuntu/didfail/toyapps/*.apk
chmod 777 -R /home/ubuntu/didfail/toyapps/out
rm -rf /home/ubuntu/didfail/toyapps/out/xml/*.xml
mkdir /home/ubuntu/didfail/toyapps/out/xml
cp /home/ubuntu/didfail/toyapps/out/*.xml /home/ubuntu/didfail/toyapps/out/xml
rm InteractionsOutput.json
python didfail.py
