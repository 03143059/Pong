import javax.swing.*;
import java.io.*;
import java.net.Socket;

/**
 * Created by Werner on 8/10/2014.
 */
public class MasterGameThread extends Thread {
    private final NetworkGameScreen screen;
    Socket slaveSocket = null;

    public MasterGameThread(NetworkGameScreen screen) throws IOException {
        super("MasterGameThread");
        this.screen = screen;
        String remoteHost = ((PongWindow)screen.parent).RemotePlayer; // formato HRS-GUA-02/169.254.80.80
        slaveSocket = new Socket(remoteHost.substring(remoteHost.lastIndexOf('/')+1), 6789);
    }

    @Override
    public void run() {
        try {
            DataOutputStream outToSlave = new DataOutputStream(slaveSocket.getOutputStream());
            InputStream in = slaveSocket.getInputStream();
            BufferedReader inFromSlave = new BufferedReader(new InputStreamReader(in));
            String line;
            while (true){

                if (in.available() > 0) {
                    line = inFromSlave.readLine();
                    // convert bool:keycode to event on screen
                    int code = Integer.parseInt(line.substring(line.indexOf(':')+1));
                    boolean val = Boolean.valueOf(line.substring(0, line.indexOf(':')));
                    screen.remoteKeys[code] = val;
                    System.out.println("From slave: " + code + "=" + val);
                }
                synchronized(screen.keystr) {
                    if(!screen.keystr.isEmpty()) {
                        String s = screen.keystr.poll();
                        System.out.println("Sending to slave: " + s);
                        outToSlave.writeBytes(s + '\n');
                        outToSlave.flush();
                    }
                }
            }
            //
        } catch (Exception ioe) {
            //screen.RemotePlayer = "ERROR: " + ioe.getMessage();
            ioe.printStackTrace(System.err);
            try{
                slaveSocket.close();
            } catch (Exception e){}
            JOptionPane.showMessageDialog(null, ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
