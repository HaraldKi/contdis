package de.pifpafpuf.contdis;

import org.junit.Test;

public class BasicTests {

  @Test(timeout=2000)
  public void serverStartStopTest() throws Exception {
    Server s = Server.create(31125, 1);

    Thread.sleep(900);
    s.shutdown();
  }
}
