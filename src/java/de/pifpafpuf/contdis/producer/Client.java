package de.pifpafpuf.contdis.producer;

import java.net.InetSocketAddress;
import java.util.concurrent.SynchronousQueue;
import org.apache.log4j.Logger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import de.pifpafpuf.contdis.PushRequest;
import de.pifpafpuf.contdis.Server;

public class Client extends IoHandlerAdapter {
  private static final Logger log = Server.getLogger();
  private IoSession session = null;
  private final SynchronousQueue<PushResponse> response = 
      new SynchronousQueue<>();
  public Client() {
  }
  /*+******************************************************************/
  @Override
  public void sessionCreated(IoSession ses) {
    log.info("client session created for "+ses);
    this.session = ses;
  }
  /*+******************************************************************/
  public static Client connect(String host, int port) {
    NioSocketConnector connector = new NioSocketConnector();
    connector.setConnectTimeoutMillis(5000);

    ProtocolCodecFactory cf = new ObjectSerializationCodecFactory();
    connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(cf));

    Client result = new Client();
    connector.setHandler(result);
    log.info("connecting to "+host+":"+port);
    ConnectFuture future = connector.connect(new InetSocketAddress(host, port));

    future.awaitUninterruptibly();
    future.getSession();
    return result;
  }
  /*+******************************************************************/
  public Object send(PushRequest msg) throws InterruptedException {
    session.write(msg);
    return response.take();
  }
  /*+******************************************************************/
  /** for testing purposes only, don't use */
  public Object sendAny(Object msg) throws InterruptedException {
    session.write(msg);
    return response.take();
  }
  /*+******************************************************************/
  @Override
  public void messageReceived(IoSession session, Object message)
      throws InterruptedException
  {
    try {
      PushResponse resp = PushResponse.class.cast(message);
      response.put(resp);
    } catch (ClassCastException e) {
      PushResponse r = new ExceptionResponse(e);
      response.put(r);
    }
  }
}
