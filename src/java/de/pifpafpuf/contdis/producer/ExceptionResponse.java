package de.pifpafpuf.contdis.producer;

import java.io.Serializable;

public class ExceptionResponse implements Serializable, PushResponse {
  // we do no send the exception, because the stacktrace may contain classes
  // not available on the client
  public final String stacktrace;
  public final String msg;
  public final String exceptionName;
  
  public ExceptionResponse(Throwable e) {
    this.msg = e.getMessage();
    this.stacktrace = renderTrace(e);
    this.exceptionName = e.getClass().getCanonicalName();
  }

  private String renderTrace(Throwable e) {
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement el : e.getStackTrace()) {
      sb.append(el.toString());
      sb.append('\n');
    }
    return sb.toString();
  }
  
  @Override
  public String toString() {
    return exceptionName+'\n'+msg+'\n'+stacktrace;
  }
}
