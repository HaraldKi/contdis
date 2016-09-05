package de.pifpafpuf.contdis;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import de.pifpafpuf.contdis.producer.ExceptionResponse;
import de.pifpafpuf.contdis.producer.ResponseOk;

public class Server extends IoHandlerAdapter {
  private static final Logger log = getLogger();
  private final IoAcceptor acceptor;
  private final int port;
  private String fixedReturnMessage = null;
  private final KeyedQueue queue;
  /*+******************************************************************/
  public Server(IoAcceptor acceptor, int port, int maxInflight) {
    this.acceptor = acceptor;
    this.port = port;
    this.queue = new KeyedQueue(maxInflight);
  }
  /*+******************************************************************/
  @Override
  public void messageReceived(IoSession session, Object message)
      throws Exception
  {
    if (fixedReturnMessage!=null) {
      // for testing clients only, don't use in production code
      session.write(fixedReturnMessage);
      return;
    }
    if (message instanceof PushRequest) {
      queueRequest((PushRequest)message);
      session.write(ResponseOk.INSTANCE);
      return;
    }

    Exception e =
        new IllegalArgumentException("unexpected type "
            + message.getClass().getName()
            + " received");
    session.write(new ExceptionResponse(e));
  }
  /*+******************************************************************/
  private void queueRequest(PushRequest msg) {

  }
  /*+******************************************************************/
  /**
   * for client testing only, don't use in production code.
   */
  public void setFixedReturnMessage(String s) {
    this.fixedReturnMessage = s;
  }
  /*+******************************************************************/
  @Override
  public void exceptionCaught(IoSession session, Throwable cause)
      throws Exception
  {
    cause.printStackTrace();
  }

  /**
   * start listening on the configured port.
   *
   * This method returns immediately.
   * @throws IOException
   */
  public void start() throws IOException {
    log.info("binding server on port "+port);
    acceptor.bind(new InetSocketAddress(port));
  }
  /*+******************************************************************/
  public void shutdown() {
    acceptor.unbind();
    acceptor.dispose(true);
    log.info("server for port "+port+" now shut down");
  }
  /*+******************************************************************/
  public static Logger getLogger() {
    StackTraceElement[] stack =  Thread.currentThread().getStackTrace();
    return Logger.getLogger(stack[2].getClassName());
  }
  /*+******************************************************************/
  public static Server create(int port, int maxInfligt) throws IOException {
    NioSocketAcceptor acceptor = new NioSocketAcceptor();
    acceptor.setReuseAddress(true);
    ProtocolCodecFactory cf = new ObjectSerializationCodecFactory();
    acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(cf));
    Server server = new Server(acceptor, port, maxInfligt);
    acceptor.setHandler(server);
    return server;
  }
  public static void main(String[] argv) throws IOException {
    create(31125, 10);
    System.out.println("XXXXXXXXXXXXXXXX not yet implemented, need to find"
        + " out how to wait for acceptor to be disposed");
  }
}
