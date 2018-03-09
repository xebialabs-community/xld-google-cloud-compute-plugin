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


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.getOnlyElement;

public class GoogleCloudCompute {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(ComputeScopes.COMPUTE);
    private static final String APPLICATION_NAME = "XebiaLabs/1.0";

    private final HttpTransport httpTransport;
    private final Credential credential;
    private final Compute compute;
    private final String project;

    public GoogleCloudCompute(final String client_email, String private_key, final String projectId) throws IOException, GeneralSecurityException {

        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        PrivateKey privateKey = privateKeyFromPkcs8(private_key);
        GoogleCredential.Builder credentialBuilder = new GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(JSON_FACTORY)
            .setServiceAccountId(client_email)
            .setServiceAccountScopes(Collections.emptyList())
            .setServiceAccountPrivateKey(privateKey)
            .setServiceAccountProjectId(projectId);
        //.setServiceAccountPrivateKeyId(privateKeyId);

        this.credential = credentialBuilder.build().createScoped(SCOPES);

        this.compute = new Compute.Builder(
            httpTransport, JSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME)
            .setHttpRequestInitializer(credential)
            .build();
        this.project = projectId;
    }

    public GoogleCloudCompute(final String json_file_path, final String projectId) throws IOException, GeneralSecurityException {
        this.credential = GoogleCredential.fromStream(new FileInputStream(json_file_path)).createScoped(SCOPES);
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.compute = new Compute.Builder(
            httpTransport, JSON_FACTORY, null)
            .setApplicationName(APPLICATION_NAME)
            .setHttpRequestInitializer(credential)
            .build();
        this.project = projectId;
    }


    public String createInstance(String instanceName, String imageName, String imageProject, String machine, String zone, String externalAddress, Map<String, String> metadata) throws IOException {
        List<MachineType> machineTypesForZone = getMachineTypesForZone(machine, zone);
        MachineType machineType = getOnlyElement(machineTypesForZone);
        System.out.println("machineType = " + machineType);

        List<Image> images = searchImage(imageName, imageProject);
        Image image = getFirst(images, null);
        System.out.println("image = " + image);

        Network network = compute.networks().get(project, "default").execute();
        System.out.println("network = " + network);

        Instance instance = new Instance();
        instance.setName(instanceName);
        instance.setMachineType(machineType.getSelfLink());

        NetworkInterface ifc = new NetworkInterface();
        ifc.setNetwork(network.getSelfLink());
        List<AccessConfig> configs = new ArrayList<>();
        AccessConfig config = new AccessConfig();
        config.setType("ONE_TO_ONE_NAT");
        config.setName("External NAT");
        if (!Strings.isNullOrEmpty(externalAddress)) {
            String address = this.compute.addresses().get(project, getRegion(zone), externalAddress).execute().getAddress();
            System.out.println(externalAddress + "-> Set address = " + address);
            config.setNatIP(address);
        }
        configs.add(config);
        ifc.setAccessConfigs(configs);
        instance.setNetworkInterfaces(Collections.singletonList(ifc));


        // Add attached Persistent Disk to be used by VM Instance.
        AttachedDisk disk = new AttachedDisk();
        disk.setBoot(true);
        disk.setAutoDelete(true);
        disk.setType("PERSISTENT");
        AttachedDiskInitializeParams params = new AttachedDiskInitializeParams();
        // Assign the Persistent Disk the same name as the VM Instance.
        params.setDiskName(instanceName);
        // Specify the source operating system machine image to be used by the VM Instance.
        params.setSourceImage(image.getSelfLink());
        // Specify the disk type as Standard Persistent Disk
        params.setDiskType("https://www.googleapis.com/compute/v1/projects/" + project + "/zones/"
            + zone + "/diskTypes/pd-standard");
        disk.setInitializeParams(params);
        instance.setDisks(Collections.singletonList(disk));


        // Initialize the service account to be used by the VM Instance and set the API access scopes.
        ServiceAccount account = new ServiceAccount();
        account.setEmail("default");
        List<String> scopes = new ArrayList<>();
        scopes.add("https://www.googleapis.com/auth/devstorage.full_control");
        scopes.add("https://www.googleapis.com/auth/compute");
        account.setScopes(scopes);
        instance.setServiceAccounts(Collections.singletonList(account));

        // Optional - Add a startup script to be used by the VM Instance.
        Metadata meta = new Metadata();
        List<Metadata.Items> items = Lists.newArrayList();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            Metadata.Items item = new Metadata.Items();
            item.setKey(entry.getKey());
            item.setValue(entry.getValue());
            items.add(item);
        }
        meta.setItems(items);
        instance.setMetadata(meta);


