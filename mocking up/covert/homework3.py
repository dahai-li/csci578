import simplejson
from bs4 import BeautifulSoup

files = ["/home/ubuntu/COVERT/covert_dist/app_repo/bundle/analysis/merged/Facebook.xml"]

data = {}
nodes = []
links = []
intents = []

index = 0
for file in files:
    fp = open(file, "r")
    soup = BeautifulSoup(fp, "xml")
    fp.close

    apkName = soup.findAll('name')[-1].string;

    maincomponent = {}
    maincomponent['index'] = index
    maincomponent['label'] = apkName
    maincomponent['name'] = apkName
    maincomponent['level'] = 1
    maincomponent['score'] = 10
    maincomponent['id'] = index

    mainlinks = []
    maindescriptions = []
    maincomponent['links'] = mainlinks
    maincomponent['description'] = apkName

    nodes.append(maincomponent)

    index += 1

    for componentElement in soup.findAll('Component'):
        component = {}
        component['index'] = index
        component['type'] = componentElement.find('type').string
        component['label'] = componentElement.find('name').string
        component['name'] = apkName
        component['level'] = 2
        component['score'] = 8
        component['id'] = index

        componentlinks = []
        componentlinks.append(maincomponent.get('index'))
        component['links'] = componentlinks
        maincomponent.get('links').append(index)

        descriptions = []
        component['description'] = descriptions

        actions = []
        for filterElement in componentElement.find('IntentFilter').findAll('filter'):
            for actionElement in filterElement.findAll('actions'):
                if actions.count(actionElement.string) == 0:
                    actions.append(actionElement.string)
        component['intent filter actions'] = actions
        
        nodes.append(component)

        link = {}
        link['source'] = index
        link['target'] = maincomponent.get('index')
        link['weight'] = 1

        links.append(link)

        index += 1
    data['nodes'] = nodes
    data['links'] = links


    for intentElement in soup.findAll('Intent'):
        intent = {}
        intent['sender'] = intentElement.find('sender').string
        intent['component'] = intentElement.find('component').string
        intent['action'] = str(intentElement.find('action').string).strip('"')
        intents.append(intent)

for intent in intents:
    for component in data['nodes']:
        if (intent.get('component') != None):
            if ((intent.get('sender') != intent.get('component'))
                & (intent.get('sender') == component.get('label'))):
                intent['explicit sender index'] = component.get('index')
                intent['explicit sender name'] = component.get('label')
                intent['explicit sender apk'] = component.get('name')
                intent['explicit intent type'] = 'Explicit Intent (' +  intent.get('component') + ')'

            if ((intent.get('sender') != intent.get('component'))
                & (intent.get('component') == component.get('label'))):
                intent['explicit recipient index'] = component.get('index')
                intent['explicit recipient name'] = component.get('label')
                intent['explicit recipient apk'] = component.get('name')
                intent['explicit intent type'] = 'Explicit Intent (' +  intent.get('component') + ')'
                    
        if (intent.get('action') != None):
            if ((intent.get('sender') != intent.get('component'))
                & (intent.get('sender') == component.get('label'))):
                intent['implicit sender index'] = component.get('index')
                intent['implicit sender name'] = component.get('label')
                intent['implicit sender apk'] = component.get('name')
                intent['implicit intent type'] = 'Implicit Intent (' +  intent.get('action') + ')'

            if (component.get('intent filter actions') != None):
                if (component.get('intent filter actions').count(intent.get('action')) != 0):
                    intent['implicit recipient index'] = component.get('index')
                    intent['implicit recipient name'] = component.get('label')
                    intent['implicit recipient apk'] = component.get('name')
                    intent['implicit intent type'] = 'Implicit Intent (' + intent.get('action') + ')'

for intent in intents:
    if ((intent.get('explicit recipient index') != None) & (intent.get('explicit sender index') != None)):
        for node in data['nodes']:
            if (intent.get('explicit sender index') == node.get('index')):
                if (node.get('links').count(intent.get('explicit recipient index')) == 0):
                    node.get('links').append(intent.get('explicit recipient index'))
                    node.get('description').append('Send' + ' ' + intent.get('explicit intent type') + ' ' + 'to' + ' '
                                                    + intent.get('explicit recipient name') + ' (' + 'APK name : ' + intent.get('explicit recipient apk') + ') ')

                    link = {}
                    link['source'] = intent.get('explicit sender index')
                    link['target'] = intent.get('explicit recipient index')
                    link['weight'] = 1
                    data.get('links').append(link)
                        
            if (intent.get('explicit recipient index') == node.get('index')) :
                if (node.get('links').count(intent.get('explicit sender index')) == 0):
                    node.get('links').append(intent.get('explicit sender index'))
                    node.get('description').append('Receive ' + ' ' + intent.get('explicit intent type') + ' ' + 'from' + ' '
                                                    + intent.get('explicit sender name') + ' (' + 'APK name : ' + intent.get('explicit sender apk') + ') ')

    if ((intent.get('implicit recipient index') != None) & (intent.get('implicit sender index') != None)
        & (intent.get('implicit recipient index') != intent.get('implicit sender index'))):
        for node in data['nodes']:
            if (intent.get('implicit sender index') == node.get('index')):
                if (node.get('links').count(intent.get('implicit recipient index')) == 0):
                    node.get('links').append(intent.get('implicit recipient index'))
                    node.get('description').append('Send' + ' ' + intent.get('implicit intent type') + ' ' + 'to' + ' '
                                                    + intent.get('implicit recipient name') + ' (' + 'APK name : ' + intent.get('implicit recipient apk') + ') ')
                                                       
                    link = {}
                    link['source'] = intent.get('implicit sender index')
                    link['target'] = intent.get('implicit recipient index')
                    link['weight'] = 1
                    data.get('links').append(link)
                        
            if (intent.get('implicit recipient index') == node.get('index')) :
                if (node.get('links').count(intent.get('implicit sender index')) == 0):
                    node.get('links').append(intent.get('implicit sender index'))
                    node.get('description').append('Receive' + ' ' + intent.get('implicit intent type') + ' ' + 'from' + ' ' 
                                                    + intent.get('implicit sender name') + ' (' + 'APK name : ' + intent.get('implicit sender apk') + ') ')
        
with open('homework3.json','w') as fp:
    fp.write(simplejson.dumps(data, sort_keys=True, indent=4 * ' '))
