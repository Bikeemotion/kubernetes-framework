Kubernetes Framework [![Build Status](https://travis-ci.org/Bikeemotion/kubernetes-framework.svg?branch=master)](https://travis-ci.org/Bikeemotion/kubernetes-framework) [![](https://raw.githubusercontent.com/novoda/novoda/master/assets/btn_apache_lisence.png)](LICENSE.txt)
====================================
An implementation of a very simple java client for Kubernetes API

## How to Use

### Adding Dependency
```
 <dependency>
    <groupId>com.bikeemotion</groupId>
    <artifactId>kubernetes-framework</artifactId>
    <version>1.0.8</version>
</dependency>
```

### Available methods
```
 static Portal queryService(String serviceName, String defaultHost, int defaultPort);
 
 static Set<Pod> queryPods(String key, final String value);
 
 static int queryReplicationControllerReplicas(String replicationControllerName);
 
 static Map<String,String> queryConfigMapData(String configMapName);
```

# Testing it
#### Pre-requisites

* JDK 8 or newer
* Maven 3.1.0 or newer

#### Build
```
mvn clean install
```


