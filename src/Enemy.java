import javax.swing.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Comparator;
import java.util.PriorityQueue;


public class Enemy {
    private double x, y;
    private int row, col;
    private final int speed;
    private boolean isChasing;
    private int lostSightCounter;
    private int patrolTargetRow, patrolTargetCol;
    private final Random random;

    private boolean isMoving = false;
    private int prevDirectionX = 0;
    private int prevDirectionY = 0;

    private final MazePanel mazePanel;
    public enum EnemyAnimationState { IDLE, WALKING_FORWARD, WALKING_BACKWARD }
    private EnemyAnimationState currentState = EnemyAnimationState.IDLE;
    private final List<BufferedImage> enemyIdleFrames = new ArrayList<>();
    private final List<BufferedImage> walkForwardFrames = new ArrayList<>();
    private final List<BufferedImage> walkBackwardFrames = new ArrayList<>();
    private final int numWalkFrames;
    private final int numEnemyIdleFrames;
    private int currentFrame = 0;
    private long lastFrameTime = 0;

    public Enemy(int startX, int startY, BufferedImage enemyIdleSpriteSheet, BufferedImage walkForwardSpriteSheet,
                 BufferedImage walkBackwardSpriteSheet, int speed, int sightRange, MazePanel mazePanel) {
        this.x = startX * MazePanel.CELL_SIZE;
        this.y = startY * MazePanel.CELL_SIZE;
        this.row = startY;
        this.col = startX;
        this.speed = speed;
        this.mazePanel = mazePanel;
        this.numWalkFrames = 4;
        this.numEnemyIdleFrames = 8;
        this.isChasing = false;
        this.lostSightCounter = 0;
        this.random = new Random();
        loadAnimationFrames(enemyIdleSpriteSheet, Enemy.EnemyAnimationState.IDLE);
        loadAnimationFrames(walkForwardSpriteSheet, EnemyAnimationState.WALKING_FORWARD);
        loadAnimationFrames(walkBackwardSpriteSheet, EnemyAnimationState.WALKING_BACKWARD);
        setRandomPatrolTarget(mazePanel.getMaze());
    }

