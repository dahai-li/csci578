import json
import re
from bs4 import BeautifulSoup
import os 

for i in os.listdir("app_repo/bundle/analysis/merged/"):
	f1 = open(os.path.join("app_repo/bundle/analysis/merged/", i), "r")
 	soup = BeautifulSoup(f1,"xml")

	allComponents = []
	allIntents = []
	allFilters = []
	allReceivers = []
	allSenders = []
	allPermissions = []
	appname = []

	appname = soup.application.apkFile.next_sibling.next_sibling.contents[0]
	for ele in soup.usesPermissions.find_all('name'):
			appname.append(name,ele.string)

	for eachComponent in soup.find_all('Component'):
		temp = eachComponent.find('name').string
		allComponents.append(temp)

	for eachComponent in soup.find_all('Intent'):
		temp = eachComponent.find('action').string
		if(temp):
			temp = temp.replace("\"","")
		temp2 = eachComponent.find('sender').string
		if temp is not None:
			allIntents.append(temp)
			allSenders.append(temp2)
		if temp2 not in allComponents:
			allComponents.append(temp2)

	for eachComponent in soup.find_all('Component'):
		temp = eachComponent.find('name').string
		temp2 = eachComponent.find('IntentFilter')
		for eachIntentFilter in temp2.find_all('filter'):
			for eachFilterAction in eachIntentFilter.find_all('actions'):
				allFilters.append(eachFilterAction.string)
				allReceivers.append(temp)
				if temp not in allComponents:
					allComponents.append(temp)



	for eachComponent in soup.find_all('permission'):
		allPermissions.append(eachComponent.string)

	#print soup.find('appname').string
		#theFile = json.dumps(eachComponent)
		#print(eachComponent)

	#packageName = soup.find('name').string
	print appname

	d = {
			'theAppName' : appname,
			'intentActions' : allIntents,
			'senders' : allSenders,
			'filterActions' : allFilters,
			'receivers' : allReceivers,
			'componentNames' : allComponents,
			'permissions': allPermissions
	}
	jsonFile = json.dumps(d)
	f2 = open(os.path.join("json/", i.split('.')[0] + '.json'),"w")
	f2.write("{}\n".format(jsonFile))
	#f2.write(jsonFile)
	f2.close()
