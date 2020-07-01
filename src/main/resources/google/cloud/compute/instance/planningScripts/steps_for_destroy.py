#
# Copyright 2020 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

instance_name = previousDeployed.instanceName if previousDeployed.instanceName else previousDeployed.name

context.addStepWithCheckpoint(steps.jython(
    description='Destroy instance {} on {}'.format(instance_name, previousDeployed.container.name),
    script="google/cloud/compute/instance/compute_destroy.py",
    order=16
), delta)

context.addStep(steps.jython(
    description="Wait for instance {} to be fully destroy".format(instance_name),
    script="google/cloud/compute/instance/compute_destroy_running.py",
    order=17
))

context.addStep(steps.wait(
    description="Wait for instance {} to be fully destroyed (2)".format(instance_name),
    seconds=previousDeployed.waitOnDestroy,
    order=18
))
