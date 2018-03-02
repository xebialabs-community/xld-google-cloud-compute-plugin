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


import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jclouds.googlecomputeengine.domain.Instance;
import org.jclouds.googlecomputeengine.domain.JCloudGoogleCompute;

import java.io.File;
import java.util.Map;

import static com.google.common.base.Charsets.UTF_8;

public class JCloudGoogleComputeTest {
    public static void main(String args[]) throws Exception {
        //String json_file_path = "/Users/bmoussaud/.ssh/MyFirstProject-3606a89398e9.json";
        String json_file_path = "/Users/bmoussaud/.ssh/MyFirstProject--468c2c49e42d.json";
        //String json_file_path_2 = "/Users/bmoussaud/.ssh/gcloud.json";

        String machine = "n1-standard-1";
        String project = "just-terminus-194507";
        String zone = "europe-west1-b";

        String instanceName = "instance-4";
        String imageName = "ubuntu-1710";
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
            //GoogleCloudCompute googleCompute = new GoogleCloudCompute(client_email, private_key);
            GoogleCloudCompute googleCompute = new GoogleCloudCompute(json_file_path);
            selfLinkCreate = googleCompute.createInstance(instanceName, imageName, "ubuntu-os-cloud", machine, zone, externalAddress, metadata);
        }
        {
            GoogleCloudCompute googleCompute = new GoogleCloudCompute(json_file_path);
            googleCompute.waitForOperation(selfLinkCreate, zone);
            System.out.println("Created " + instanceName);
        }
        {
            GoogleCloudCompute googleCompute = new GoogleCloudCompute(json_file_path);
            com.google.api.services.compute.model.Instance instance = googleCompute.getInstanceByName(instanceName, zone);
            System.out.println("instance = " + instance);

            String natIP = instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP();
            String id = instance.getSelfLink().toString();
            String status = instance.getStatus();
            System.out.println("id = " + id);
            System.out.println("natIP = " + natIP);
            System.out.println("status = " + status);
        }
        {
            GoogleCloudCompute googleCompute = new GoogleCloudCompute(json_file_path);
            String selfLinkDelete = googleCompute.deleteInstance(instanceName, zone);
            googleCompute.waitForOperation(selfLinkDelete, zone);
            System.out.println("Deleted " + instanceName);
        }
        System.out.println(" DONE !");
    }

}
