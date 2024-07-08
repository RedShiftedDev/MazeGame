import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.List;
import javax.imageio.ImageIO;

public class MazePanel extends JPanel {

    public static final int CELL_SIZE = 40;
    private final Timer animationTimer;
    private final GameOver gameOverScreen;
    private int score = 0;
    private final List<BufferedImage> spikeAnimationFrames = new ArrayList<>();
    private final List<Spike> spikes = new ArrayList<>();
    private final List<BufferedImage> coinAnimationFrames = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();
    private final List<BufferedImage> keyAnimationFrames = new ArrayList<>();
    private final List<Key> key = new ArrayList<>();
    private final List<BufferedImage> healthBarFrames = new ArrayList<>();
    private HealthBar heroHealthBar;
    private int accumulatedSpikeDamage = 0;
    private boolean checkSpikeCollisionsNextFrame = false;
    private Hero hero;
    private final List<Enemy> enemies;
    public final int[][] maze;
    private double cameraX;
    private BufferedImage upperWallImage;
    private BufferedImage lowerWallImage;
    private BufferedImage pathImage;
    private BufferedImage chestImage;
    private BufferedImage exitImage;
    BufferedImage enemyBackwardSpriteSheet;
    BufferedImage enemyForwardSpriteSheet;
    BufferedImage enemyIdleSpriteSheet;

    public MazePanel(int[][] maze, int rows, int cols, int level) {
        this.maze = maze;
        loadImages();
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new PlayerKeyListener(this));
        gameOverScreen = new GameOver(this);
        this.enemies = new ArrayList<>();

        enemies.add(new Enemy(1, 1, enemyIdleSpriteSheet,
                enemyForwardSpriteSheet, enemyBackwardSpriteSheet, 45, 7, this));
        enemies.add(new Enemy(cols - 2, 1, enemyIdleSpriteSheet,
                enemyForwardSpriteSheet, enemyBackwardSpriteSheet, 50, 6, this));
        enemies.add(new Enemy(cols / 2, rows - 2, enemyIdleSpriteSheet,
                enemyForwardSpriteSheet, enemyBackwardSpriteSheet, 60, 5, this));
        enemies.add(new Enemy(cols / 2, rows - 6, enemyIdleSpriteSheet,
                enemyForwardSpriteSheet, enemyBackwardSpriteSheet, 70, 7, this));
        cameraX = hero.getX();

        int preferredWidth = maze[0].length * CELL_SIZE;
        int preferredHeight = maze.length * CELL_SIZE;
        setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        animationTimer = new Timer(16, new ActionListener() {
            long lastTime = System.nanoTime();
            @Override
            public void actionPerformed(ActionEvent e) {
                long currentTime = System.nanoTime();
                double deltaTime = (currentTime - lastTime) / 1000000000.0;
                lastTime = currentTime;
                update(deltaTime);
                repaint();
            }
        });

