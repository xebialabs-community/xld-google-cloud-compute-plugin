#
# Copyright 2018 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#


instance_name = deployed.instanceName if deployed.instanceName else deployed.name

context.addStepWithCheckpoint(steps.jython(
    description="Create instance {0} on {1}".format(instance_name, deployed.container.name),
    script="google/cloud/compute/instance/compute_create.py",
    order=80
), delta)

context.addStep(steps.jython(
    description="Wait for instance {} to be running".format(instance_name),
    script="google/cloud/compute/instance/compute_create_running.py",
    order=81
))

context.addStep(steps.wait(
    description="Wait for the ssh connection is available on {0}".format(instance_name),
    seconds=deployed.waitOnCreate,
    order=82
))
