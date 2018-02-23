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

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Injector;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;

import org.jclouds.domain.Credentials;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.googlecloud.domain.ListPage;
import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.googlecomputeengine.GoogleComputeEngineProviderMetadata;
import org.jclouds.googlecomputeengine.domain.*;


import org.jclouds.googlecomputeengine.domain.Image;
import org.jclouds.googlecomputeengine.features.InstanceApi;
import org.jclouds.googlecomputeengine.options.ListOptions;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;
import com.google.inject.Module;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.base.Charsets.UTF_8;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_SCRIPT_COMPLETE;
import static org.jclouds.compute.options.TemplateOptions.Builder.runScript;
import static org.jclouds.googlecomputeengine.options.ListOptions.Builder.filter;

// Based on https://github.com/jclouds/jclouds-examples/blob/master/google-lb/src/main/java/org/jclouds/examples/google/lb/MainApp.java
public class JCloudGoogleCompute {

    private final Credentials credentials;
    private final GoogleComputeEngineApi googleApi;
    private final String project;


    public JCloudGoogleCompute(final String json_file_path) throws IOException {
        this.credentials = getCredentialFromJsonKeyFile(json_file_path);
        this.googleApi = createGoogleComputeEngineApi(credentials.identity, credentials.credential);
        this.project = this.googleApi.project().get().name();
        System.out.printf("Successfully Authenticated to project '%s'\n", project);
    }

    public JCloudGoogleCompute(final String clientEmail, final String privateKey) throws IOException {
        this.credentials = new Credentials(clientEmail, privateKey);
        this.googleApi = createGoogleComputeEngineApi(credentials.identity, credentials.credential);
        this.project = this.googleApi.project().get().name();
        System.out.printf("Successfully Authenticated to project '%s'\n", project);
    }

    public String deleteInstance(String instanceName, String zone) {
        Operation o = getInstanceApi(zone).delete(instanceName);
        return o.selfLink().toString();
    }

    public String createInstance(String instanceName, String imageName, String imageProject, String machine, String zone) {
        URI machineTypeURL = googleApi.machineTypesInZone(zone).get(machine).selfLink();
        System.out.println("machineTypeURL = " + machineTypeURL);

        Image image = searchImage(imageName, imageProject);
        System.out.println("image = " + image);

        URI networkURL = googleApi.networks().get("default").selfLink();
        if (networkURL == null) {
            throw new RuntimeException("Your project does not have a default network. Please recreate the default network or try again with a new project");
        }
        System.out.println("networkURL = " + networkURL);


        NewInstance newInstance = NewInstance.create(
            instanceName, // name
            machineTypeURL, // machineType
            networkURL, // network
            image.selfLink());
        System.out.println("newInstance = " + newInstance);

        Operation o = getInstanceApi(zone).create(newInstance);

        System.out.println("o = " + o);

        return o.selfLink().toString();
    }

    private Image searchImage(String imageName, String imageProject) {
        System.out.println("JCloudGoogleCompute.searchImage " + imageName + "," + imageProject);
        ListOptions filter = filter(String.format("name eq %s.*", imageName));
        List<Image> list = googleApi.images().listInProject(imageProject, filter).next();
        List<Image> result = new ArrayList<>();
        for (Image image : list) {
            if (image.deprecated() == null) {
                result.add(image);
            }
        }
        if (result.isEmpty()) {
            throw new RuntimeException("Image not found " + imageName);
        }
        return result.iterator().next();
    }


    private static Credentials getCredentialFromJsonKeyFile(String filename) throws IOException {
        String fileContents = Files.toString(new File(filename), UTF_8);
        Supplier<Credentials> credentialSupplier = new GoogleCredentialsFromJson(fileContents);
        return credentialSupplier.get();
    }


    private InstanceApi getInstanceApi(String zone) {
        InstanceApi instanceApi = googleApi.instancesInZone(zone);
        return instanceApi;
    }

    private static GoogleComputeEngineApi createGoogleComputeEngineApi(String identity, String credential) {
        ContextBuilder contextBuilder = ContextBuilder.newBuilder(GoogleComputeEngineProviderMetadata.builder().build())
            .credentials(identity, credential);
        Injector injector = contextBuilder.buildInjector();
        return injector.getInstance(GoogleComputeEngineApi.class);
    }

    public Instance getInstanceByName(String name, String zone) {
        ListOptions filter = filter(String.format("name eq %s", name));
        ListPage<Instance> next = getInstanceApi(zone).list(filter).next();
        return getOnlyElement(next);
    }

    public int waitForOperation(String operationSelfLink) {
        Operation operation = getOperation(operationSelfLink);

        int timeout = 60; // seconds
        int time = 0;

        while (operation.status() != Operation.Status.DONE) {
            if (time >= timeout) {
                return 1;
            }
            time++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            operation = this.googleApi.operations().get(operation.selfLink());
        }
        //TODO: Check for errors.
        return 0;
    }

    public boolean isOperationDone(String operationSelfLink) {
        final Operation operation = getOperation(operationSelfLink);
        return operation.status() == Operation.Status.DONE;
    }

    public Operation getOperation(String operationSelfLink) {
        URI uri = URI.create(operationSelfLink);
        return this.googleApi.operations().get(uri);
    }
}
