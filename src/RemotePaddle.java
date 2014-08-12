import java.awt.geom.Rectangle2D;

/**
 * @author Emil
 */
public class RemotePaddle extends Rectangle2D.Double {
    private final int gameHeight, gameWidth;
    private double  speed;

    public RemotePaddle(int x, int y, int gameWidth, int gameHeight, int width, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.gameHeight = gameHeight;
        this.gameWidth = gameWidth;
    }

    boolean setSpeed(int speed) {
        if (speed < 0 || speed > 16)
            return false;
        this.speed = speed;
        return true;
    }

    boolean setY(int y) {
        if (y < height)
            this.y = y;
        return y < height;
    }

    void move(boolean dir) {
        if (dir)
            y += speed;
        else
            y -= speed;
    }

}