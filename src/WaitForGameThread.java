import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Werner on 8/9/2014.
 */
public class WaitForGameThread extends Thread {
    private final PongWindow window;

    public WaitForGameThread(PongWindow window) {
        super("WaitForGameThread");
        this.window = window;
    }

    @Override
    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket(4446);
            InetAddress address = InetAddress.getByName("230.0.0.1");
            socket.joinGroup(address);

            DatagramPacket packet;

            byte[] buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            window.RemotePlayer = new String(packet.getData(), 0, packet.getLength());

            socket.leaveGroup(address);
            socket.close();
        } catch (IOException ioe) {
            window.RemotePlayer = "ERROR: " + ioe.getMessage();
        }
    }
}
