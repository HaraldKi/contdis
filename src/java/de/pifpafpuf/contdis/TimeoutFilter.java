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
    result.setTimeout(timeoutms);
    schedule(result);
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
      Elem e = deschedule(key);
      if (e!=null) {
        queue.requeue(key);
      }
    }
  }

  private synchronized void schedule(PushRequest req) {
    Elem e = new Elem(req);
    req.setTimeout(timeoutms);
    if (tail==null) {
      head.set(e);
    } else {
      tail.right = e;
      e.left = tail;
    }
    tail = e;
    waiting.put(req.key(), e);
  }

  private synchronized Elem deschedule(String key) {
    Elem e = waiting.remove(key);
    if (e!=null) {
      unlink(e);
    }
    return e;
  }

  private void unlink(Elem e) {
    //System.out.println("unlinking "+e);
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
        long expires = candidate.req.getExpiresMillis();
        long delta = Math.max(50, expires-System.currentTimeMillis());
        Thread.sleep(delta);
        //System.out.println("requeuing "+candidate);
        requeue(candidate.req.key());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }

    }
  }

  private static final class Elem {
    public Elem left = null;
    public Elem right = null;
    public final PushRequest req;
    // public final long expires; XXX use the one from pushrequest
    public Elem(PushRequest req) {
      this.req = req;
    }
    @Override
    public String toString() {
      return "Elem[key="+req.key()+", expires="+req.getExpiresMillis()+"]";
    }
  }
}
