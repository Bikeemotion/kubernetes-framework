package com.bikeemotion.kubernetes;

public class Pod {

  private String IP;
  private String containerID;

  public String getIP() {
    return IP;
  }

  public Pod setIP(String IP) {
    this.IP = IP;
    return this;
  }

  public String getContainerID() {
    return containerID;
  }

  public Pod setContainerID(String containerID) {
    this.containerID = containerID;
    return this;
  }
}
