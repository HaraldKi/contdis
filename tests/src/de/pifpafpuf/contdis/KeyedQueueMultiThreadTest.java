package de.pifpafpuf.contdis;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.Test;

public class KeyedQueueMultiThreadTest {
  private ConcurrentHashMap<String, Integer> result;
  private Random rand;
  private BlockingQueue<FailedInfo> failedInfos = new LinkedBlockingQueue<>();
  
  @Before
  public void setup() {
    rand = new Random(1122);
    result = new ConcurrentHashMap<>();
  }
  private long randSleep() {
    int decide = rand.nextInt(100);
    if (decide==0) {
      return rand.nextInt(100);
    } else {
      return rand.nextInt(10);
    }
  }
  private synchronized void save(String key, int value) 
      throws InterruptedException 
  {
    //System.out.println("saving "+key+", value="+value);
    Integer i = result.get(key);
    if (i!=null) {
      if (i>=value) {
        failedInfos.put(new FailedInfo(Thread.currentThread(), key, value, i));
      }
    }
    result.put(key, value);
  }

  @Test
  public void basicTest() throws Exception {
    KeyedQueue q = new KeyedQueue(10);
    int value = 0;
    List<Thread> threads = new LinkedList<>();
    for (int i=0; i<13; i++) {
      Thread t = new Thread(new Consumer(q, false));
      t.start();
      threads.add(t);
    }
    for (int i=0; i<10000; i++) {
      String key = Integer.toString(i%47);
      q.put(new PushRequest(key, value++));
    }
    for (Thread t : threads) {
      t.interrupt();
      t.join();
    }
    assertEquals(0, failedInfos.size());
  }

  @Test
  public void withFailTest() throws Exception {
    KeyedQueue q = new KeyedQueue(10);
    int value = 0;
    List<Thread> threads = new LinkedList<>();
    for (int i=0; i<13; i++) {
      Thread t = new Thread(new Consumer(q, true));
      t.start();
      threads.add(t);
    }
    for (int i=0; i<10000; i++) {
      String key = Integer.toString(i%47);
      q.put(new PushRequest(key, value++));
    }
    for (Thread t : threads) {
      t.interrupt();
      t.join();
    }
    for (FailedInfo fi : failedInfos) {
      System.out.println(fi);
    }

    assertEquals(0, failedInfos.size());    
  }

  private final class Consumer implements Runnable {
    private final KeyedQueue q;
    private final boolean mayfail;
    public Consumer(KeyedQueue q, boolean mayfail) {
      this.q = q;
      this.mayfail = mayfail;
    }
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          PushRequest pr = q.take();
          Thread.sleep(randSleep());
          if (mayfail && rand.nextInt(100)==0) {
            q.requeue(pr.key());
          } else {
            save(pr.key(), (Integer)pr.getData());
            q.ack(pr.key());
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }
  
  private static final class FailedInfo {
    public final String key;
    public final int value;
    public final int olderValue;
    public final Thread t;
    public FailedInfo(Thread t, String key, int value, int olderValue) {
      this.t = t;
      this.key = key;
      this.value = value;
      this.olderValue = olderValue;
    }
    
    @Override
    public String toString() {
      return "FailedInfo [key="+key+", value="+value+", olderValue="
          +olderValue+", t="+t+"]";
    }

  }
}
