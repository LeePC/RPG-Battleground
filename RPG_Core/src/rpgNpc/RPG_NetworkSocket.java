package rpgNpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RPG_NetworkSocket extends Socket
{
	private static final byte[] EMPTY = new byte[1];
	
	
	@Override
	public InputStream getInputStream()
	{
		return new ByteArrayInputStream(EMPTY);
	}
	
	@Override
	public OutputStream getOutputStream()
	{
		return new ByteArrayOutputStream(1);
	}
}
