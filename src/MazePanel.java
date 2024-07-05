import Players.Hero;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import javax.imageio.ImageIO;

public class MazePanel extends JPanel {

    private static final int CELL_SIZE = 40; // Size of each cell in pixels
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;

    private Hero hero;

    private double zoom = 1.0;
    private double targetZoom = 1.0;
    private double zoomProgress = 0.0;

    public void zoomIn() {
        targetZoom += 0.1;
        if (targetZoom > 2.0) {
            targetZoom = 2.0;
        }
        zoomProgress = 0.0;
    }

    public void zoomOut() {
        targetZoom -= 0.1;
        if (targetZoom < 0.5) {
            targetZoom = 0.5;
        }
        zoomProgress = 0.0;
    }

    public void handleZoomIn() {
        zoomIn();
    }

    public void handleZoomOut() {
        zoomOut();
    }

    private final int rows;
    private final int cols;

    private BufferedImage[][] tiles; // 2D array to hold different tile types
    private static final int GRASS_TILES = 0; // Index for grass tiles
    private static final int WALL_TILES = 1;  // Index for wall tiles
    private BufferedImage[] oceanFrames; // Array to hold ocean frames
    private int currentOceanFrame = 0;
    private final int[] leftwallIndices = {25, 33, 41, 57};
    private final int[][] leftwallTileMap;
    private final int[] rightwallIndices = {26, 35, 42, 58};
    private final int[][] rightwallTileMap;
    private final int[] topwallIndices = {18, 28, 29 ,30};
    private final int[][] topwallTileMap;
    private final int[] secondBottomWallIndices = {50, 97, 98, 99, 100, 148, 149};
    private final int[][] secondBottomWallMap;
    private final int[] bottomWallIndices = {66, 113, 114, 115, 116, 118};
    private final int[][] bottomWallTileMap;
    private final int[] grassTilesIndices = createGrassTilesIndices();
    private int[] createGrassTilesIndices() {
        int[] indices = new int[32];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        return indices;
    }
    private final int[][] grassTileMap;
    private double cameraX;
    private double cameraY;

