package de.pifpafpuf.contdis;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class KeyedQueueTest {

  @Test
  public void aTest() {
    KeyedQueue kQueue = new KeyedQueue(11);
    assertEquals(11, kQueue.capacity());
  }
  @Test
  public void oneElemTest() throws InterruptedException {
    KeyedQueue kQueue = new KeyedQueue(10);
    PushRequest pr = new PushRequest("key1", "data");
    kQueue.put(pr);
    PushRequest result = kQueue.take();
    assertTrue(pr==result);
    assertEquals(1, kQueue.size());
    kQueue.ack("key1");
    assertEquals(0, kQueue.size());
  }

  @Test
  public void oneElemOrderTest() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(10);
    for (int i=0; i<10; i++) {
      kQueue.put(new PushRequest("key", i));
    }
    PushRequest pr = kQueue.take();
    assertEquals("key", pr.key);
    assertEquals(9, pr.data);
  }
  
  @Test
  public void oneElemOrderTakeTest() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(10);
    PushRequest fifth = null;
    for (int i=0; i<10; i++) {
      kQueue.put(new PushRequest("key", i));
      if (i==5) {
        fifth = kQueue.take();
        kQueue.ack("key");
      }
    }
    assertEquals(5, fifth.data);
    PushRequest pr = kQueue.take();
    assertEquals("key", pr.key);
    assertEquals(9, pr.data);
    
  }
  @Test
  public void testReplay() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(10);
    kQueue.put(new PushRequest("key", "delayed"));
    PushRequest pr = kQueue.take();
    kQueue.requeue(pr.key);
    pr = kQueue.take();
    assertEquals("delayed", pr.data);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void ackUnknownKey() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(10);
    kQueue.ack("blabla");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void ackUnprocessed() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(10);
    kQueue.put(new PushRequest("key", "Data"));
    kQueue.ack("key");
  }
  
  @Test
  public void getNullFromEmpty() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(10);
    PushRequest pr = kQueue.poll(1, TimeUnit.MILLISECONDS);
    assertEquals(null, pr);
  }
  
  @Test
  public void pollTwiceTest() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(10);
    kQueue.put(new PushRequest("key", "data"));
    PushRequest pr = kQueue.poll(1, TimeUnit.SECONDS);
    kQueue.put(new PushRequest("key", "data"));
    pr = kQueue.poll(10, TimeUnit.MILLISECONDS);
    assertEquals(null, pr);
  }
  @Test
  public void ackAll() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(10);
    for (int i=0; i<10; i++) {
      kQueue.put(new PushRequest("key", i));
      PushRequest pr = kQueue.poll(1, TimeUnit.SECONDS);
      assertEquals(i, pr.data);
      kQueue.ack("key");
    }
  }

  @Test
  public void ackOdd() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(10);
    kQueue.put(new PushRequest("key", 0));
    PushRequest pr = kQueue.poll(1, TimeUnit.SECONDS);
    assertEquals(0, pr.data);
    kQueue.put(new PushRequest("key", 1));
    kQueue.ack("key");
    pr = kQueue.poll(1, TimeUnit.SECONDS);
    assertEquals(1, pr.data);
    
    pr = kQueue.poll(1, TimeUnit.NANOSECONDS);
    assertEquals(null, pr);
  }
  
  @Test
  public void queueFullTest() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(1);
    kQueue.put(new PushRequest("key", 0));
    boolean putOk = 
        kQueue.offer(new PushRequest("otherkey", 0), 1, TimeUnit.NANOSECONDS);
    assertFalse(putOk);
  }

  @Test
  public void queueOverwriteSameTest() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(1);
    kQueue.put(new PushRequest("key", 0));
    boolean putOk = 
        kQueue.offer(new PushRequest("key", 0), 1, TimeUnit.NANOSECONDS);
    assertTrue(putOk);
  }

  @Test
  public void queueNoOverwriteInflightTest() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(1);
    kQueue.put(new PushRequest("key", 0));
    PushRequest pr = kQueue.take();
    boolean putOk = 
        kQueue.offer(new PushRequest("key", 0), 1, TimeUnit.NANOSECONDS);
    assertFalse(putOk);

    kQueue.ack("key");
    putOk = kQueue.offer(pr, 1, TimeUnit.NANOSECONDS);
    assertTrue(putOk);
  }
  
  @Test
  public void doubleRequeueTest() throws Exception {
    KeyedQueue kQueue = new KeyedQueue(1);
    kQueue.put(new PushRequest("key", 0));
    PushRequest pr = kQueue.take();
    kQueue.requeue("key");
    kQueue.requeue("key");
    fail("this should fail, or how to treat a double rqueue correctly?");
  }
}
