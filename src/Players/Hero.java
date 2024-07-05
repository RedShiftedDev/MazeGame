package Players;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Hero {
    private int row, col;
    private double x, y;
    private final double speed;
    private int health;

    private boolean movingUp, movingDown, movingLeft, movingRight;

    private int targetRow, targetCol;
    private boolean moving = false;

    public enum AnimationState { IDLE, WALKING_FORWARD, WALKING_BACKWARD }
    private AnimationState currentState = AnimationState.IDLE;

    private final List<BufferedImage> idleFrames = new ArrayList<>();
    private final List<BufferedImage> walkForwardFrames = new ArrayList<>(); // For forward animation
    private final List<BufferedImage> walkBackwardFrames = new ArrayList<>(); // For backward animation
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private final int numIdleFrames;
    private final int numWalkFrames;

    public Hero(int startX, int startY, BufferedImage idleSpriteSheet, BufferedImage walkForwardSpriteSheet, BufferedImage walkBackwardSpriteSheet, int speed) {
        this.row = row;
        this.col = col;
        this.x = startX; // Set initial x and y directly
        this.y = startY;
        this.speed = speed;
        this.health = 100;
        this.numIdleFrames = 4;
        this.numWalkFrames = 4;
        loadAnimationFrames(idleSpriteSheet, AnimationState.IDLE);
        loadAnimationFrames(walkForwardSpriteSheet, AnimationState.WALKING_FORWARD); // Load forward frames
        loadAnimationFrames(walkBackwardSpriteSheet, AnimationState.WALKING_BACKWARD); // Load backward frames
    }

    public void setMoving(int keyCode, boolean isMoving) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                movingLeft = isMoving;
                break;
            case KeyEvent.VK_RIGHT:
                movingRight = isMoving;
                break;
            case KeyEvent.VK_UP:
                movingUp = isMoving;
                break;
            case KeyEvent.VK_DOWN:
                movingDown = isMoving;
                break;
        }

        // Update animation state based on movement
        if (isMoving) {
            if (keyCode == KeyEvent.VK_LEFT) {
                setState(AnimationState.WALKING_BACKWARD); // Moving up means walking backward
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                setState(AnimationState.WALKING_FORWARD); // Moving down means walking forward
            } else if ((keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP) &&
                    currentState != AnimationState.WALKING_FORWARD &&
                    currentState != AnimationState.WALKING_BACKWARD) {
                // For left and right, if not already walking, default to forward
                setState(AnimationState.WALKING_FORWARD);
            }
        } else if (currentState == AnimationState.WALKING_FORWARD || currentState == AnimationState.WALKING_BACKWARD) {
            // If stopped moving from a walking state, go back to idle
            setState(AnimationState.IDLE);
        }
    }

    public void move(double deltaTime) {
        if (movingLeft)  x -= speed * deltaTime * 2;
        if (movingRight) x += speed * deltaTime * 2;
        if (movingUp)    y -= speed * deltaTime * 2;
        if (movingDown)  y += speed * deltaTime * 2;
    }

    public boolean isMoving() {
        return moving;
    }

    public int getTargetRow() {
        return targetRow;
    }

    public int getTargetCol() {
        return targetCol;
    }

    public void stopMoving() {
        this.moving = false;
        setState(AnimationState.IDLE);
    }

    private void checkTargetReached() {
        if (row == targetRow && col == targetCol) {
            moving = false;
            setState(AnimationState.IDLE);
        }
    }

    private void loadAnimationFrames(BufferedImage spriteSheet, AnimationState state) {
        if (spriteSheet == null) return;

        int numFrames = switch (state) {
            case IDLE -> numIdleFrames;
            case WALKING_BACKWARD, WALKING_FORWARD -> numWalkFrames;
        };

        int frameWidth = spriteSheet.getWidth() / numFrames;
        int frameHeight = spriteSheet.getHeight();

        List<BufferedImage> frames;
        switch (state) {
            case IDLE -> frames = idleFrames;
            case WALKING_FORWARD -> frames = walkForwardFrames;
            case WALKING_BACKWARD -> frames = walkBackwardFrames;
            default -> {
                return; // Or throw an error if an invalid state is passed
            }
        }

        frames.clear();

        for (int i = 0; i < numFrames; i++) {
            frames.add(spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight));
        }
    }

    public void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        long frameTime = 200;

        if (currentTime - lastFrameTime >= frameTime) {
            switch (currentState) {
                case WALKING_FORWARD:
                    currentFrame = (currentFrame + 1) % walkForwardFrames.size();
                    break;
                case WALKING_BACKWARD:
                    currentFrame = (currentFrame + 1) % walkBackwardFrames.size();
                    break;
                default:
                    currentFrame = (currentFrame + 1) % idleFrames.size();
                    break;
            }
            lastFrameTime = currentTime;
        }
    }

    public void setState(AnimationState newState) {
        // Only reset the frame if switching to a DIFFERENT animation state
        if (currentState != newState) {
            currentState = newState;
            currentFrame = 0; // Reset frame to the beginning of the new animation
        }
    }

    // Getters for x and y
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

    public int getHealth() {
        return health;
    }

    public void decreaseHealth(int amount) {
        health -= amount;
    }

    public BufferedImage getImage() {
        return switch (currentState) {
            case WALKING_FORWARD -> walkForwardFrames.get(currentFrame);
            case WALKING_BACKWARD -> walkBackwardFrames.get(currentFrame);
            default -> idleFrames.get(currentFrame);
        };
    }
}