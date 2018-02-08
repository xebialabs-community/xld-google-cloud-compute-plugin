import googleapiclient.discovery
import os

import time


class GoogleCompute:

    def __init__(self, json_file_path):
        os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = json_file_path
        self.compute = googleapiclient.discovery.build('compute', 'v1')

    def create_instance(self, project, zone, name):
        # Get the latest Debian Jessie image.
        image_response = self.compute.images().getFromFamily(
            project='debian-cloud', family='debian-8').execute()
        source_disk_image = image_response['selfLink']

        # Configure the machine
        machine_type = "zones/{}/machineTypes/n1-standard-1".format(zone)

        config = {
            'name': name,
            'machineType': machine_type,

            # Specify the boot disk and the image to use as a source.
            'disks': [
                {
                    'boot': True,
                    'autoDelete': True,
                    'initializeParams': {
                        'sourceImage': source_disk_image,
                    }
                }
            ],

            # Specify a network interface with NAT to access the public
            # internet.
            'networkInterfaces': [{
                'network': 'global/networks/default',
                'accessConfigs': [
                    {'type': 'ONE_TO_ONE_NAT', 'name': 'External NAT'}
                ]
            }],

            # Allow the instance to access cloud storage and logging.
            'serviceAccounts': [{
                'email': 'default',
                'scopes': [
                    'https://www.googleapis.com/auth/devstorage.read_write',
                    'https://www.googleapis.com/auth/logging.write'
                ]
            }],

            # Metadata is readable from the instance and allows you to
            # pass configuration from deployment scripts to instances.
            'metadata': {
                'items': [{
                    'key': 'benoit',
                    'value': 'moussaud'
                }, {
                    'key': 'xldeploy',
                    'value': '7.6'
                }]
            }
        }

        response = self.compute.instances().insert(
            project=project,
            zone=zone,
            body=config).execute()

        return response

    def wait_for_operation(self, project, zone, operation):
        print('Waiting for operation to finish...')
        while True:
            result = self.compute.zoneOperations().get(
                project=project,
                zone=zone,
                operation=operation).execute()

            # print(result)

            if result['status'] == 'DONE':
                print("done.")
                if 'error' in result:
                    raise Exception(result['error'])
                return result

            time.sleep(1)

    def get_compute_instance(self, project, zone, instance):
        return self.compute.instances().get(project=project, zone=zone, instance=instance).execute()

    def list(self, project, zone):
        return self.compute.instances().list(project=project, zone=zone).execute()

    @staticmethod
    def get_external_ip(gc_compute_instance_info):
        # print(gc_compute_instance_info)
        network_interfaces = gc_compute_instance_info['networkInterfaces']
        network_interface = network_interfaces[0]
        access_configs = network_interface['accessConfigs']
        access_config = access_configs[0]
        return access_config['natIP']

    def delete_instance(self, project, zone, instance):
        return self.compute.instances().delete(
            project=project,
            zone=zone,
            instance=instance).execute()


if __name__ == '__main__':
    json = "/Users/bmoussaud/.ssh/MyFirstProject--468c2c49e42d.json"
    gc = GoogleCompute(json)
    project_id = 'just-terminus-194507'
    zone = 'europe-west1-b'
    instance_name = 'benoit3-instance'
    if 1 == 0:
        print('ask to create instance'.format(instance_name))
        response = gc.create_instance(project_id, zone, instance_name)
        print(response)
        final_result = gc.wait_for_operation(project_id, zone, response['name'])
        print(final_result)

    instance_info = gc.get_compute_instance(project_id, zone, instance_name)
    print("External IP Address is {}".format(gc.get_external_ip(instance_info)))
    response = gc.delete_instance(project_id, zone, instance_name)
    gc.wait_for_operation(project_id, zone, response['name'])

    print('done')
