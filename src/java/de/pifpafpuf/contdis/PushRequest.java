package de.pifpafpuf.contdis;

import java.io.Serializable;

public class PushRequest implements Serializable {
  private final Object data;
  private final String key;
  private long expiresMillis;
  
  public PushRequest(String key, Object data) {
    this.key = key;
    this.data = data;
    this.expiresMillis = Long.MAX_VALUE;
  }
  
  public Object getData() {
    return expired() ? null : data;
  }
  
  public String getKey() {
    return expired() ? null : key;
  }

  private boolean expired() {
    return System.currentTimeMillis()>expiresMillis;
  }
  
  void setTimeout(long millis) {
    expiresMillis = System.currentTimeMillis()+millis;
  }
  
  void setNeverExpire() {
    expiresMillis = Long.MAX_VALUE;
  }
  
  long getExpiresMillis() {
    return expiresMillis;
  }
}
