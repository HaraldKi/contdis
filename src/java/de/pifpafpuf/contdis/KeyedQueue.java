package de.pifpafpuf.contdis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class KeyedQueue {
  private final BlockingQueue<String> keyQueue;
  private final Map<String, RequestStatus> data;
  private final Semaphore gate;

  public KeyedQueue(int maxInflight) {
    this.keyQueue = new LinkedBlockingQueue<>();
    this.data = new HashMap<>(4*maxInflight/3);
    this.gate = new Semaphore(maxInflight);
  }

  public synchronized void put(PushRequest req) throws InterruptedException {
    gate.acquire();
    RequestStatus rstat = data.get(req.key);
    if (rstat==null) {
      RequestStatus rst = new RequestStatus(req);
      keyQueue.put(req.key);
      data.put(req.key, rst);
    } else {
      rstat.waiting = req;
    }
  }

  public synchronized PushRequest take() throws InterruptedException {
    String key = keyQueue.take();
    RequestStatus rst = data.get(key);
    rst.inflight = rst.waiting;
    rst.waiting = null;
    return rst.inflight;
  }

  public synchronized void ack(String key) throws InterruptedException {
    RequestStatus rst = data.get(key);
    if (rst==null) {
      throw new IllegalArgumentException("data for key "+key+" was not handed "
          + "out, so it cannot be acknowledged");
    }
    if (rst.waiting==null) {
      data.remove(key);
    } else {
      rst.inflight = null;
      keyQueue.put(rst.waiting.key);
    }
    gate.release();
  }
  
  XXXXXXXXXXXXXXXXXXXXXXXXXX  inflight vs waiting handling not correct yet
  
  public synchronized void fail(String key) throws InterruptedException {
    RequestStatus rst = data.get(key);
    if (rst==null) {
      throw new IllegalArgumentException("data for key "+key+" was not handed "
          + "out, so it cannot be failed");
    }
    keyQueue.put(rst.inflight.key);
  }
  /*+******************************************************************/
  private static final class RequestStatus {
    PushRequest waiting;
    PushRequest inflight = null;
    public RequestStatus(PushRequest req) {
      this.waiting = req;
    }
  }
}
