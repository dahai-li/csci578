cd soot-infoflow-android-iccta

mysql -uroot -proot -e 'drop database cc';
mysql -uroot -proot -e 'create database cc';
mysql -uroot -proot cc < res/schema;

## EPICC ##
cd iccProvider/epicc
mkdir output_iccta

## Run epicc
for var in "$@"
do
    echo "$var"
	./runEpicc.sh $var
done

## APK COMBINER ##
cd ../../release/

# Remove previous apks
rm -f *.apk

# Run apk combiner
./preparingEnv.sh
./runApkCombiner.sh $@

# Run IccTA
echo "Finding APK"
combinedApk=$(find . -name *.apk)
echo $combinedApk
# Run iccTa
java -Xmx8g -jar IccTA.jar -apkPath $combinedApk -androidJars ../android-platforms -iccProvider epicc -intentMatchLevel 3 -enableDB -nocallbacks > output_iccta/java_out.txt
