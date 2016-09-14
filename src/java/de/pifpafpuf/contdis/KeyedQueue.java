package de.pifpafpuf.contdis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class KeyedQueue implements KeyedQueueConsumer {
  private final BlockingQueue<String> keyQueue;
  private final Map<String, RequestStatus> data;
  private final Semaphore gate;
  private final int capacity;
  
  public KeyedQueue(int capacity) {
    this.capacity = capacity;
    this.keyQueue = new LinkedBlockingQueue<>();
    this.data = new HashMap<>(4*capacity/3);
    this.gate = new Semaphore(capacity);
  }

  public int capacity() {
    return capacity;
  }
  
  public int size() {
    return capacity-gate.availablePermits();
  }
  
  public void put(PushRequest req) throws InterruptedException {
    offer(req, Integer.MAX_VALUE, TimeUnit.DAYS);
  }
  
  public boolean offer(PushRequest req, long timeout,
                       TimeUnit u) throws InterruptedException {
    synchronized(this) {
      RequestStatus rstat = data.get(req.getKey());
      if (rstat!=null && rstat.waiting!=null) {
        rstat.waiting = req;
        return true;
      }
    }
    if (!gate.tryAcquire(timeout, u)) {
      return false;
    }
    synchronized(this) {
      RequestStatus rstat = data.get(req.getKey());
      if (rstat==null) {
        RequestStatus rst = new RequestStatus(req);
        keyQueue.put(req.getKey());
        data.put(req.getKey(), rst);
      } else {
        rstat.waiting = req;
      }
    }
    return true;
  }

  public PushRequest take() throws InterruptedException {
    return poll(Integer.MAX_VALUE, TimeUnit.DAYS);
  }
  
  public PushRequest poll(long timeout, TimeUnit u) throws InterruptedException {
    String key = keyQueue.poll(timeout, u);
    if (key==null) {
      return null;
    }
    synchronized(this) {
      RequestStatus rst = data.get(key);
      if (rst.head==null) {
        rst.head = rst.waiting;
        rst.waiting = null;
      }
      rst.inflight = true;
      return rst.head;
    }
  }

  public synchronized void ack(String key) throws InterruptedException {
    RequestStatus rst = data.get(key);
    verifyAck(key, rst, "ackknowledged");
    if (rst.waiting==null) {
      data.remove(key);
    } else {
      rst.head = null;
      keyQueue.put(rst.waiting.getKey());
    }
    gate.release();
  }

  protected void verifyAck(String key, RequestStatus rst, String what)
    throws IllegalArgumentException
  {
    if (rst==null || rst.head==null || !rst.inflight) {
      System.out.println(rst);
      throw new IllegalArgumentException("data for key `"+key
                                         +"' was not handed out, so it"
                                         +" cannot be "+what);
    }
  }
  
  public synchronized void requeue(String key) throws InterruptedException {
    RequestStatus rst = data.get(key);
    verifyAck(key, rst, "requeued");
    keyQueue.put(rst.head.getKey());
    rst.inflight = false;
  }
  /*+******************************************************************/
  private static final class RequestStatus {
    PushRequest waiting;
    PushRequest head = null;
    boolean inflight = false;
    public RequestStatus(PushRequest req) {
      this.waiting = req;
    }
    @Override
    public String toString() {
      return "RequestStatus [waiting="+waiting+", head="+head+", inflight="
          +inflight+"]";
    }
  }
}
