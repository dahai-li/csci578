rm -rf json
mkdir json
chmod 777 -R json

rm -rf jsonJoin/
mkdir jsonJoin
chmod 777 jsonJoin

./covert.sh bundle
python Parser1.py
./ParserJoin4Parser2.sh
python Interactions.py
