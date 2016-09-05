package de.pifpafpuf.contdis;

import java.io.Serializable;

public class PushRequest implements Serializable {
  public final Object data;
  public final String key;
  
  public PushRequest(String key, Object data) {
    this.key = key;
    this.data = data;
  }
  
}
