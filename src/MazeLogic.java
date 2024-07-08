import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MazeLogic {

    public static final int FLOOR = 0;
    public static final int WALL = 1;
    public static final int SPIKE = 2;
    public static final int TREASURE = 3;
    public static final int COIN = 4;
    public static final int KEY = 5;
    public static final int EXIT = 9;

    private static final Random random = new Random();

    public static int[][] generateMaze(int rows, int cols) {
        int[][] maze = new int[rows * 2 + 1][cols * 2 + 1];

        // Initialize maze with walls
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                maze[i][j] = WALL;
            }
        }

        // Start maze generation from a random cell
        int startX = random.nextInt(rows) * 2 + 1;
        int startY = random.nextInt(cols) * 2 + 1;
        carvePassages(maze, startX, startY);

        // Place special items (updated logic for random placement)
        placeSpecialItems(maze, rows, cols);

        return maze;
    }

    private static void carvePassages(int[][] maze, int currentRow, int currentCol) {
        maze[currentRow][currentCol] = FLOOR;

        List<int[]> directions = new ArrayList<>();
        directions.add(new int[]{0, 2}); // Up
        directions.add(new int[]{0, -2}); // Down
        directions.add(new int[]{2, 0}); // Right
        directions.add(new int[]{-2, 0}); // Left
        Collections.shuffle(directions);

        for (int[] direction : directions) {
            int newRow = currentRow + direction[0];
            int newCol = currentCol + direction[1];

            if (isValidCell(maze, newRow, newCol)) {
                maze[currentRow + direction[0] / 2][currentCol + direction[1] / 2] = FLOOR;
                carvePassages(maze, newRow, newCol);
            }
        }
    }

    private static boolean isValidCell(int[][] maze, int row, int col) {
        return (row > 0 && row < maze.length - 1 &&
                col > 0 && col < maze[0].length - 1 &&
                maze[row][col] == WALL);
    }


    // Function to place special items randomly in the maze
    private static void placeSpecialItems(int[][] maze, int rows, int cols) {
        placeExit(maze, rows, cols);
        placeItemRandomly(maze, SPIKE, (int) (rows * cols * 0.05));  // 5% spikes
        placeItemRandomly(maze, TREASURE, 2);
        placeItemRandomly(maze, KEY, 1);
        placeCoinsInTrails(maze, (int) (rows * cols * 0.20));
    }

    private static void placeCoinsInTrails(int[][] maze, int totalCoins) {
        int placedCoins = 0;
        while (placedCoins < totalCoins) {
            // Choose a random starting point on a FLOOR tile
            int row, col;
            do {
                row = random.nextInt(maze.length / 2) * 2 + 1;
                col = random.nextInt(maze[0].length / 2) * 2 + 1;
            } while (maze[row][col] != FLOOR);

            List<int[]> directions = new ArrayList<>();
            checkAndSet(maze, row, col, directions);

            // Place coins along the trail
            int trailLength = random.nextInt(3) + 3; // Trail of 3 to 5 coins
            int currentRow = row;
            int currentCol = col;

            for (int i = 0; i < trailLength && placedCoins < totalCoins; i++) {
                // Place coin if cell is a floor tile
                if (maze[currentRow][currentCol] == FLOOR) {
                    maze[currentRow][currentCol] = COIN;
                    placedCoins++;
                }

                // If there are possible directions, choose one and move
                if (!directions.isEmpty()) {
                    int randomIndex = random.nextInt(directions.size());
                    int[] direction = directions.get(randomIndex);
                    currentRow += direction[0];
                    currentCol += direction[1];

                    // Remove the chosen direction to avoid immediate backtracking
                    directions.remove(randomIndex);

                    // Update possible directions after moving (avoiding walls and going back)
                    directions.clear();
                    checkAndSet(maze, row, col, directions);
                } else {
                    // No more valid directions from this point, break the loop
                    break;
                }
            }
        }
    }

    private static void checkAndSet(int[][] maze, int row, int col, List<int[]> directions) {
        if (row > 0 && maze[row - 1][col] == FLOOR) directions.add(new int[]{-1, 0}); // Up
        if (row < maze.length - 1 && maze[row + 1][col] == FLOOR) directions.add(new int[]{1, 0}); // Down
        if (col > 0 && maze[row][col - 1] == FLOOR) directions.add(new int[]{0, -1}); // Left
        if (col < maze[0].length - 1 && maze[row][col + 1] == FLOOR) directions.add(new int[]{0, 1}); // Right
    }

    // Function to place an item type randomly in the maze
    private static void placeItemRandomly(int[][] maze, int itemType, int count) {
        int placed = 0;
        while (placed < count) {
            int row = random.nextInt(maze.length / 2) * 2 + 1;
            int col = random.nextInt(maze[0].length / 2) * 2 + 1;
            if (maze[row][col] == FLOOR) {
                maze[row][col] = itemType;
                placed++;
            }
        }
    }

    // Function to place the exit
    private static void placeExit(int[][] maze, int rows, int cols) {
        int exitRow, exitCol;
        do {
            // Choose a random side for the exit
            int side = random.nextInt(4);

            exitCol = switch (side) {
                case 0 -> {
                    exitRow = 1;
                    yield random.nextInt(cols) * 2 + 1; // Top
                }
                case 1 -> {
                    exitRow = random.nextInt(rows) * 2 + 1;
                    yield maze[0].length - 2; // Right
                }
                case 2 -> {
                    exitRow = maze.length - 2;
                    yield random.nextInt(cols) * 2 + 1; // Bottom
                }
                default -> {
                    exitRow = random.nextInt(rows) * 2 + 1;
                    yield 1; // Left
                }
            };
        } while (maze[exitRow][exitCol] != FLOOR);

        maze[exitRow][exitCol] = EXIT;
    }
}