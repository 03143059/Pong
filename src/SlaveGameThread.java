import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Werner on 8/10/2014.
 */
public class SlaveGameThread extends Thread {
    private final PongWindow window;
    private NetworkGameScreen screen = null;
    ServerSocket masterSocket = null;

    public void SetScreen(NetworkGameScreen screen){
        this.screen = screen;
        synchronized (this) {
            notify();
        }
    }

    public SlaveGameThread(PongWindow window) throws IOException {
        super("SlaveGameThread");
        this.window = window;
        masterSocket = new ServerSocket(6789, 0, Pong.address);
    }

    @Override
    public void run() {

        Socket connectionSocket = null;
        try {
            // block until connection is made
            connectionSocket = masterSocket.accept();
            // start game
            window.RemotePlayer = "MASTER " + Pong.address.getHostAddress();
            InputStream in = connectionSocket.getInputStream();
            BufferedReader inFromMaster = new BufferedReader(new InputStreamReader(in));
            DataOutputStream outToMaster = new DataOutputStream(connectionSocket.getOutputStream());
            String line;
            synchronized (this){
                wait();
            }
            while(true) {

                if (in.available() > 0) {
                    line = inFromMaster.readLine();
                    // convert bool:keycode to event on screen
                    int code = Integer.parseInt(line.substring(line.indexOf(':')+1));
                    boolean val = Boolean.valueOf(line.substring(0, line.indexOf(':')));
                    screen.remoteKeys[code] = val;
                    System.out.println("From master: " + code + "=" + val);
                }
                synchronized(screen.keystr) {
                    if(!screen.keystr.isEmpty()) {
                        String s = screen.keystr.poll();
                        System.out.println("Sending to master: " + s);
                        outToMaster.writeBytes(s + '\n');
                        outToMaster.flush();
                    }
                }
            }
        } catch (Exception ioe) {
            //screen.RemotePlayer = "ERROR: " + ioe.getMessage();
            ioe.printStackTrace(System.err);
            try{
                connectionSocket.close();
            } catch (Exception e){}
            JOptionPane.showMessageDialog(null, ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
