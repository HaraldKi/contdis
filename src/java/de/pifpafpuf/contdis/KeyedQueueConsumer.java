package de.pifpafpuf.contdis;

import java.util.concurrent.TimeUnit;

public interface KeyedQueueConsumer {
  PushRequest take() throws InterruptedException;
  PushRequest poll(long timeout, TimeUnit u) throws InterruptedException;
  void ack(String key) throws InterruptedException;
  void requeue(String key) throws InterruptedException;
}
