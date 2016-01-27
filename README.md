Kubernetes Framework [![Build Status](https://travis-ci.org/Bikeemotion/kubernetes-framework.svg?branch=master)](https://travis-ci.org/Bikeemotion/kubernetes-framework) [![](https://raw.githubusercontent.com/novoda/novoda/master/assets/btn_apache_lisence.png)](LICENSE.txt)
====================================
An implementation of a java client for Kubernetes API

### Adding Dependency
```
 <dependency>
    <groupId>com.bikeemotion</groupId>
    <artifactId>kubernetes-framework</artifactId>
    <version>1.0.1</version>
</dependency>
```

# Testing it
#### Pre-requisites

* JDK 8 or newer
* Maven 3.1.0 or newer

#### Build
```
mvn clean install
```

### How to Use Kubernetes Framework
```
  // querying Kubernetes service
  Portal servicePortalIP = queryService(serviceId, defaultAddress, 5701);

  // querying Kubernetes pods
  List<Portal> queryPods(endpointName, defaultHost, defaultPort);

  // querying number replicas of a pod
  int queryReplicationControllerReplicas(final String replicationControllerName)
```
