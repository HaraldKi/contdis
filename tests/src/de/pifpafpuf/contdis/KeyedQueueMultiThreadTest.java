package de.pifpafpuf.contdis;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;

public class KeyedQueueMultiThreadTest {
  private ConcurrentHashMap<String, Integer> result;
  private Random rand;

  @Before
  public void setup() {
    rand = new Random(1122);
    result = new ConcurrentHashMap<>();
  }
  private long randSleep() {
    int decide = rand.nextInt(100);
    if (decide==0) {
      return rand.nextInt(1000);
    } else {
      return rand.nextInt(100);
    }
  }
  private synchronized void save(String key, int value) {
    //System.out.println("saving "+key+", value="+value);
    Integer i = result.get(key);
    if (i!=null) {
      assertTrue(value>i);
    }
    result.put(key, value);
  }

  @Test
  public void basicTest() throws Exception {
    KeyedQueue q = new KeyedQueue(10);
    int value = 0;
    List<Thread> threads = new LinkedList<>();
    for (int i=0; i<13; i++) {
      Thread t = new Thread(new Consumer(q));
      t.start();
      threads.add(t);
    }
    for (int i=0; i<1000; i++) {
      String key = Integer.toString(i%47);
      q.put(new PushRequest(key, value++));
    }
    for (Thread t : threads) {
      t.interrupt();
      t.join();
    }
  }

  private final class Consumer implements Runnable {
    private final KeyedQueue q;
    public Consumer(KeyedQueue q) {
      this.q = q;
    }
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          PushRequest pr = q.take();
          save(pr.key, (Integer)pr.data);
          Thread.sleep(randSleep());
          q.ack(pr.key);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }

  }
}
