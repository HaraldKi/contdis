package de.pifpafpuf.contdis;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;


public class NullBlockingRefTest {
  @Test(timeout=100)
  public void basicTest() throws InterruptedException {
    NullBlockingRef<String> nbr = new NullBlockingRef<>("a");
    assertEquals("a", nbr.get());
  }
  
  @Test
  public void waitForValueTest() {
    NullBlockingRef<String> nbr = new NullBlockingRef<>();
    new Thread(new Getter(nbr)).start();
    Thread.sleep(100);
    nbr.set("x");
    assertEquals(
    
  }
  
  
  public static final class Getter implements Runnable {
    private final NullBlockingRef<String> ref;
    public long delay = 0;
    public String result = null;
    
    public Getter(NullBlockingRef<String> ref) {
      this.ref = ref;
    }

    @Override 
    public void run() {
      long now = System.currentTimeMillis();
      try {
        result = ref.get();
      } catch (InterruptedException e) {
        // really ignore
      }
      delay = System.currentTimeMillis()-now;
    }
    
  }
}
