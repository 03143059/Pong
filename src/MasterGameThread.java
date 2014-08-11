import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Werner on 8/10/2014.
 */
public class MasterGameThread extends Thread {
    private final NetworkGameScreen screen;
    Socket clientSocket = null;

    public MasterGameThread(NetworkGameScreen screen) throws IOException {
        super("WaitForGameThread");
        this.screen = screen;
        clientSocket = new Socket("localhost", 6789);
    }

    @Override
    public void run() {
        try {
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line;
            while ((line = inFromServer.readLine()) != null){
                System.out.println("From Server: " + line);
                outToServer.writeBytes("from client" + '\n');
            }
            clientSocket.close();
        } catch (IOException ioe) {
            //screen.RemotePlayer = "ERROR: " + ioe.getMessage();
        }
    }
}