        System.out.println(instance.toPrettyString());

        Compute.Instances.Insert insert = compute.instances().insert(project, zone, instance);

        Operation execute = insert.execute();
        return execute.getName();
    }

    public String createInstancesFromTemplate(String groupName, String templateName, String zone, int targetSize) throws IOException {
        Compute.InstanceTemplates.Get request = this.compute.instanceTemplates().get(project, templateName);
        InstanceTemplate response = request.execute();
        InstanceGroupManager instanceGroupManager = new InstanceGroupManager()
            .setName(groupName)
            .setInstanceTemplate(response.getSelfLink())
            .setTargetSize(targetSize);

        Operation operation = this.compute.instanceGroupManagers().insert(project, zone, instanceGroupManager).execute();

        return operation.getName();
    }

    private String getRegion(String zone) throws IOException {

        Compute.Regions.List request = this.compute.regions().list(project);
        RegionList response;
        do {
            response = request.execute();
            if (response.getItems() == null) {
                continue;
            }

            for (Region region : response.getItems()) {
                if (zone.startsWith(region.getName())) {
                    return region.getName();
                }
            }
            request.setPageToken(response.getNextPageToken());
        } while (response.getNextPageToken() != null);

        throw new RuntimeException("No region found for zone" + zone);
    }

    private List<Image> searchImage(String imageName, String imageProject) throws IOException {
        List<com.google.api.services.compute.model.Image> result = Lists.newArrayList();
        String filter = String.format("name eq %s.*", imageName);
        Compute.Images.List request = this.compute.images().list(imageProject).setFilter(filter);
        ImageList response;
        do {
            response = request.execute();
            if (response.getItems() == null) {
                continue;
            }

            for (com.google.api.services.compute.model.Image image : response.getItems()) {
                result.add(image);
            }
            request.setPageToken(response.getNextPageToken());
        } while (response.getNextPageToken() != null);

        return result;

    }

    protected List<MachineType> getMachineTypesForZone(String machine, String zone) throws IOException {
        String filter = String.format("(zone eq %s)(name eq %s)", zone, machine);
        System.out.println("Search machine" + machine + " using" + filter);
        Compute.MachineTypes.AggregatedList request = this.compute.machineTypes().aggregatedList(project).setFilter(filter);
        MachineTypeAggregatedList response;
        List<MachineType> result = Lists.newArrayList();
        do {
            response = request.execute();
            if (response.getItems() == null) {
                continue;
            }
            for (Map.Entry<String, MachineTypesScopedList> item : response.getItems().entrySet()) {
                List<MachineType> machineTypes = item.getValue().getMachineTypes();
                if (machineTypes == null)
                    continue;
                for (MachineType machineType : machineTypes) {
                    result.add(machineType);
                }

            }
            request.setPageToken(response.getNextPageToken());
        } while (response.getNextPageToken() != null);

        return result;
    }

    public void waitForOperation(String selfLink, String zone) throws Exception {
        System.out.println("Waiting for operation completion...");
        Operation.Error error = blockUntilComplete(selfLink, zone, 120 * 1000);
        if (error == null) {
            System.out.println("Success!");
        } else {
            System.out.println(error.toPrettyString());
        }

    }

    public Instance getInstanceByName(String instanceName, String zone) throws IOException {

        Compute.Instances.List instances = compute.instances().list(project, zone);
        InstanceList list = instances.execute();
        if (list.getItems() == null) {
            throw new RuntimeException("Instance " + instanceName + " not found in " + zone + " zone");
        } else {
            for (com.google.api.services.compute.model.Instance instance : list.getItems()) {
                if (instance.getName().equals(instanceName))
                    return instance;
            }
        }
        throw new RuntimeException("Instance " + instanceName + " not found in " + zone + " zone");
    }

    public InstanceGroup getInstanceByGroupName(String groupName, String zone) throws IOException {

        Compute.InstanceGroups.List instances = compute.instanceGroups().list(project, zone);
        InstanceGroupList list = instances.execute();
        if (list.getItems() == null) {
            throw new RuntimeException("Group " + groupName + " not found in " + zone + " zone");
        } else {
            for (InstanceGroup instance : list.getItems()) {
                if (instance.getName().equals(groupName))
                    return instance;
            }
        }
        throw new RuntimeException("Group " + groupName + " not found in " + zone + " zone");
    }


    public List<String> getManagedInstancesSelfLinkByGroupName(String zone, String resourceId) throws IOException {

        Compute.InstanceGroupManagers.ListManagedInstances instances = compute.instanceGroupManagers().listManagedInstances(project, zone, resourceId);
        InstanceGroupManagersListManagedInstancesResponse list = instances.execute();
        List<String> managedInstanceSelfLink = Lists.newArrayList();
        if (list.getManagedInstances() == null) {
            throw new RuntimeException("No Instance running in Group " + resourceId + " in " + zone + " zone");
        } else {
            for (ManagedInstance instance : list.getManagedInstances()) {
                managedInstanceSelfLink.add(instance.getInstance());
            }
        }
        return managedInstanceSelfLink;
    }

    public String deleteInstance(String instanceName, String zone) throws IOException {
        Compute.Instances.Delete delete = compute.instances().delete(project, zone, instanceName);
        return delete.execute().getName();
    }


    /**
     * Wait until {@code operation} is completed.
     *
     * @param opId    the operation id returned by the original request
     * @param timeout the timeout, in millis
     * @return the error, if any, else {@code null} if there was no error
     * @throws InterruptedException if we timed out waiting for the operation to complete
     * @throws IOException          if we had trouble connecting
     */
    public Operation.Error blockUntilComplete(String opId, String zone, long timeout)
        throws Exception {
        long start = System.currentTimeMillis();
        Compute.ZoneOperations.Get init = compute.zoneOperations().get(project, zone, opId);
        Operation operation = getOperation(opId, zone);


        final long POLL_INTERVAL = 5 * 1000;

        String status = operation.getStatus();
        String opId2 = operation.getName();
        while (operation != null && !status.equals("DONE")) {
            Thread.sleep(POLL_INTERVAL);
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed >= timeout) {
                throw new InterruptedException("Timed out waiting for operation to complete");
            }
            System.out.println("waiting...");
            if (zone != null) {
                Compute.ZoneOperations.Get get = compute.zoneOperations().get(project, zone, opId);
                operation = get.execute();
            } else {
                Compute.GlobalOperations.Get get = compute.globalOperations().get(project, opId);
                operation = get.execute();
            }
            if (operation != null) {
                status = operation.getStatus();
            }
        }
        return operation == null ? null : operation.getError();
    }

    public Operation getOperation(String opId, String zone) throws IOException {
        Compute.ZoneOperations.Get init = compute.zoneOperations().get(project, zone, opId);
        return init.execute();
    }

    public boolean isOperationDone(String opId, String zone) throws IOException {
        return getOperation(opId, zone).getStatus().equals("DONE");
    }

    private static PrivateKey privateKeyFromPkcs8(String privateKeyPem) throws IOException {
        Reader reader = new StringReader(privateKeyPem);
        PemReader.Section section = PemReader.readFirstSectionAndClose(reader, "PRIVATE KEY");
        if (section == null) {
            throw new IOException("Invalid PKCS8 data.");
        }
        byte[] bytes = section.getBase64DecodedBytes();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        Exception unexpectedException = null;
        try {
            KeyFactory keyFactory = SecurityUtils.getRsaKeyFactory();
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            return privateKey;
        } catch (NoSuchAlgorithmException exception) {
            unexpectedException = exception;
        } catch (InvalidKeySpecException exception) {
            unexpectedException = exception;
        }
        throw new RuntimeException("Unexpected exception reading PKCS data", unexpectedException);
    }

    public List<String> getInstanceNames() throws IOException {
        Compute.Instances.AggregatedList request = this.compute.instances().aggregatedList(project);

        InstanceAggregatedList response = request.execute();
        if (response.getItems() == null) {
            return Collections.emptyList();
        }

        List<String> instanceNames = Lists.newArrayList();
        for (Map.Entry<String, InstancesScopedList> stringInstancesScopedListEntry : response.getItems().entrySet()) {
            List<Instance> instances = stringInstancesScopedListEntry.getValue().getInstances();
            if (instances == null)
                continue;
            for (Instance instance : instances) {
                instanceNames.add(instance.getName());
            }
        }
        return instanceNames;

    }

    public Instance getInstanceBySelfLink(String selfLink) throws URISyntaxException, IOException {
        SelfLink sl = new SelfLink(selfLink);
        return this.getInstanceByName(sl.getName(), sl.getZone());
    }

    public String deleteInstanceFromGroup(String groupName, String zone) throws IOException {
        return compute.instanceGroupManagers().delete(project, zone, groupName).execute().getName();
    }
}