    public MazePanel(int[][] maze, int rows, int cols, int level) {
        this.rows = rows;
        this.cols = cols;
        this.grassTileMap = new int[rows][cols];
        this.topwallTileMap = new int[rows][cols];
        this.leftwallTileMap = new int[rows][cols];
        this.rightwallTileMap = new int[rows][cols];
        this.secondBottomWallMap = new int[rows][cols];
        this.bottomWallTileMap = new int[rows][cols];
        this.cameraX = 0;
        this.cameraY = 0;
        initializeTileMap();

        loadImages();
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new PlayerKeyListener(this));
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // Animation Timer
        Timer animationTimer = new Timer(16, new ActionListener() {
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

        Timer oceanAnimationTimer = new Timer(200, _ -> {
            currentOceanFrame = (currentOceanFrame + 1) % oceanFrames.length;
            repaint(); // Repaint this OceanBackground component
        });
        oceanAnimationTimer.start();
    }

    private void initializeTileMap() {
        Random random = new Random();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                bottomWallTileMap[r][c] = bottomWallIndices[random.nextInt(rightwallIndices.length)];
                secondBottomWallMap[r][c] = secondBottomWallIndices[random.nextInt(rightwallIndices.length)];
                rightwallTileMap[r][c] = rightwallIndices[random.nextInt(rightwallIndices.length)];
                leftwallTileMap[r][c] = leftwallIndices[random.nextInt(leftwallIndices.length)];
                topwallTileMap[r][c] = topwallIndices[random.nextInt(topwallIndices.length)];
                grassTileMap[r][c] = grassTilesIndices[random.nextInt(grassTilesIndices.length)];
            }
        }
    }

    private void loadImages() {
        try {
            BufferedImage oceanSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/ocean/Water.png"))); // Replace with your path
            int numOceanFrames = 4;
            oceanFrames = new BufferedImage[numOceanFrames];
            int oceanFrameWidth = oceanSpriteSheet.getWidth() / numOceanFrames;
            int oceanFrameHeight = oceanSpriteSheet.getHeight();
            for (int i = 0; i < numOceanFrames; i++) {
                oceanFrames[i] = oceanSpriteSheet.getSubimage(i * oceanFrameWidth, 0, oceanFrameWidth, oceanFrameHeight);
            }
            tiles = new BufferedImage[2][]; // Create the 2D array
            BufferedImage tilesSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/mazeTiles/Grass.png")));
            loadTilesFromSpriteSheet(tilesSpriteSheet, GRASS_TILES);
            BufferedImage wallSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/mazeTiles/Wall.png")));
            loadTilesFromSpriteSheet(wallSpriteSheet, WALL_TILES);
            BufferedImage idleSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/player/idle.png")));
            BufferedImage walkingForwardSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/player/walkingForward.png")));
            BufferedImage walkingBackwardSpriteSheet = ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/player/walkingBackward.png")));
            hero = new Hero(1, 1, idleSpriteSheet, walkingForwardSpriteSheet, walkingBackwardSpriteSheet, 60);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTilesFromSpriteSheet(BufferedImage spriteSheet, int tileTypeIndex) throws IOException {

        int numRows = spriteSheet.getHeight() / 32;
        int numCols = spriteSheet.getWidth() / 32;
        int numTiles = numRows * numCols;

        tiles[tileTypeIndex] = new BufferedImage[numTiles]; // Create array for this type

        for (int i = 0; i < numTiles; i++) {
            int row = i / numCols;
            int col = i % numCols;

            tiles[tileTypeIndex][i] = spriteSheet.getSubimage(
                    col * 32,
                    row * 32,
                    32,
                    32
            );
        }
    }

    public void updateCamera() {
        // Center the camera on the hero with smoothing adjusted for zoom:
        double targetCameraX = (hero.getX() * zoom) - ((double) WIDTH / 2 / zoom) + ((double) CELL_SIZE / 2 / zoom);
        double targetCameraY = (hero.getY() * zoom) - ((double) HEIGHT / 2 / zoom) + ((double) CELL_SIZE / 2 / zoom);

        // Apply Easing for smooth movement
        double smoothingSpeed = 9.0; // Adjust this value for desired smoothness
        cameraX = easeInOutQuad(0.9/smoothingSpeed, cameraX, targetCameraX - cameraX * Math.ceil(zoom) * 1.5, 0.9);
        cameraY = easeInOutQuad(1.0/smoothingSpeed, cameraY, targetCameraY - cameraY * Math.ceil(zoom) * 1.5, 1.0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateCamera(); // Update before drawing

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Apply Scaling and Translation for Zoom and Camera
        g2d.scale(zoom, zoom);
        g2d.translate(-cameraX, -cameraY);

        // 2. Draw Ocean Background (NOW after scaling and translating)
        drawOceanBackground(g2d);

        // 3. Draw Maze
        drawMaze(g2d);

        // 4. Draw Hero (After maze, within the same transformation context)
        int heroX = (int) (hero.getX());
        int heroY = (int) (hero.getY());
        int heroSize = 32;
        g2d.drawImage(hero.getImage(), heroX, heroY, heroSize, heroSize, null);

        if (zoomProgress < 1.0) {
            zoomProgress += 0.05; // Increase the progress of the zoom
            zoom = zoom + (targetZoom - zoom) * zoomProgress;
        } else {
            zoom = targetZoom; // Snap to the target zoom if the progress is complete
        }

        Toolkit.getDefaultToolkit().sync();

    }

    private void drawOceanBackground(Graphics2D g2d) {
        if (oceanFrames != null) {

            // 1. Calculate Visible Area in Tile Coordinates
            int startX = (int) Math.floor(cameraX / CELL_SIZE) - 1;
            int startY = (int) Math.floor(cameraY / CELL_SIZE) - 1;
            int endX = (int) Math.ceil((cameraX + getWidth() / zoom) / CELL_SIZE) + 1;
            int endY = (int) Math.ceil((cameraY + getHeight() / zoom) / CELL_SIZE) + 1;

            // 2. Draw Ocean Tiles
            for (int row = startY; row < endY; row++) {
                for (int col = startX; col < endX; col++) {
                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;

                    // 3. Select Tile Index
                    int tileIndex;
                    tileIndex = currentOceanFrame;

                    // Randomly choose an ocean frame
                    g2d.drawImage(oceanFrames[tileIndex], x, y, CELL_SIZE, CELL_SIZE, null);
                }
            }
        }
    }

    private void drawMaze(Graphics g) {

        // 1. Draw Grass Tiles First (Layer 0)
        for (int r = 0; r < rows ; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * CELL_SIZE;
                int y = r * CELL_SIZE;
                g.drawImage(tiles[GRASS_TILES][grassTileMap[r][c]], x, y, CELL_SIZE, CELL_SIZE, null);
            }
        }

        // 1.5 Draw Paths
        for (int r = 0; r < rows ; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * CELL_SIZE;
                int y = r * CELL_SIZE;

                if(r == 2 && c <= 5) g.drawImage(tiles[GRASS_TILES][32], x, y - CELL_SIZE, CELL_SIZE, CELL_SIZE, null);

            }
        }

        // 2. Draw Wall Tiles Second (Layer 1)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                int x = c * CELL_SIZE;
                int y = r * CELL_SIZE;

                int toptileIndex = topwallTileMap[r][c];
                if(r == 0 && c == 0) {
                    g.drawImage(tiles[WALL_TILES][17], x, y, CELL_SIZE, CELL_SIZE, null);
                }
                if(r == 0 && c == cols - 1) {
                    g.drawImage(tiles[WALL_TILES][19], x, y, CELL_SIZE, CELL_SIZE, null);
                }
                if(r == rows - 2 && c == 0) {
                    g.drawImage(tiles[WALL_TILES][49], x, y, CELL_SIZE, CELL_SIZE, null);
                }
                if(r == rows - 2 && c == cols - 1) {
                    g.drawImage(tiles[WALL_TILES][51], x, y, CELL_SIZE, CELL_SIZE, null);
                }
                if(r == rows - 1 && c == 0) {
                    g.drawImage(tiles[WALL_TILES][65], x, y, CELL_SIZE, CELL_SIZE, null);
                }
                if(r == rows - 1 && c == cols - 1) {
                    g.drawImage(tiles[WALL_TILES][67], x, y, CELL_SIZE, CELL_SIZE, null);
                }
                if(r == 0 &&(c > 0 && c < cols - 1)){
                    g.drawImage(tiles[WALL_TILES][toptileIndex], x, y, CELL_SIZE, CELL_SIZE, null);
                }

                int lefttileIndex = leftwallTileMap[r][c];
                if(c == 0 &&(r > 0 && r < rows - 2)){
                    g.drawImage(tiles[WALL_TILES][lefttileIndex], x, y, CELL_SIZE, CELL_SIZE, null);
                }

                int rightileIndex = rightwallTileMap[r][c];
                if(c == cols - 1 &&(r > 0 && r < rows - 2)){
                    g.drawImage(tiles[WALL_TILES][rightileIndex], x, y, CELL_SIZE, CELL_SIZE, null);
                }

                int bottomWallTileIndex = bottomWallTileMap[r][c];
                if(r == rows - 1 &&(c > 0 && c < cols - 1)){
                    g.drawImage(tiles[WALL_TILES][bottomWallTileIndex], x, y, CELL_SIZE, CELL_SIZE, null);
                }

                int secondBottomWallIndex = secondBottomWallMap[r][c];
                if(r == rows - 2 &&(c > 0 && c < cols - 1)){
                    g.drawImage(tiles[WALL_TILES][secondBottomWallIndex], x, y, CELL_SIZE, CELL_SIZE, null);
                }
            }
        }
    }

    public void movePlayer(int keyCode, boolean isMoving) {
        hero.setMoving(keyCode, isMoving); // Pass keyCode and isMoving to Hero
    }

    public boolean isCollision(int row, int col) {
        // Check for collisions with maze boundaries:
        if (row < -1 || row >= rows - 1 || col < 0 || col >= cols) return true;
        // Check for collision with walls (no buffer):
        else return row == -1 || row == rows - 2;
    }

    public void update(double deltaTime) {
        hero.move(deltaTime);
        if (hero.isMoving()) {
            int nextRow = hero.getRow() + (int) Math.signum(hero.getTargetRow() - hero.getRow());
            int nextCol = hero.getCol() + (int) Math.signum(hero.getTargetCol() - hero.getCol());

            if (!isCollision(nextRow, nextCol)) {
                hero.move(deltaTime);
            } else {
                hero.stopMoving();
            }
        }

        // Smooth zoom transition (now OUTSIDE the hero movement block):
        if (zoom != targetZoom) {
            double zoomSpeed = 0.05;
            zoomProgress += zoomSpeed;
            if (zoomProgress > 1.0) {
                zoomProgress = 1.0;
            }

            zoom = easeInOutQuad(zoomProgress, zoom, targetZoom - zoom, 100);
            updateCamera();
            repaint();
        }

        hero.updateAnimation();
        repaint();
    }

    // Ease-In-Out Quad function:
    private double easeInOutQuad(double t, double b, double c, double d) {
        t /= d/2;
        if (t < 1) return c/2*t*t + b;
        t--;
        return -c/2 * (t*(t-2) - 1) + b;
    }

}