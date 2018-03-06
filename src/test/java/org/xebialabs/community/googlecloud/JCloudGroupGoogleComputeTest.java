/**
 * Copyright 2018 XEBIALABS
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.xebialabs.community.googlecloud;


import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceGroup;
import com.google.api.services.compute.model.Operation;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Charsets.UTF_8;

public class JCloudGroupGoogleComputeTest {
    public static void main(String args[]) throws Exception {

        String json_file_path = "/Users/bmoussaud/.ssh/MyFirstProject--468c2c49e42d.json";

        String machine = "n1-standard-1";
        String project = "just-terminus-194507";
        String zone = "europe-west1-b";

        //String instanceName = "instance-8";
        String imageName = "ubuntu-1710";
        String templateName = "instance-template-1";
        String groupName = "my-xl-group-3";
        String selfLinkCreate = "";

        String fileContents = Files.toString(new File(json_file_path), UTF_8);
        final JsonObject json = new JsonParser().parse(fileContents).getAsJsonObject();
        String client_email = json.get("client_email").toString().replace("\"", "");
        // When reading the file it reads in \n in as
        String private_key = json.get("private_key").toString().replace("\"", "").replace("\\n", "\n");
        System.out.println("private_key = " + private_key);
        System.out.println("client_email = " + client_email);
        String externalAddress = ""; //"vm-1";
        Map<String, String> metadata = Maps.newHashMap();
        metadata.put("startup-script-url", "gs://ci-scripts/start-vm.sh");
        {
            GoogleCloudCompute googleCompute = new GoogleCloudCompute(client_email, private_key, project);
            selfLinkCreate = googleCompute.createInstancesFromTemplate(groupName, templateName, zone, 2);

        }
        {
            GoogleCloudCompute googleCompute = new GoogleCloudCompute(json_file_path, project);
            //googleCompute.waitForOperation(selfLinkCreate, zone);
            System.out.println("Wait for Create....");
            waitFor(zone, groupName, googleCompute, selfLinkCreate);
            System.out.println("Created " + groupName);
        }
        {
            GoogleCloudCompute googleCompute = new GoogleCloudCompute(json_file_path, project);
            InstanceGroup instanceGroup = googleCompute.getInstanceByGroupName(groupName, zone);
            System.out.println("instanceGroup = " + instanceGroup);
            List<String> managedInstancesByGroupName = googleCompute.getManagedInstancesSelfLinkByGroupName(zone, instanceGroup.getName());
            for (String s : managedInstancesByGroupName) {
                System.out.println("s = " + s);
                Instance instance = googleCompute.getInstanceBySelfLink(s);
                String natIP = instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP();
                String id = instance.getSelfLink().toString();
                String status = instance.getStatus();
                System.out.println("id = " + id);
                System.out.println("natIP = " + natIP);
                System.out.println("status = " + status);
            }

            System.out.println("--- Instances --");
            System.out.println(googleCompute.getInstanceNames());
            System.out.println("--- /Instances --");

        }
        {
            GoogleCloudCompute googleCompute = new GoogleCloudCompute(json_file_path, project);
            String selfLinkDelete = googleCompute.deleteInstanceFromGroup(groupName, zone);
            System.out.println("Wait for Delete....");
            waitFor(zone, groupName, googleCompute, selfLinkDelete);
        }
        System.out.println(" DONE !");
    }

    public static void waitFor(String zone, String instanceName, GoogleCloudCompute googleCompute, String selfLinkDelete) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        while (!googleCompute.isOperationDone(selfLinkDelete, zone)) {
            Thread.sleep(1000 * 5);
        }
        long stop = System.currentTimeMillis();
        System.out.println("Deleted " + instanceName);
        long delta = (stop - start) / 1000;
        System.out.println("delta = " + delta + " seconds");
        Operation operation = googleCompute.getOperation(selfLinkDelete, zone);
        System.out.println("operation.getInsertTime() = " + operation.getInsertTime());
        System.out.println("operation.getStartTime()  = " + operation.getStartTime());
        System.out.println("operation.getEndTime()    = " + operation.getEndTime());
    }

}
