/**
 * Copyright 2018 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
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
        String json_file_path = "/Users/bmoussaud/.ssh/MyFirstProject-3606a89398e9.json";

        String machine = "n1-standard-1";
        String project = "just-terminus-194507";
        String zone = "europe-west1-b";

        String instanceName = "simple-instance-4";
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
            JCloudGoogleCompute googleCompute = new JCloudGoogleCompute(client_email, private_key);
            selfLinkCreate = googleCompute.createInstance(instanceName, imageName, "ubuntu-os-cloud", machine, zone, externalAddress, metadata);
        }
        {
            JCloudGoogleCompute googleCompute = new JCloudGoogleCompute(json_file_path);
            googleCompute.waitForOperation(selfLinkCreate);
            System.out.println("Created " + instanceName);
        }
        {
            JCloudGoogleCompute googleCompute = new JCloudGoogleCompute(json_file_path);
            Instance instance = googleCompute.getInstanceByName(instanceName, zone);
            System.out.println("instance = " + instance);
            String natIP = instance.networkInterfaces().get(0).accessConfigs().get(0).natIP();
            String id = instance.selfLink().toString();
            Instance.Status status = instance.status();
            System.out.println("id = " + id);
            System.out.println("natIP = " + natIP);
            System.out.println("status = " + status);
        }
        {
            JCloudGoogleCompute googleCompute = new JCloudGoogleCompute(json_file_path);
            String selfLinkDelete = googleCompute.deleteInstance(instanceName, zone);
            googleCompute.waitForOperation(selfLinkDelete);
            System.out.println("Deleted " + instanceName);
        }
        System.out.println(" DONE !");
    }

}
