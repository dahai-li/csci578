rm -rf json
mkdir json
chmod 777 -R json

./covert.sh bundle
python Parser.py
python Interactions.py