        animationTimer.start();
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[row][col] == MazeLogic.SPIKE) {
                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;
                    spikes.add(new Spike(x, y, spikeAnimationFrames));
                }
            }
        }

        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[row][col] == MazeLogic.COIN) {
                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;
                    coins.add(new Coin(x, y, row, col, coinAnimationFrames));
                }
            }
        }

        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[row][col] == MazeLogic.KEY) {
                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;
                    key.add(new Key(x, y, keyAnimationFrames));
                }
            }
        }

        AudioPlayer.stopSound("bgc");
        AudioPlayer.loopSound("backgroundMusic");
    }

    private void loadImages() {
        try {
            AudioPlayer.loadSound("chaseSound", "/assets/sounds/chase.wav");
            AudioPlayer.loadSound("backgroundMusic", "/assets/sounds/backgroundSounds.wav");
            AudioPlayer.loadSound("coin", "/assets/sounds/coin.wav"); // Example coin sound
            AudioPlayer.loadSound("key", "/assets/sounds/key.wav");   // Example key sound
            AudioPlayer.loadSound("spike", "/assets/sounds/spike.wav"); // Example spike hurt sound

            BufferedImage healthBarSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/mazeElement/100hpto0hp.png")));
            int numHealthBarFrames = 11;
            int healthBarFrameWidth = healthBarSpriteSheet.getWidth();
            int healthBarFrameHeight = healthBarSpriteSheet.getHeight() / numHealthBarFrames;
            for (int i = 0; i < numHealthBarFrames; i++) {
                healthBarFrames.add(healthBarSpriteSheet.getSubimage(0, i * healthBarFrameHeight, healthBarFrameWidth, healthBarFrameHeight));
            }

            BufferedImage keyAnimationSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/mazeElement/key.png")));
            int numKeyFrames = 6;
            int keyFrameWidth = keyAnimationSpriteSheet.getWidth() / numKeyFrames;
            int keyFrameHeight = keyAnimationSpriteSheet.getHeight();
            for (int i = 0; i < numKeyFrames; i++) {
                keyAnimationFrames.add(keyAnimationSpriteSheet.getSubimage(i * keyFrameWidth, 0, keyFrameWidth, keyFrameHeight));
            }

            BufferedImage coinAnimationSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/mazeElement/coins.png")));
            int numCoinFrames = 6;
            int coinFrameWidth = coinAnimationSpriteSheet.getWidth() / numCoinFrames;
            int coinFrameHeight = coinAnimationSpriteSheet.getHeight();
            for (int i = 0; i < numCoinFrames; i++) {
                coinAnimationFrames.add(coinAnimationSpriteSheet.getSubimage(i * coinFrameWidth, 0, coinFrameWidth, coinFrameHeight));
            }

            BufferedImage spikeAnimationSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/mazeElement/SpikesSprite.png"))); // Update path
            int numSpikeFrames = 4;
            int spikeFrameWidth = spikeAnimationSpriteSheet.getWidth() / numSpikeFrames;
            int spikeFrameHeight = spikeAnimationSpriteSheet.getHeight();
            for (int i = 0; i < numSpikeFrames; i++) {
                spikeAnimationFrames.add(spikeAnimationSpriteSheet.getSubimage(i * spikeFrameWidth, 0, spikeFrameWidth, spikeFrameHeight));
            }

            chestImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/images/mazeElement/chest.png")));
            exitImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/images/mazeElement/exit.png")));
            upperWallImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/mazeTiles/topWall.png")));
            lowerWallImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/mazeTiles/lowerWall-3.png")));
            pathImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/mazeTiles/path.png")));
            BufferedImage idleSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/player/standing.png")));
            BufferedImage walkingForwardSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/player/walkingForward.png")));
            BufferedImage walkingBackwardSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/player/walkingBackward.png")));
            enemyBackwardSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/player/enemy/EnemyBackward.png")));
            enemyForwardSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/player/enemy/EnemyForward.png")));
            enemyIdleSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/player/enemy/EnemyStanding.png")));
            Random rand = new Random();
            int startX, startY;
            do {
                startX = rand.nextInt(maze[0].length);
                startY = rand.nextInt(maze.length);
            } while (maze[startY][startX] != MazeLogic.FLOOR);

            hero = new Hero(startX * CELL_SIZE, startY * CELL_SIZE, idleSpriteSheet,
                    walkingForwardSpriteSheet, walkingBackwardSpriteSheet, 60, this);
            heroHealthBar = new HealthBar(100, healthBarFrames);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(37, 37, 37));
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Graphics2D g2dMinimap = (Graphics2D) g2d.create();

        g2d.translate(0, 0);

        // Draw health bar
        int healthBarX = 10; // Example position
        int healthBarY = 10;
        g2d.drawImage(heroHealthBar.getImage(), healthBarX, healthBarY, null);

        // Draw HP number
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("HP: " + heroHealthBar.getCurrentHP(), healthBarX + 5, healthBarY + 15); // Adjust position

        // Center the maze
        int mazeWidth = maze[0].length * CELL_SIZE;
        int mazeHeight = maze.length * CELL_SIZE;
        int xOffset = (getWidth() - mazeWidth) / 2;
        int yOffset = (getHeight() - mazeHeight) / 2;
        g2d.translate(xOffset, yOffset);

        // Draw maze
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                int x = col * CELL_SIZE;
                int y = row * CELL_SIZE;
                if (maze[row][col] == MazeLogic.WALL) {
                    if (hasBottomPassage(row, col)) {
                        g2d.drawImage(lowerWallImage, x, y, CELL_SIZE, CELL_SIZE, null);
                    } else {
                        g2d.drawImage(upperWallImage, x, y, CELL_SIZE, CELL_SIZE, null);
                    }
                } else {
                    g2d.drawImage(pathImage, x, y, CELL_SIZE, CELL_SIZE, null);
                }

                switch (maze[row][col]) {
                    case MazeLogic.SPIKE:
                        for (Spike spike : spikes) {
                            g2d.drawImage(spike.getImage(), (int) spike.getX(), (int) spike.getY(), CELL_SIZE, CELL_SIZE, null);
                        }
                        break;
                    case MazeLogic.TREASURE:
                        g2d.drawImage(chestImage, x, y, CELL_SIZE, CELL_SIZE, null);
                        break;
                    case MazeLogic.EXIT:
                        g2d.drawImage(exitImage, x, y, CELL_SIZE, CELL_SIZE, null);
                        break;
                    case MazeLogic.COIN:
                        for (Coin coin : coins) {
                            if (!coin.isCollected()) { // Only draw if not collected
                                g2d.drawImage(coin.getImage(), (int) coin.getX(), (int) coin.getY(), CELL_SIZE, CELL_SIZE, null);
                            }
                        }
                        break;
                    case MazeLogic.KEY:
                        for (Key key : key) {
                            if (!key.isCollected()) { // Only draw if NOT collected
                                g2d.drawImage(key.getImage(), (int) key.getX(), (int) key.getY(), CELL_SIZE, CELL_SIZE, null);
                            }
                        }
                        break;
                }
            }
        }

        // Draw Hero after the maze, still within the transformed context
        int heroX = (int) (hero.getX());
        int heroY = (int) (hero.getY());
        int heroSize = 32;
        g2d.drawImage(hero.getImage(), heroX, heroY, heroSize, heroSize, null);

        // g2d.translate(cameraX - getWidth() / 2.0, cameraY - getHeight() / 2.0);

        for (Enemy enemy : enemies) {
            g.drawImage(enemy.getImage(), (int)enemy.getX(), (int)enemy.getY(), CELL_SIZE, CELL_SIZE, null);
        }

        g2dMinimap.dispose();

        Toolkit.getDefaultToolkit().sync();
    }

    public int[][] getMaze() {
        return maze;
    }

    private boolean checkCoinCollision(Coin coin, Hero hero) {
        double[] coinBounds = {coin.getX() + CELL_SIZE / 2.0, coin.getY() + CELL_SIZE / 2.0, CELL_SIZE / 2.0};
        double[] heroBounds = hero.getBounds();
        double distance = Point2D.distance(coinBounds[0], coinBounds[1], heroBounds[0], heroBounds[1]);
        return distance < coinBounds[2] + heroBounds[2];
    }

    private boolean hasBottomPassage(int row, int col) {
        // Check if the cell below is within bounds
        if (row + 1 < maze.length) {
            // Check if the cell below is NOT a wall
            return maze[row + 1][col] != MazeLogic.WALL;
        }
        return false; // Cell below is out of bounds
    }

    public void movePlayer(int keyCode, boolean isMoving) {
        hero.setMoving(keyCode, isMoving); // Pass keyCode and isMoving to Hero
    }

    boolean isCollision(double nextX, double nextY) {
        int heroSize = 32; // Size of the hero

        // Check the four corners of the hero's bounding box
        int[][] pointsToCheck = {
                { (int) nextX, (int) nextY }, // Top-left
                { (int) nextX + heroSize - 1, (int) nextY }, // Top-right
                { (int) nextX, (int) nextY + heroSize - 1 }, // Bottom-left
                { (int) nextX + heroSize - 1, (int) nextY + heroSize - 1 } // Bottom-right
        };

        for (int[] point : pointsToCheck) {
            int gridX = point[0] / CELL_SIZE;
            int gridY = point[1] / CELL_SIZE;

            if (isOutOfBounds(gridX, gridY) || maze[gridY][gridX] == MazeLogic.WALL) {
                return true;
            }
        }
        return false;
    }

    public void handlePlayerDeath() {
        animationTimer.stop();
        gameOverScreen.showGameOver(score); // Pass the score
    }

    public void resetGame() {
        // 3. Reset game state:
        score = 0;
        //   - Reset player position
        //   - Reset enemy positions
        animationTimer.start();
    }

    public void showTotalScore() {
        // TODO: Implement logic to display total score.
        // You might want to store high scores in a separate class or file
        // and display them in a new window or dialog.
        System.out.println("Total Score: [Implement this]");
    }

    public void goToMainMenu() {
        // TODO: Implement logic to transition to the main menu.
        // You might need to change the JFrame content pane or use a card layout.
        System.out.println("Go to Main Menu [Implement this]");
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= maze[0].length || y < 0 || y >= maze.length;
    }

    private boolean checkKeyCollision(Key key, Hero hero) {
        double[] keyBounds = {key.getX() + CELL_SIZE / 2.0, key.getY() + CELL_SIZE / 2.0, CELL_SIZE / 2.0};
        double[] heroBounds = hero.getBounds();
        double distance = Point2D.distance(keyBounds[0], keyBounds[1], heroBounds[0], heroBounds[1]);
        return distance < keyBounds[2] + heroBounds[2];
    }

    public void update(double deltaTime) {
        double remainingDeltaTime = deltaTime;
        while (remainingDeltaTime > 0) {
            double timeStep = Math.min(remainingDeltaTime, 0.01);
            double newX = hero.getX();
            double newY = hero.getY();

            if (hero.movingLeft) {
                newX -= hero.speed * timeStep * 2;
                if (isCollision(newX, hero.getY())) {
                    newX = hero.getX();
                }
            }
            if (hero.movingRight) {
                newX += hero.speed * timeStep * 2;
                if (isCollision(newX, hero.getY())) {
                    newX = hero.getX();
                }
            }
            if (hero.movingUp) {
                newY -= hero.speed * timeStep * 2;
                if (isCollision(hero.getX(), newY)) {
                    newY = hero.getY();
                }
            }
            if (hero.movingDown) {
                newY += hero.speed * timeStep * 2;
                if (isCollision(hero.getX(), newY)) {
                    newY = hero.getY();
                }
            }

            hero.x = newX;
            hero.y = newY;

            remainingDeltaTime -= timeStep;

            for (Spike spike : spikes) {
                spike.updateAnimation(deltaTime);
            }
        }

        for (Coin coin : coins) {
            if (!coin.isCollected() && checkCoinCollision(coin, hero)) {
                coin.collect();
                score += 10; // Example: Increase score by 10
                AudioPlayer.playSound("coin");
            }
        }

        hero.updateAnimation();
        repaint();
        updateCamera();

        for (Enemy enemy : enemies) {
            enemy.update(hero, maze);
        }

        for (Coin coin : coins) {
            coin.updateAnimation(deltaTime);
        }

        // Key Collision Check
        for (Key key : key) {
            if (!key.isCollected() && checkKeyCollision(key, hero)) {
                key.collect();
                score += 50;
                AudioPlayer.playSound("key");
                System.out.println("Key Collected! Score: " + score);
                hero.collectKey(); // Give the key to the hero
            }
        }

        for (Key key : key) {
            key.updateAnimation(deltaTime); // Update key animation
        }

        if (hero.isHurt() && accumulatedSpikeDamage > 0) {
            heroHealthBar.reduceHP(accumulatedSpikeDamage, hero);
            accumulatedSpikeDamage = 0;
            checkSpikeCollisionsNextFrame = true;
        }

        // Always check for spike collisions:
        for (Spike spike : spikes) {
            if (checkCollision(hero, spike)) {
                if (hero.isHurt()) {
                    System.out.println("Hero hit spike! HP: " + heroHealthBar.getCurrentHP());
                    accumulatedSpikeDamage += 10;
                    AudioPlayer.playSound("spike");
                    hero.setHurt(true);
                    break;
                }
            }
        }

        // Apply accumulated damage if needed (only after invulnerability)
        if (checkSpikeCollisionsNextFrame) {
            checkSpikeCollisionsNextFrame = false;
        }

        // *** GAME OVER CHECK ***
        if (heroHealthBar.getCurrentHP() <= 0) {
            System.out.println("Hero HP: " + heroHealthBar.getCurrentHP() + ", Triggering Game Over");
            handlePlayerDeath();
        }

        if (checkWinCondition()) {
            handleWin();
        }

        // Check for exit door collision:
        if (checkExitDoorCollision(hero)) {
            if (hero.hasKey()) {
                handleWin();
            } else {
                System.out.println("You need the key to open the exit!");
            }
        }

    }

    public void handleWin() {
        animationTimer.stop(); // Stop the game
        JOptionPane.showMessageDialog(this,
                "Congratulations!\nYou Escaped the Maze!",
                "You Win!",
                JOptionPane.INFORMATION_MESSAGE);
        goToMainMenu();
    }

    private boolean checkWinCondition() {
        int heroRow = hero.getRow();
        int heroCol = hero.getCol();
        return maze[heroRow][heroCol] == MazeLogic.EXIT && hero.hasKey();
    }

    private boolean checkExitDoorCollision(Hero hero) {
        double[] exitBounds = {0, 0, CELL_SIZE, CELL_SIZE}; // Initialize properly
        int exitRow, exitCol;

        // Find the exit door coordinates (assuming only one exit)
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[0].length; col++) {
                if (maze[row][col] == MazeLogic.EXIT) {
                    exitRow = row;
                    exitCol = col;
                    exitBounds[0] = exitCol * CELL_SIZE; // Set x-coordinate
                    exitBounds[1] = exitRow * CELL_SIZE; // Set y-coordinate
                    break;
                }
            }
        }

        double[] heroBounds = hero.getBounds();
        double distance = Point2D.distance(exitBounds[0] + exitBounds[2] / 2.0,
                exitBounds[1] + exitBounds[3] / 2.0,
                heroBounds[0], heroBounds[1]);
        return distance < exitBounds[2] / 2.0 + heroBounds[2];
    }

    private boolean checkCollision(Hero hero, Spike spike) {
        double[] heroBounds = hero.getBounds();
        double[] spikeBounds = {spike.getX() + CELL_SIZE / 2.0, spike.getY() + CELL_SIZE / 2.0, CELL_SIZE / 2.0};
        double distance = Point2D.distance(heroBounds[0], heroBounds[1], spikeBounds[0], spikeBounds[1]);
        return distance < heroBounds[2] + spikeBounds[2];
    }

    private void updateCamera() {
        cameraX += (hero.getX() - cameraX) * 0.1;
    }
}