    private void loadAnimationFrames(BufferedImage spriteSheet, EnemyAnimationState state) {
        if (spriteSheet == null) return;

        int numFrames = switch (state) {
            case IDLE -> numEnemyIdleFrames;
            case WALKING_BACKWARD, WALKING_FORWARD -> numWalkFrames;
        };

        int frameWidth = spriteSheet.getWidth() / numFrames;
        int frameHeight = spriteSheet.getHeight();

        List<BufferedImage> frames;
        switch (state) {
            case IDLE -> frames = enemyIdleFrames;
            case WALKING_FORWARD -> frames = walkForwardFrames;
            case WALKING_BACKWARD -> frames = walkBackwardFrames;
            default -> {
                return;
            }
        }

        frames.clear();

        for (int i = 0; i < numFrames; i++) {
            frames.add(spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight));
        }
    }

    private void setRandomPatrolTarget(int[][] maze) {
        do {
            patrolTargetRow = random.nextInt(maze.length);
            patrolTargetCol = random.nextInt(maze[0].length);
        } while (maze[patrolTargetRow][patrolTargetCol] != MazeLogic.FLOOR ||
                (patrolTargetRow == row && patrolTargetCol == col));
    }

    private boolean canSeeHero(Hero hero, int[][] maze) {
        int enemyRow = this.row;
        int enemyCol = this.col;
        int playerRow = hero.getRow();
        int playerCol = hero.getCol();

        // Check if within sight range
        if (Math.abs(enemyRow - playerRow) <= 7 &&
                Math.abs(enemyCol - playerCol) <= 7) {

            return isPathClear(enemyRow, enemyCol, playerRow, playerCol, maze);
        }
        return false;
    }

    private boolean isPathClear(int x0, int y0, int x1, int y1, int[][] maze) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = (dx > dy ? dx : -dy) / 2;

        while (true) {
            // Check if the current cell is a wall
            if (maze[x0][y0] == MazeLogic.WALL) {
                return false;
            }

            if (x0 == x1 && y0 == y1) {
                break;
            }

            int e2 = err;
            if (e2 > -dx) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dy) {
                err += dx;
                y0 += sy;
            }
        }
        return true;
    }

    public void update(Hero hero, int[][] maze) {
        if (checkPlayerCollision(hero)) {
            mazePanel.handlePlayerDeath();
            return;
        }

        if (canSeeHero(hero, maze)) {
            isChasing = true;
            lostSightCounter = 0;
            chaseHero(hero, maze);
        } else {
            if (isChasing) {
                lostSightCounter++;
                if (lostSightCounter >= 30) {
                    isChasing = false;
                    setRandomPatrolTarget(maze);
                } else {
                    chaseHero(hero, maze);
                }
            } else {
                patrol(maze);
            }
        }
        updateAnimation();
    }

    private boolean checkPlayerCollision(Hero hero) {
        double[] enemyBounds = getBounds();
        double[] heroBounds = hero.getBounds();
        double distance = Point2D.distance(enemyBounds[0], enemyBounds[1], heroBounds[0], heroBounds[1]);
        return distance < enemyBounds[2] + heroBounds[2];
    }

    public double[] getBounds() {
        double centerX = x + MazePanel.CELL_SIZE / 2.0;
        double centerY = y + MazePanel.CELL_SIZE / 2.0;
        double radius = MazePanel.CELL_SIZE / 2.0;
        return new double[]{centerX, centerY, radius};
    }

    private boolean canMove(int[][] maze, int nextRow, int nextCol) {
        return nextRow >= 0 && nextRow < maze.length &&
                nextCol >= 0 && nextCol < maze[0].length &&
                (maze[nextRow][nextCol] == MazeLogic.FLOOR || maze[nextRow][nextCol] == MazeLogic.COIN || maze[nextRow][nextCol] == MazeLogic.SPIKE);
    }

    private void chaseHero(Hero hero, int[][] maze) {
        AudioPlayer.loopSound("chaseSound");
        System.out.println("Chasing " + isChasing);
        // Find the shortest path to the hero using A*
        List<Node> path = findPath(row, col, hero.getRow(), hero.getCol(), maze);

        if (path != null && path.size() > 1) {
            // Move one step along the calculated path
            Node nextNode = path.get(1); // Index 0 is the current position
            int targetRow = nextNode.getRow();
            int targetCol = nextNode.getCol();

            if (targetRow < row) {
                moveUp();
            } else if (targetRow > row) {
                moveDown();
            } else if (targetCol < col) {
                moveLeft();
            } else if (targetCol > col) {
                moveRight();
            }
        }
    }

    private static class Node {
        int row, col;
        int gCost, hCost; // gCost: cost from start, hCost: heuristic cost to goal
        Node parent;

        Node(int row, int col) {
            this.row = row;
            this.col = col;
        }

        int getFCost() {
            return gCost + hCost;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }
    }

    private List<Node> findPath(int startRow, int startCol, int goalRow, int goalCol, int[][] maze) {
        Node[][] grid = new Node[maze.length][maze[0].length];
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                grid[i][j] = new Node(i, j);
            }
        }

        Node startNode = grid[startRow][startCol];
        Node goalNode = grid[goalRow][goalCol];

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::getFCost));
        List<Node> closedSet = new ArrayList<>();

        startNode.gCost = 0;
        startNode.hCost = calculateHeuristic(startNode, goalNode);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();

            if (currentNode == goalNode) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode);

            for (Node neighbor : getNeighbors(currentNode, maze, grid)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                int tentativeGCost = currentNode.gCost + 1; // Assuming cost of 1 per move

                if (!openSet.contains(neighbor) || tentativeGCost < neighbor.gCost) {
                    neighbor.parent = currentNode;
                    neighbor.gCost = tentativeGCost;
                    neighbor.hCost = calculateHeuristic(neighbor, goalNode);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null; // No path found
    }

    private List<Node> reconstructPath(Node endNode) {
        List<Node> path = new ArrayList<>();
        Node currentNode = endNode;

        while (currentNode != null) {
            path.add(currentNode);
            currentNode = currentNode.parent;
        }

        Collections.reverse(path);
        return path;
    }

    private List<Node> getNeighbors(Node node, int[][] maze, Node[][] grid) {
        List<Node> neighbors = new ArrayList<>();
        int[] dx = { -1, 1, 0, 0 };
        int[] dy = { 0, 0, -1, 1 };

        for (int i = 0; i < 4; i++) {
            int newRow = node.row + dx[i];
            int newCol = node.col + dy[i];

            if (newRow >= 0 && newRow < maze.length &&
                    newCol >= 0 && newCol < maze[0].length &&
                    maze[newRow][newCol] != MazeLogic.WALL) {

                neighbors.add(grid[newRow][newCol]);
            }
        }
        return neighbors;
    }

    private int calculateHeuristic(Node a, Node b) {
        // Manhattan distance heuristic
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    private void patrol(int[][] maze) {
        if (!isMoving) {
            if (row == patrolTargetRow && col == patrolTargetCol) {
                setRandomPatrolTarget(maze);
            }

            List<int[]> validMoves = getValidMoves(maze);

            if (!validMoves.isEmpty()) {
                // Choose a random valid move, avoiding immediate backtracking
                int randomIndex;
                do {
                    randomIndex = random.nextInt(validMoves.size());
                } while (validMoves.size() > 1 &&
                        validMoves.get(randomIndex)[0] == -prevDirectionY &&
                        validMoves.get(randomIndex)[1] == -prevDirectionX);

                int[] newDirection = validMoves.get(randomIndex);
                prevDirectionY = newDirection[0];
                prevDirectionX = newDirection[1];

                if (newDirection[0] == -1) {
                    moveUp();
                } else if (newDirection[0] == 1) {
                    moveDown();
                } else if (newDirection[1] == -1) {
                    moveLeft();
                } else if (newDirection[1] == 1) {
                    moveRight();
                }
            }
        }
    }

    private List<int[]> getValidMoves(int[][] maze) {
        List<int[]> validMoves = new ArrayList<>();
        int[] directions = {-1, 0, 1, 0, 0, -1, 0, 1}; // Up, Down, Left, Right

        for (int i = 0; i < directions.length - 1; i += 2) {
            int newRow = row + directions[i];
            int newCol = col + directions[i + 1];

            if (canMove(maze, newRow, newCol)) {
                validMoves.add(new int[]{directions[i], directions[i + 1]});
            }
        }

        return validMoves;
    }

    private void moveUp() {
        if (!isMoving && canMove(mazePanel.getMaze(), row - 1, col)) {
            isMoving = true;
            this.currentState = EnemyAnimationState.WALKING_BACKWARD;
            double targetY = this.y - MazePanel.CELL_SIZE;

            Timer timer = new Timer(10, e -> {
                if (y > targetY) {
                    y -= speed * 0.01 * 2;
                } else {
                    y = targetY;
                    row--;
                    isMoving = false;
                    ((Timer) e.getSource()).stop();
                }
                mazePanel.repaint();
            });
            timer.start();
        }
    }

    private void moveDown() {
        if (!isMoving && canMove(mazePanel.getMaze(), row + 1, col)) {
            isMoving = true;
            this.currentState = EnemyAnimationState.WALKING_FORWARD;
            double targetY = this.y + MazePanel.CELL_SIZE;
            Timer timer = new Timer(10, e -> {
                if (y < targetY) {
                    y += speed * 0.01 * 2;
                } else {
                    y = targetY;
                    row++;
                    isMoving = false;
                    ((Timer) e.getSource()).stop();
                }
                mazePanel.repaint();
            });
            timer.start();
        }
    }

    private void moveLeft() {
        if (!isMoving && canMove(mazePanel.getMaze(), row, col - 1)) {
            isMoving = true;
            this.currentState = EnemyAnimationState.WALKING_FORWARD;
            double targetX = this.x - MazePanel.CELL_SIZE;
            Timer timer = new Timer(10, e -> {
                if (x > targetX) {
                    x -= speed * 0.01 * 2;
                } else {
                    x = targetX;
                    col--;
                    isMoving = false;
                    ((Timer) e.getSource()).stop();
                }
                mazePanel.repaint();
            });
            timer.start();
        }
    }

    private void moveRight() {
        if (!isMoving && canMove(mazePanel.getMaze(), row, col + 1)) {
            isMoving = true;
            this.currentState = EnemyAnimationState.WALKING_BACKWARD;
            double targetX = this.x + MazePanel.CELL_SIZE;
            Timer timer = new Timer(10, e -> {
                if (x < targetX) {
                    x += speed * 0.01 * 2;
                } else {
                    x = targetX;
                    col++;
                    isMoving = false;
                    ((Timer) e.getSource()).stop();
                }
                mazePanel.repaint();
            });
            timer.start();
        }
    }

    public void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastFrameTime + 100) {
            currentFrame = (currentFrame + 1) % getCurrentAnimationFrames().size();
            lastFrameTime = currentTime;
        }
    }

    private List<BufferedImage> getCurrentAnimationFrames() {
        return switch (currentState) {
            case WALKING_FORWARD -> walkForwardFrames;
            case WALKING_BACKWARD -> walkBackwardFrames;
            default -> enemyIdleFrames;
        };
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public BufferedImage getImage() {
        return switch (currentState) {
            case WALKING_FORWARD -> walkBackwardFrames.get(currentFrame);
            case WALKING_BACKWARD -> walkForwardFrames.get(currentFrame);
            default -> enemyIdleFrames.get(currentFrame);
        };
    }
}