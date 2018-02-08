== SSH

ssh-keygen -t rsa -f ~/.ssh/xld-google-cloud-compute -C root

== GGC

zone: europe-west1-b
service account: "email": "929779550645-compute@developer.gserviceaccount.com",


gcloud auth login   
gcloud config set project just-terminus-194507

pip3 install google-api-python-client google-auth-httplib2

metadata vs tag vs labels


POST https://www.googleapis.com/compute/v1/projects/just-terminus-194507/zones/europe-west1-b/instances
{
  "name": "instance-1",
  "zone": "projects/just-terminus-194507/zones/europe-west1-b",
  "minCpuPlatform": "Automatic",
  "machineType": "projects/just-terminus-194507/zones/europe-west1-b/machineTypes/custom-1-2048",
  "metadata": {
    "items": [
      {
        "key": "metadata_1",
        "value": "value_of_metadata_1"
      },
      {
        "key": "startup-script",
        "value": "echo \"benoit\" > /tmp/benoit.txt"
      }
    ]
  },
  "tags": {
    "items": [
      "http-server",
      "https-server"
    ]
  },
  "disks": [
    {
      "type": "PERSISTENT",
      "boot": true,
      "mode": "READ_WRITE",
      "autoDelete": true,
      "deviceName": "instance-1",
      "initializeParams": {
        "sourceImage": "projects/ubuntu-os-cloud/global/images/ubuntu-1710-artful-v20180126",
        "diskType": "projects/just-terminus-194507/zones/europe-west1-b/diskTypes/pd-standard",
        "diskSizeGb": "10"
      }
    }
  ],
  "canIpForward": false,
  "networkInterfaces": [
    {
      "subnetwork": "projects/just-terminus-194507/regions/europe-west1/subnetworks/default",
      "accessConfigs": [
        {
          "name": "External NAT",
          "type": "ONE_TO_ONE_NAT"
        }
      ],
      "aliasIpRanges": []
    }
  ],
  "description": "",
  "labels": {
    "nom": "moussaud"
  },
  "scheduling": {
    "preemptible": false,
    "onHostMaintenance": "MIGRATE",
    "automaticRestart": true
  },
  "deletionProtection": false,
  "serviceAccounts": [
    {
      "email": "929779550645-compute@developer.gserviceaccount.com",
      "scopes": [
        "https://www.googleapis.com/auth/devstorage.read_only",
        "https://www.googleapis.com/auth/logging.write",
        "https://www.googleapis.com/auth/monitoring.write",
        "https://www.googleapis.com/auth/servicecontrol",
        "https://www.googleapis.com/auth/service.management.readonly",
        "https://www.googleapis.com/auth/trace.append"
      ]
    }
  ]
}

POST https://www.googleapis.com/compute/v1/projects/just-terminus-194507/global/firewalls
{
  "name": "default-allow-http",
  "kind": "compute#firewall",
  "sourceRanges": [
    "0.0.0.0/0"
  ],
  "targetTags": [
    "http-server"
  ],
  "allowed": [
    {
      "IPProtocol": "tcp",
      "ports": [
        "80"
      ]
    }
  ],
  "network": "projects/just-terminus-194507/global/networks/default"
}

POST https://www.googleapis.com/compute/v1/projects/just-terminus-194507/global/firewalls
{
  "name": "default-allow-https",
  "kind": "compute#firewall",
  "sourceRanges": [
    "0.0.0.0/0"
  ],
  "targetTags": [
    "https-server"
  ],
  "allowed": [
    {
      "IPProtocol": "tcp",
      "ports": [
        "443"
      ]
    }
  ],
  "network": "projects/just-terminus-194507/global/networks/default"
}