import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Werner on 8/10/2014.
 */
public class SlaveGameThread extends Thread {
    private final PongWindow window;
    ServerSocket welcomeSocket = null;

    public SlaveGameThread(PongWindow window) throws IOException {
        super("WaitForGameThread");
        this.window = window;
        welcomeSocket = new ServerSocket(6789);
    }

    @Override
    public void run() {
        try {
            String clientSentence;
            String capitalizedSentence;
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            while((clientSentence = inFromClient.readLine()) != null) {
                System.out.println("Received by TCP: " + clientSentence);
                capitalizedSentence = clientSentence.toUpperCase() + '\n';
                outToClient.writeBytes(capitalizedSentence);
            }
            connectionSocket.close();
        } catch (IOException ioe) {
            //screen.RemotePlayer = "ERROR: " + ioe.getMessage();
        }
    }
}
