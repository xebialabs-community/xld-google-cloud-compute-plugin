# XL Deploy Google Cloud Compute plugin

[![Build Status][xld-google-cloud-compute-plugin-travis-image] ][xld-google-cloud-compute-plugin-travis-url]
[![Codacy Badge][xld-google-cloud-compute-plugin-codacy-image] ][xld-google-cloud-compute-plugin-codacy-url]
[![Code Climate][xld-google-cloud-compute-plugin-code-climate-image] ][xld-google-cloud-compute-plugin-code-climate-url]
[![License: MIT][xld-google-cloud-compute-plugin-license-image] ][xld-google-cloud-compute-plugin-license-url]
[![Github All Releases][xld-google-cloud-compute-plugin-downloads-image] ]()

[xld-google-cloud-compute-plugin-travis-image]: https://travis-ci.org/xebialabs-community/xld-google-cloud-compute-plugin.svg?branch=master
[xld-google-cloud-compute-plugin-travis-url]: https://travis-ci.org/xebialabs-community/xld-google-cloud-compute-plugin
[xld-google-cloud-compute-plugin-codacy-image]: https://api.codacy.com/project/badge/Grade/db7f22096a014ff0974def7351b21d73    
[xld-google-cloud-compute-plugin-codacy-url]: https://www.codacy.com/app/ltutar/xld-google-cloud-compute-plugin
[xld-google-cloud-compute-plugin-code-climate-image]: https://codeclimate.com/github/ltutar/xld-google-cloud-compute-plugin/badges/gpa.svg
[xld-google-cloud-compute-plugin-code-climate-url]: https://codeclimate.com/github/ltutar/xld-google-cloud-compute-plugin
[xld-google-cloud-compute-plugin-license-image]: https://img.shields.io/badge/License-MIT-yellow.svg
[xld-google-cloud-compute-plugin-license-url]: https://opensource.org/licenses/MIT
[xld-google-cloud-compute-plugin-downloads-image]: https://img.shields.io/github/downloads/xebialabs-community/xld-google-cloud-compute-plugin/total.svg

## Preface

This document describes the functionality provided by the Google Cloud Compute plugin

See the [XL Deploy reference manual](https://docs.xebialabs.com/xl-deploy) for background information on XL Deploy and deployment automation concepts.  

## Overview

The plugin is a XL Deploy plugin that adds capability for provision instance into Google Cloud.

## Requirements

* **Requirements**
	* **XL Deploy** 7.0.1+

## Installation

* Copy the latest JAR file from the [releases page](https://github.com/xebialabs-community/xld-google-cloud-compute-plugin/releases) into the `XL_DEPLOY_SERVER/plugins` directory.
* Restart the XL Deploy server.


## Usage

1. Go to `Repository - Infrastructure`, create a new `google.AccountCloud` and fill in the properties. You may use the 'Import JSON Control task to help you'
2. Use the 'Check Connection' Control task to valide the parameters.
3. Create an environment under `Repository - Environments` and add the corresponding `google.AccountCloud` as container.

## Deployables ##

The plugin supports one deployable:

1. Create an provisioning package `udm.ProvisioningPackage` with `googlecloud.compute.InstanceSpec` as deployables. 
2. Add templates https://docs.xebialabs.com/xl-deploy/how-to/use-provisioning-outputs.html
3. Start deploying.

