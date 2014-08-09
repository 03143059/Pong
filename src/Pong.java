import java.awt.*;
import java.io.IOException;

/**
 * Created by Werner on 7/16/2014.
 */
public class Pong {
    public static void main(String[] args) throws IOException {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PongWindow();
            }
        });
    }

}
