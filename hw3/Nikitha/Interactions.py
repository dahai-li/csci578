import json
from bs4 import BeautifulSoup
senderreceiver=[]
per=[]

files = ['AudioRecorder.json','ContactsList-debug.json','SetWallpaper.json','Snake-debug.json','Battery HD.json','Google Calendar.json','Facebook.json','Compass for Android.json',
'GO Weather EX.json','Percentage Calculator v1.json','MapsNavigationDirections.json','Equalizer & Bass Booster.json','Student Agenda.json','Quick Memo.json','UC Cleaner.json',
'Magic Fluids Free.json','Easy Screenshot.json','Chrome Browser - Google_49_0_2623_105_apk-dl_com.json','Battery Doctor (Battery Saver).json','Twitter.json']

xmlfiles = ['AudioRecorder.xml','ContactsList-debug.xml','SetWallpaper.xml','Snake-debug.xml','Battery HD.xml','Google Calendar.xml','Facebook.xml','Compass for Android.xml',
'GO Weather EX.xml','Percentage Calculator v1.xml','MapsNavigationDirections.xml','Equalizer & Bass Booster.xml','Student Agenda.xml','Quick Memo.xml','UC Cleaner.xml',
'Magic Fluids Free.xml','Easy Screenshot.xml','Chrome Browser - Google_49_0_2623_105_apk-dl_com.xml','Battery Doctor (Battery Saver).xml','Twitter.xml']

for f in range(len(files)):
	#for k in range(f+1,4,1):
	for k in range(f+1,len(files)):
		app1 = json.loads(open(files[f]).read())
		app2 = json.loads(open(files[k]).read())
		act=[]
		intents=[]
		flag1 = False
	

		for i in range(len(app1["Intents"])):
			intents.append(app1["Intents"][i])
			x=intents[i]
			if(x):
				intents[i]=x.replace("\"","")
				for j in range(len(app2["Actions"])):
					act.append(app2["Actions"][j])
					if(intents[i] == act[j]):
						flag1=True
						senderreceiver.append([app1["sender"][i],app2["Components Name"][j]])
				

	
for p in range(len(xmlfiles)):
	soup = BeautifulSoup(open(xmlfiles[p]),"xml")
	name = soup.application.apkFile.next_sibling.next_sibling.contents[0]
	for ele in soup.usesPermissions.find_all('permission'):
		per.append([name,ele.string])

data={
'interacting Components':senderreceiver,
'Permissions':per
}

json_str = json.dumps(data)
f= open('InteractionsOutput.json',"w")
f.write(json_str) 

f.close()
