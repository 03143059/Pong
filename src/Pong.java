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
    public static InetAddress address = null;
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("-i")) {
            int num = 0;
            if (args.length == 2)
            try {
                num = Integer.parseInt(args[1]);
            } catch(NumberFormatException nfe){

            }
            Enumeration nifEnm = NetworkInterface.getNetworkInterfaces();
            while (nifEnm.hasMoreElements()) {
                NetworkInterface nif = (NetworkInterface)nifEnm.nextElement();
                if (!nif.isLoopback() && nif.getInterfaceAddresses().size() > 0) {
                    Enumeration addrEnum = nif.getInetAddresses();
                    int i = 0;
                    while (addrEnum.hasMoreElements()) {
                        InetAddress a = (InetAddress)addrEnum.nextElement();
                        if (a instanceof Inet4Address) {
                            if (i == 0){
                                if (num == 0)// mostrar interfaces?
                                    System.out.println(String.format("%d\t%s\t%s",
                                            nif.getIndex(), nif.getName(), nif.getDisplayName()));
                            }
                            i++;
                            // mostrar interfaces?
                            if (num == 0)
                                System.out.println("\t" + a.getHostAddress());
                            else if (num == nif.getIndex())
                                address = a;
                        }
                    } // end-while address
                }
            } // end-while interfaces
            if (address == null) {
                if (num > 0)
                    System.err.println("El numero de interfaz es invalido!");
                System.exit(2);
            }
            System.out.println("Utilizando IP: " + address.getHostAddress());
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new PongWindow();
                }
            });
        } else {
            System.out.println("Uso: java Pong -i [interface]");
            System.exit(1);
        }
    }

}
