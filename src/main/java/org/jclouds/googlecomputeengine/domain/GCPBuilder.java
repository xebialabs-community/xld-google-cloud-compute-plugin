/**
 * Copyright 2018 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.jclouds.googlecomputeengine.domain;

import com.google.common.collect.ImmutableList;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.googlecomputeengine.domain.NewInstance.create;

public class GCPBuilder {


    private String name;
    private URI machineType;
    private Boolean canIpForward;
    private List<NewInstance.NetworkInterface> networkInterfaces;
    private List<AttachDisk> disks;
    private String description;
    private Tags tags;
    private Metadata metadata;
    private List<Instance.ServiceAccount> serviceAccounts;
    private Instance.Scheduling scheduling;

    public GCPBuilder(String name, URI machineType, URI network, List<Instance.NetworkInterface.AccessConfig> accessConfigs, URI sourceImage) {
        checkNotNull(name, "NewInstance name cannot be null");
        this.name = name;
        this.machineType = machineType;
        this.networkInterfaces = ImmutableList.of(NewInstance.NetworkInterface.create(network, accessConfigs));
        this.disks = Arrays.asList(AttachDisk.newBootDisk(sourceImage));
    }


    public GCPBuilder canIpForward(Boolean canIpForward) {
        this.canIpForward = canIpForward;
        return this;
    }

    public GCPBuilder description(String description) {
        this.description = description;
        return this;
    }

    public GCPBuilder tags(Tags tags) {
        this.tags = tags;
        return this;
    }

    public GCPBuilder metadata(Metadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public GCPBuilder metadata(Map<String, String> metadata) {
        this.metadata = Metadata.create().putAll(metadata);
        return this;
    }

    /**
     * A list of service accounts, with their specified scopes, authorized for this instance.
     * Service accounts generate access tokens that can be accessed through the metadata server
     * and used to authenticate applications on the instance.
     * Note: to add scopes to the default service account on the VM you can use 'default' as
     * a keyword for email.
     */
    public GCPBuilder serviceAccounts(List<Instance.ServiceAccount> serviceAccounts) {
        this.serviceAccounts = serviceAccounts;
        return this;
    }

    public GCPBuilder scheduling(Instance.Scheduling scheduling) {
        this.scheduling = scheduling;
        return this;
    }

    public NewInstance build() {
        return create(name, machineType, canIpForward, networkInterfaces, disks, description,
            tags != null ? tags : Tags.create(),
            metadata != null ? metadata : Metadata.create(),
            serviceAccounts, scheduling);
    }
}

