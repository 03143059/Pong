import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Werner on 8/9/2014.
 */
public class SearchForGameThread extends Thread {
    public static final int MAX_TRIES = 10;
    private final PongWindow window;
    DatagramSocket socket = null;

    private long FIVE_SECONDS = 5000;

    public SearchForGameThread(PongWindow window) throws SocketException {
        super("SearchForGameThread");
        this.window = window;
        socket = new DatagramSocket(4445);
    }

    @Override
    public void run() {

        for (int i = 1; i <= MAX_TRIES; i++) {
            try {
                // construct quote
                String dString = InetAddress.getLocalHost().toString();
                byte[] buf = dString.getBytes();
                // send it
                InetAddress group = InetAddress.getByName("230.0.0.1");
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 4446);
                socket.send(packet);
                window.RemotePlayer = String.valueOf(i);
                // sleep for a while
                try {
                    sleep((long) (Math.random() * FIVE_SECONDS));
                } catch (InterruptedException e) {
                }

            } catch (IOException e) {
                window.RemotePlayer = "ERROR: " + e.getMessage();
            }
        }
        window.RemotePlayer = "Tiempo de espera agotado\nNo hay respuesta";
        socket.close();
    }
}
