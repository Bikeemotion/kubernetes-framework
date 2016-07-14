/**
 * Copyright (C) Bikeemotion
 * 2014
 *
 * The reproduction, transmission or use of this document or its contents is not
 * permitted without express written authorization. All rights, including rights
 * created by patent grant or registration of a utility model or design, are
 * reserved. Modifications made to this document are restricted to authorized
 * personnel only. Technical specifications and features are binding only when
 * specifically and expressly agreed upon in a written contract.
 */
package com.bikeemotion.kubernetes;

import com.bikeemotion.kubernetes.api.resources.ResourceType;
import com.bikeemotion.kubernetes.api.resources.mappings.Endpoint;
import com.bikeemotion.kubernetes.api.resources.mappings.PodList;
import com.bikeemotion.kubernetes.api.resources.mappings.ReplicationController;
import com.bikeemotion.kubernetes.api.resources.mappings.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KubernetesDao {

  //members
  private static final Logger log = LoggerFactory.getLogger(KubernetesDao.class);

  private static final String SERVICE_ACCOUNT_TOKEN = "/var/run/secrets/kubernetes.io/serviceaccount/token";

  private static final HostnameVerifier trustAllHosts = (hostname, session) -> true;
  private static final TrustManager[] trustAll = new TrustManager[] {
      new X509TrustManager() {

        public void checkServerTrusted(X509Certificate[] certs, String authType) {

        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {

        }

        public X509Certificate[] getAcceptedIssuers() {

          return null;
        }
      }
  };

  //public API
  public static boolean isInKubernetes() {

    return getEnvOrDefault("KUBERNETES_SERVICE_HOST", "NONE").compareTo("NONE") != 0;
  }

  public static String getEnvOrDefault(final String variableName, final String defaultValue) {

    final String value = System.getenv(variableName);
    return (value == null || value.isEmpty())
        ? defaultValue
        : value;
  }

  public static Portal queryService(
      final String serviceName,
      final String defaultHost,
      int defaultPort) {

    // if the service isn't there or this is simply not running on k8s it wont have results
    if (queryK8sResource(ResourceType.SERVICE, Service.class, serviceName, Collections.EMPTY_MAP) != null) {

      return new Portal(
          String.format(
              "%s:%s",
              serviceName, // using DNS resolution. don't care about IPs
              defaultPort), // screw dynamic ports for now
          defaultHost,
          defaultPort);
    } else {

      return new Portal(null, defaultHost, defaultPort);
    }
  }

  public static List<Portal> queryPods(
      final String endpointName,
      final String defaultHost,
      final int defaultPort) {

    // endpoints have the POD's IPs
    final Endpoint value = queryK8sResource(ResourceType.ENDPOINT, Endpoint.class, endpointName, Collections.EMPTY_MAP);
    if (value != null && value.subsets != null) {

      final List<Portal> result = new ArrayList<>();
      value.subsets.stream().forEach(subset ->
          subset.addresses.stream().forEach(a ->
              result.add(new Portal(a.ip + ":" + defaultPort, defaultHost, defaultPort))));

      return result;
    } else {

      return Collections.EMPTY_LIST;
    }
  }

  public static AbstractMap.SimpleEntry<Integer, Integer> queryReadyPods(final String labelQuery) {

    // according to
    //    http://kubernetes.io/docs/api-reference/v1/operations/
    //    http://kubernetes.io/docs/user-guide/labels/

    final Map<String, String> queryParams = new HashMap<>();
    queryParams.put("labelSelector", labelQuery.replaceAll("=", "%3D"));

    final PodList podList = queryK8sResource(
        ResourceType.POD,
        PodList.class,
        "",
        queryParams);

    if (podList != null && podList.items.size() > 0) {
      int readyPods = (int) podList.items.stream()
          .flatMap(item -> item.status.containerStatuses.stream())
          .filter(containers -> containers.ready)
          .count();

      return new AbstractMap.SimpleEntry<>(readyPods, podList.items.size());
    } else {

      log.warn("The label query [{}] didn't return any results", labelQuery);
      return new AbstractMap.SimpleEntry<>(0, 0);
    }
  }

  public static int queryReplicationControllerReplicas(final String replicationControllerName) {

    final ReplicationController rc = queryK8sResource(
        ResourceType.REPLICATIONCONTROLLER,
        ReplicationController.class,
        replicationControllerName,
        Collections.EMPTY_MAP);

    if (rc != null) {

      return rc.status.replicas;
    } else {

      return 0;
    }
  }

  //internal API
  private static String getServiceAccountToken()
    throws IOException {

    return new String(Files.readAllBytes(Paths.get(SERVICE_ACCOUNT_TOKEN)));
  }

  private static <T> T queryK8sResource(
      final ResourceType resourceType,
      final Class<T> clazz,
      final String artifactName,
      final Map<String, String> queryParams) {

    T result = null;

    final String namespace = getEnvOrDefault("POD_NAMESPACE", "default");
    final String host = getEnvOrDefault("KUBERNETES_PORT_443_TCP_ADDR", "10.100.0.1");
    final String path = String.format(
        "https://%s/api/v1/namespaces/%s/%s/%s?%s",
        host,
        namespace,
        resourceType,
        artifactName,
        queryParams.keySet()
            .stream()
            .map((k) -> k + "=" + queryParams.get(k))
            .collect(Collectors.joining("&")));

    log.info("URL -> {}", path);

    try {

      final String token = getServiceAccountToken();
      final SSLContext ctx = SSLContext.getInstance("SSL");
      final URL url = new URL(path);
      final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

      ctx.init(null, trustAll, new SecureRandom());
      conn.setSSLSocketFactory(ctx.getSocketFactory());
      conn.setHostnameVerifier(trustAllHosts);
      conn.addRequestProperty("Authorization", "Bearer " + token);

      if (conn.getResponseCode() != 404) {

        result = new ObjectMapper().readValue(conn.getInputStream(), clazz);
      } else {

        log.warn("Couldn't find artifact [{}] on resource [{}]", artifactName, resourceType);
        result = null;
      }
    } catch (IOException | NoSuchAlgorithmException | KeyManagementException ex) {

//      if (log.isDebugEnabled()) {

        log.error("Request to Kubernetes API failed with the following exception", ex);
//      } else {
//        log.warn("Request to Kubernetes API failed");
//      }
    }

    return result;
  }
}
