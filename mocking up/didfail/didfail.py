import simplejson 
from bs4 import BeautifulSoup 
import os 
 
files = [] 
for i in os.listdir("/home/ubuntu/didfail/toyapps/out/xml"): 
    files.append(os.path.join("/home/ubuntu/didfail/toyapps/out/xml", i))

data = []
sendcomponents = []
receivecomponents = []

for file in files:
    fp = open(file, "r")
    soup = BeautifulSoup(fp, "xml")
    fp.close

    for componentElement in soup.findAll('flow'):
        component = {}
        
        if componentElement.find('sink', {'is-intent':True}):
            component['comp_name'] = str(componentElement.find('source')['component'])
            component['is-intent'] = str(componentElement.find('sink')['is-intent'])
            sendcomponents.append(component)

        if componentElement.find('sink', {'is-intent-result':True}):
            component['comp_name'] = str(componentElement.find('sink')['component'])
            component['is-intent-result'] = str(componentElement.find('sink')['is-intent-result'])
            receivecomponents.append(component)

for sendcomponent in sendcomponents:
    json = {}
    jsoncomponents = []

    for receivecomponent in receivecomponents:
        if sendcomponent['is-intent'] == receivecomponent['is-intent-result']:
            sendcomp = {}
            receivecomp = {}
            
            sendcomp['comp_name'] = sendcomponent['comp_name']
            jsoncomponents.append(sendcomp)

            receivecomp['comp_name'] = receivecomponent['comp_name']
            jsoncomponents.append(receivecomp)

            json['attack_type'] = str('Didfail attack')
            json['components'] = jsoncomponents
    data.append(json)

with open('InteractionsOutput.json','w') as fp:
    fp.write(simplejson.dumps(data, sort_keys=True, indent=4 * ' '))

