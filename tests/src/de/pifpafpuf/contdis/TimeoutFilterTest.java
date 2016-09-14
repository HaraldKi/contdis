package de.pifpafpuf.contdis;

import static org.junit.Assert.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class TimeoutFilterTest implements UncaughtExceptionHandler {
  private KeyedQueue queue;
  private volatile Throwable ex;
  
  @Override
  public void uncaughtException(Thread t, Throwable e) {
    ex = e;
    ex.printStackTrace();
  }

  @Before
  public void setup() {
    queue = new KeyedQueue(10);
    Thread.setDefaultUncaughtExceptionHandler(this);
    ex = null;
  }
  
  @Test
  public void basicTest() throws Exception {
    final int TIMEOUT = 80;
    TimeoutFilter tf = new TimeoutFilter(queue, TIMEOUT,  TimeUnit.MILLISECONDS);
    PushRequest pr = new PushRequest("key", "value");
    queue.put(pr);
    
    long start = System.currentTimeMillis();
    pr = tf.take(); // take element, and forget it
    PushRequest pr2 = tf.take();
    long delta = System.currentTimeMillis()-start;
    tf.ack(pr2.key);
    
    assertTrue(pr==pr2);
    assertEquals(0, queue.size());
    assertTrue(TIMEOUT<=delta);
    Thread.sleep(TIMEOUT+20);
    assertNull(ex);
  }

}
