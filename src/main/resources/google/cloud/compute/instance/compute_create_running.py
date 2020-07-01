#
# Copyright 2020 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

from org.xebialabs.community.googlecloud  import GoogleCloudCompute

googleCompute = GoogleCloudCompute(deployed.container.clientEmail, deployed.container.privateKey, deployed.container.projectId)

instanceName = deployed.instanceName if deployed.instanceName else deployed.name
zone = deployed.zone

print("Wait for a new instance {} in {} ...".format(instanceName, zone))
if not googleCompute.isOperationDone(deployed.operationSelfLink, zone):
    result = "RETRY"
else:
    instance = googleCompute.getInstanceByName(instanceName, zone)
    print("instance is {0}".format(instance))
    deployed.instanceId = str(instance.getSelfLink())
    deployed.privateIp = instance.getNetworkInterfaces().get(0).getNetworkIP()
    if instance.getNetworkInterfaces().get(0).getAccessConfigs():
        deployed.publicIp = instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP()
    print("instance ID is {0}".format(deployed.instanceId))
    print("private  IP is {0}".format(deployed.privateIp))
    print("public   IP is {0}".format(deployed.publicIp))
