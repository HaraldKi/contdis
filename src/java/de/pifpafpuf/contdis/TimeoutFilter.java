package de.pifpafpuf.contdis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TimeoutFilter
    implements KeyedQueueConsumer
{
  private final KeyedQueue queue;
  private final long timeoutms;
  private final Map<String, Elem> waiting = new HashMap<>();
  private Elem head = null;
  private Elem tail = null;
  private Semaphore count = new Semaphore(0);
    
  public TimeoutFilter(KeyedQueue queue, long timeout, TimeUnit u) {
    this.queue = queue;
    this.timeoutms = u.toMillis(timeout);
  }

  @Override
  public PushRequest take() throws InterruptedException {
    return poll(Integer.MAX_VALUE, TimeUnit.DAYS);
  }

  @Override
  public PushRequest poll(long timeout, TimeUnit u)
    throws InterruptedException
  {
    PushRequest result = queue.poll(timeout, u);
    schedule(result.key);
    return result;
  }

  @Override
  public void ack(String key) throws InterruptedException {
    deschedule(key);
    queue.ack(key);
  }

  @Override
  public void requeue(String key) throws InterruptedException {
    deschedule(key);
    queue.requeue(key);
  }
  
  private synchronized void schedule(String key) {
    Elem e = new Elem(key, timeoutms);
    if (tail==null) {
      head = e;
      tail = e;
    } else {
      tail.right = e;
    }
    waiting.put(key, e);
  }
  
  private synchronized void deschedule(String key) {
    Elem e = waiting.remove(key);
    if (e==null) {
      throw new IllegalArgumentException("key "+key+" was never handed out");
    }
    if (e.left==null) {
      head = e.right;
    } else {
      e.left.right = e.right;
    }
    if (e.right==null) {
      tail = e.left;
    } else {
      e.right.left = e.left;
    }
  }
  private void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        count.acquire();
        XXX don't use acquire, decrement in deschedule
        careful, but the element we are waiting for may have been removed meanwyile
        
        long delta = head.
            
            
        Thread.sleep(
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
      
    }
  }
  private static final class Elem {
    public Elem left = null;
    public Elem right = null;
    public final String key;
    public final long expires;
    public Elem(String key, long timeout) {
      this.key = key;
      this.expires = System.currentTimeMillis()+timeout;
    }
  }
}
