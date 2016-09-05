package de.pifpafpuf.contdis.producer;

public class IllegalResponseException extends Exception {
  public IllegalResponseException(String msg, Throwable cause) {
    super(msg);
    initCause(cause);
  }
  public IllegalResponseException(String msg) {
    super(msg);
  }
}
