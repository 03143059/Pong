import java.awt.*;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by Werner on 7/16/2014.
 */
public class Pong {
    public static void main(String[] args) throws IOException {
        if (args.length == 1 && args[0].equals("-i")) {
            Enumeration nifEnm = NetworkInterface.getNetworkInterfaces();
            while (nifEnm.hasMoreElements()) {
                NetworkInterface nif = (NetworkInterface)nifEnm.nextElement();
                if (!nif.isLoopback() && nif.getInterfaceAddresses().size() > 0) {
                    Enumeration addrEnum = nif.getInetAddresses();
                    boolean isFirst = true;
                    while (addrEnum.hasMoreElements()) {
                        InetAddress a = (InetAddress)addrEnum.nextElement();
                        if (a instanceof Inet4Address) {
                            if (isFirst){
                                System.out.println(String.format("%d\t%s\t%s", nif.getIndex(), nif.getName(), nif.getDisplayName()));
                                isFirst = false;
                            }
                            System.out.println("\t" + a.getHostAddress());
                        }
                    }
                }
            }
            System.exit(0);
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PongWindow();
            }
        });
    }

}
