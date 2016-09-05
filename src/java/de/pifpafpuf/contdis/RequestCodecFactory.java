package de.pifpafpuf.contdis;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * @deprecated "use ObjectSerializationCodecFactory"
 */
@Deprecated()
public class RequestCodecFactory implements ProtocolCodecFactory {

  @Override
  public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
    return Decoder.INSTANCE;
  }

  @Override
  public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
    return Encoder.INSTANCE;
  }
  /*+******************************************************************/
  enum Decoder implements ProtocolDecoder {
    INSTANCE;

    @Override
    public void decode(IoSession session, IoBuffer data,
                       ProtocolDecoderOutput result) throws Exception
    {
      ObjectInputStream ois = new ObjectInputStream(data.asInputStream());
      Object decoded = ois.readObject();
      result.write(decoded);
      ois.close();
    }

    @Override
    public void dispose(IoSession arg0) throws Exception {
      // nothing here
    }

    @Override
    public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
      throws Exception
    {
      // nothing here      
    }
  }
  /*+******************************************************************/
  enum Encoder implements ProtocolEncoder {
    INSTANCE;

    @Override
    public void dispose(IoSession arg0) throws Exception {
      // nothing to dispose
    }

    @Override
    public void encode(IoSession session, Object data,
                       ProtocolEncoderOutput result) throws Exception
    {
      final int STARTSIZE = 10_000_000;
      IoBuffer buf = IoBuffer.allocate(STARTSIZE).setAutoExpand(true);
      ObjectOutputStream oos = new ObjectOutputStream(buf.asOutputStream());
      oos.writeObject(data);
      oos.close();
      result.write(buf);
    }
  }
}
