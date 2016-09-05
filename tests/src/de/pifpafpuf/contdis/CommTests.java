package de.pifpafpuf.contdis;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.pifpafpuf.contdis.producer.Client;
import de.pifpafpuf.contdis.producer.ExceptionResponse;
import de.pifpafpuf.contdis.producer.ResponseOk;

public class CommTests {
  private static int port = 31234;
  private Server server;

  @Before
  public void setup() throws Exception {
    server = Server.create(port, 1);
    server.start();
  }
  @After
  public void teardown() throws Exception {
    //Thread.sleep(1000);
    server.shutdown();
    //port += 1;
  }

  @Test
  public void basicTest() throws Exception {
    Client c = Client.connect("localhost", port);
    Object response = c.send(new PushRequest("key", "value"));
    assertEquals(ResponseOk.class, response.getClass());
  }

  @Test
  public void errorResponseTest() throws Exception {
    Client c = Client.connect("localhost", port);
    Object response = c.sendAny("this is a non-expected message");
    //System.out.println(response);
    assertEquals(ExceptionResponse.class, response.getClass());
    String t = "unexpected type";
    assertTrue(t, ((ExceptionResponse)response).msg.contains(t));
  }

  @Test
  public void clientHandleBrokenResponseTest() throws Exception {
    server.setFixedReturnMessage("blabla");
    Client c = Client.connect("localhost", port);
    Object response = c.sendAny(new PushRequest("key", "value"));
    System.out.println(response);
    assertEquals(ExceptionResponse.class, response.getClass());
    assertEquals("java.lang.ClassCastException", 
                 ((ExceptionResponse)response).exceptionName);
  }
}
