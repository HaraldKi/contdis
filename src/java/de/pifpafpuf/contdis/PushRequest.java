package de.pifpafpuf.contdis;

import java.io.Serializable;

public class PushRequest implements Serializable {
  private final Object data;
  private final String key;
  
  public PushRequest(String key, Object data) {
    this.key = key;
    this.data = data;
  }
  
  public Object getData() {
    return data;
  }
  
  public String getKey() {
    return key;
  }

}
