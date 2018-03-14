#
# Copyright 2018 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

import time
from org.xebialabs.community.googlecloud import GoogleCloudCompute

def get_instance(google_compute, instance_name, wait_for_running=True):
    local_instance = google_compute.getInstanceBySelfLink(instance_name)
    local_status = local_instance.getStatus()
    print("Status {0}".format(local_status))
    if wait_for_running:
        if "RUNNING" == local_status:
            print("Running !")
            return local_instance
        else:
            print("Wait for {0} running".format(instance_name))
            time.sleep(5)
            return get_instance(google_compute, instance_name, True)
    else:
        return local_instance


googleCompute = GoogleCloudCompute(deployed.container.clientEmail, deployed.container.privateKey,
                                   deployed.container.projectId)

groupName = deployed.name
zone = deployed.zone

print("Wait for a new instance {} in {} ...".format(groupName, zone))
if not googleCompute.isOperationDone(deployed.operationSelfLink, zone):
    result = "RETRY"
else:
    instanceGroup = googleCompute.getInstanceByGroupName(groupName, zone)
    print("instanceGroup = {0} ".format(instanceGroup))
    managedInstancesByGroupName = googleCompute.getManagedInstancesSelfLinkByGroupName(zone, instanceGroup.getName())
    for s in managedInstancesByGroupName:
        print("instance group name is {0}".format(s))
        instance = get_instance(googleCompute, s)
        # instance = googleCompute.getInstanceBySelfLink(s)
        print("instance is {0}".format(instance))
        status = instance.getStatus()
        deployed.instanceName = instance.getName()
        deployed.instanceId = str(instance.getSelfLink())
        deployed.privateIp = instance.getNetworkInterfaces().get(0).getNetworkIP()
        if instance.getNetworkInterfaces().get(0).getAccessConfigs():
            deployed.publicIp = instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP()
        print("instance Name is {0}".format(deployed.instanceName))
        print("private  IP   is {0}".format(deployed.privateIp))
        print("public   IP   is {0}".format(deployed.publicIp))
        print("Instance ID   is {0}".format(deployed.instanceId))
