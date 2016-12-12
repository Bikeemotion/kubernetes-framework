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
package com.bikeemotion.kubernetes.api.resources.mappings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pod {

  // nested types
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ContainerStatus {

    public boolean ready;
    public String name;
    public int restartCount;
    public String containerID;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Status {

    public List<ContainerStatus> containerStatuses;
    public String podIP;
  }

  public Status status;
}
