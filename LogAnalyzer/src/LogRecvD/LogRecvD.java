package LogRecvD;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.logging.log4j.*;


public class LogRecvD {
	static Logger logger = LogManager.getRootLogger();

	static byte[] packet = new byte[512];
	
	public static void main(String args[]) {
		RecvFileConst fc = new RecvFileConst();
		
		DatagramSocket ds;
		try {
			ds = new DatagramSocket(514);
			while(true) {
				DatagramPacket dp = new DatagramPacket(packet, packet.length);
				ds.receive(dp);
				
				String msg = new String(dp.getData(), 0, dp.getLength());
				
				fc.FileWrite(msg);
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
