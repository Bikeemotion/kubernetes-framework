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

public class Portal {

  // members
  private final String fullURL;
  private final String defaultHost;
  private final Integer defaultPort;

  // getters
  public String getHost() {

    return fullURL == null || fullURL.isEmpty()
        ? defaultHost
        : fullURL.substring(0, fullURL.indexOf(':'));
  }

  public Integer getPort() {

    return fullURL == null || fullURL.isEmpty()
        ? defaultPort
        : Integer.parseInt(fullURL.substring(fullURL.indexOf(':') + 1, fullURL.length()));
  }

  // public API
  public Portal(final String fullURL, final String defaultHost, int defaultPort) {

    this.fullURL = fullURL;
    this.defaultHost = defaultHost;
    this.defaultPort = defaultPort;
  }

  @Override
  public String toString() {

    return getHost() + ":" + getPort();
  }
}
