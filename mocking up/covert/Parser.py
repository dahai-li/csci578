import json
import re
from bs4 import BeautifulSoup
import os 

for i in os.listdir("/var/www/html/xml"):
	fp = open(os.path.join("/var/www/html/xml/", i), "r")
 	soup = BeautifulSoup(fp, "xml")
	fp.close

	name = soup.application.apkFile.next_sibling.next_sibling.contents[0]

	action=[]
	permissions=[]
	component=[]
	intents=[]
	sender=[]

	for element in soup.components.find_all('actions'):
		action.append(element.string)
	
	for e in soup.components.find_all('name'):
		component.append(e.string)

	for ele in soup.usesPermissions.find_all('permission'):
		permissions.append(ele.string)

	for x in soup.newIntents.find_all('action'):
		intents.append(x.string)	
		
	for a in soup.newIntents.find_all('sender'): 
		sender.append(a.string)
	
	data = {
		'Application Name':name,
		'Actions':action,
		'Permissions':permissions,
		'Components Name':component,
		'Intents':intents,
		'sender':sender
	}

	json_str = json.dumps(data)
	f= open(os.path.join("/var/www/html/json/", i.split('.')[0] + '.json'),"w")
	f.write(json_str) 
	f.close()
