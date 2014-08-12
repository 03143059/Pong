import java.awt.geom.Rectangle2D;

/**
 * @author Emil
 */
public class RemotePaddle extends Rectangle2D.Double {
    private final int gameHeight, gameWidth;
    private final double  SPEED;

    public RemotePaddle(int x, int y, double speed, int gameWidth, int gameHeight, int width, int height) {
        SPEED = speed;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.gameHeight = gameHeight;
        this.gameWidth = gameWidth;
    }

    boolean setY(int y) {
        if (y < height)
            this.y = y;
        return y < height;
    }

    void move(boolean dir) {
        if (dir)
            y += SPEED;
        else
            y -= SPEED;
    }

}