import json
import os

files = []
for i in os.listdir("json"):
	files.append(os.path.join("json/", i))

data = []
for i in files:
	data.append(json.loads(open(i).read()))

link = []
allComponent1 = []
allComponent2 = []
theCommonAction = []
answer = []

for x in range(0,len(data)):
	temp = data[x]['intentActions']
	for x1 in range(0,len(temp)):
		curIntentAction = temp[x1]
		for y in range(0,len(data)):
			innerTemp = data[y]['filterActions']
			for y2 in range(0,len(innerTemp)):
				curFilterAction = innerTemp[y2]
				if(curFilterAction == curIntentAction and x != y):
					allComponent1.append(data[x]['senders'][x1])
					allComponent2.append(data[y]['receivers'][y2])
					theCommonAction.append(curIntentAction)
					d1 = {
						'Component1' : data[x]['senders'][x1],
						'Component2' : data[y]['receivers'][y2],
						'theCommonAction' : curIntentAction
					}
					if d1 not in answer:
						answer.append(d1)

#d = {
#	'Application1' : allComponent1,
#	'Application2' : allComponent2,
#	'The Common Action' : theCommonAction
#}

jsonFile = json.dumps(answer)
f2 = open("json/finalOutput.json","w")
f2.write(jsonFile)
f2.close()
