import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

public class Coin {
    private double x, y;
    private int row, col;
    private boolean isCollected = false;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private List<BufferedImage> animationFrames;
    private Random random = new Random();

    public Coin(double x, double y, int row, int col, List<BufferedImage> animationFrames) {
        this.x = x;
        this.y = y;
        this.row = row;
        this.col = col;
        this.animationFrames = animationFrames;
        this.lastFrameTime = System.currentTimeMillis() + random.nextInt(1000); // Random offset for desync
    }

    public void updateAnimation(double deltaTime) {
        if (isCollected) return; // Stop animation if collected

        long currentTime = System.currentTimeMillis();
        if (currentTime > lastFrameTime + 200) { // Update frame every 100 milliseconds
            currentFrame = (currentFrame + 1) % animationFrames.size();
            lastFrameTime = currentTime;
        }
    }

    public void collect() {
        isCollected = true;
    }

    // Getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public BufferedImage getImage() {
        return animationFrames.get(currentFrame);
    }
}