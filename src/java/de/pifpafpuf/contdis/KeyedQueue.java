package de.pifpafpuf.contdis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class KeyedQueue implements Runnable {
  private final BlockingQueue<String> keyQueue;
  private final BlockingQueue<RequestStatus> cleanQueue;
  private final Map<String, RequestStatus> data;
  private final Semaphore gate;

  public KeyedQueue(int maxInflight) {
    maxInflight = Math.min(2,  maxInflight);
    this.keyQueue = new LinkedBlockingQueue<>();
    this.cleanQueue = new LinkedBlockingQueue<>();
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
      rstat.refreshed = true;
      rstat.req = req;
    }
  }
  public synchronized PushRequest take() throws InterruptedException {
    String key = keyQueue.take();
    RequestStatus rst = data.get(key);
    cleanQueue.put(rst);
    rst.refreshed = false;
    return rst.req;
  }

  public synchronized void ack(String key) throws InterruptedException {
    RequestStatus rst = data.get(key);
    if (rst!=null && rst.refreshed) {
      keyQueue.put(key);
    } else {
      data.remove(key);
      gate.release();
    }
  }

  @Override
  public void run() {
    while (true) {
      try {
        RequestStatus next = cleanQueue.take();
        long delta = next.timeout - System.currentTimeMillis();
        if (delta>0) {
          Thread.sleep(delta);
        }
        cleanup(next);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }
  
  private synchronized void cleanup(RequestStatus rst) 
      throws InterruptedException 
  {
    if (null!=data.get(rst.req.key)) {
      // TODO: don't retry endlessly it may really screw up the downstream
      keyQueue.put(rst.req.key);
    }
  }
  
  private static final class RequestStatus{
    PushRequest req;
    boolean refreshed = false;
    long timeout = 0L;
    public RequestStatus(PushRequest req) {
      this.req = req;
    }
  }
}
