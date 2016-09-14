package de.pifpafpuf.contdis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeoutFilter
    implements KeyedQueueConsumer, Runnable
{
  private final KeyedQueue queue;
  private final long timeoutms;
  private final Map<String, Elem> waiting = new HashMap<>();
  private NullBlockingRef<Elem> head = new NullBlockingRef<>();
  private Elem tail = null;
  private final Thread timeOuter;

  public TimeoutFilter(KeyedQueue queue, long timeout, TimeUnit u) {
    this.queue = queue;
    this.timeoutms = u.toMillis(timeout);
    timeOuter = new Thread(this);
    timeOuter.setDaemon(true);
    timeOuter.start();
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
    synchronized(this) {
      Elem e = waiting.remove(key);
      if (e==null) {
        return;
      }
      queue.requeue(key);
    }
  }

  private synchronized void schedule(String key) {
    Elem e = new Elem(key, timeoutms);
    if (tail==null) {
      head.set(e);
    } else {
      tail.right = e;
      e.left = tail;
    }
    tail = e;
    waiting.put(key, e);
  }

  private synchronized void deschedule(String key) {
    Elem e = waiting.remove(key);
    if (e!=null) {
      unlink(e);
    }
  }

  private void unlink(Elem e) {
    if (e.left==null) {
      head.set(e.right);
    } else {
      e.left.right = e.right;
    }
    if (e.right==null) {
      tail = e.left;
    } else {
      e.right.left = e.left;
    }
  }

  @Override
  public void run() {
    //System.out.println("timouter started");
    while (!Thread.currentThread().isInterrupted()) {
      try {
        Elem candidate = head.get();
        //System.out.println("got candidate "+candidate);
        long delta = Math.max(0, candidate.expires-System.currentTimeMillis());
        Thread.sleep(delta);
        //System.out.println("descheduling "+candidate);
        requeue(candidate.key);
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
    @Override
    public String toString() {
      return "Elem [key="+key+", expires="+expires+"]";
    }
  }
}
