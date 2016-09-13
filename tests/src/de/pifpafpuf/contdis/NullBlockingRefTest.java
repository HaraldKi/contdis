package de.pifpafpuf.contdis;

import static org.junit.Assert.*;

import java.util.concurrent.Semaphore;

import org.junit.Test;


public class NullBlockingRefTest {
  @Test(timeout=100)
  public void basicTest() throws InterruptedException {
    NullBlockingRef<String> nbr = new NullBlockingRef<>("a");
    assertEquals("a", nbr.get());
  }

  @Test
  public void waitForValueTest() throws InterruptedException {
    NullBlockingRef<String> nbr = new NullBlockingRef<>();
    Getter g = new Getter(nbr);
    new Thread(g).start();
    g.sem.acquire();
    Thread.sleep(100);
    nbr.set("x");
    assertEquals("x", nbr.get());
    g.sem.acquire();
    assertEquals("x", g.result);
    assertTrue("100<="+g.delay, 100<=g.delay);
  }


  public static final class Getter implements Runnable {
    private final NullBlockingRef<String> ref;
    public volatile Semaphore sem = new Semaphore(0);
    public volatile long delay = 0;
    public volatile String result = null;

    public Getter(NullBlockingRef<String> ref) {
      this.ref = ref;
    }

    @Override
    public void run() {
      long startTime = System.currentTimeMillis();
      sem.release();
      try {
        //System.out.println(startTime+": started");
        result = ref.get();
        //System.out.println(System.currentTimeMillis()+": got value");
      } catch (InterruptedException e) {
        // really ignore
      }
      delay = System.currentTimeMillis()-startTime;
      sem.release();
    }
  }
}
