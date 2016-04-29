import json
import re
from bs4 import BeautifulSoup


soup = BeautifulSoup(open('Facebook.xml'),"xml")

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
f= open('Facebook.json',"w")
f.write(json_str) 

f.close()
