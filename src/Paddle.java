import java.awt.geom.Rectangle2D;

/**
 * @author daniele
 */
public class Paddle extends Rectangle2D.Double {

    private int speed;

    public Paddle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
