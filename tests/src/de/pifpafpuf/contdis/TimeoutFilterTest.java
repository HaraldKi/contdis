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
  }

  @Before
  public void setup() {
    queue = new KeyedQueue(10);
    Thread.setDefaultUncaughtExceptionHandler(this);
    ex = null;
  }
  
  @Test
  public void basicTest() throws Exception {
    TimeoutFilter tf = new TimeoutFilter(queue, 80,  TimeUnit.MILLISECONDS);
    PushRequest pr = new PushRequest("key", "value");
    queue.put(pr);
    pr = tf.take();
    Thread.sleep(100);
    PushRequest pr2 = tf.take();
    tf.ack(pr.key);
    assertTrue(pr==pr2);
    assertEquals(0, queue.size());
    Thread.sleep(100);
    assertNull(ex);
  }

}
