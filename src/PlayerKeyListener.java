import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PlayerKeyListener extends KeyAdapter {
    private final MazePanel mazePanel;

    public PlayerKeyListener(MazePanel mazePanel) {
        this.mazePanel = mazePanel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        handleMovement(e, true); // Pass 'true' for key pressed
    }

    @Override
    public void keyReleased(KeyEvent e) {
        handleMovement(e, false); // Pass 'false' for key released
    }

    private void handleMovement(KeyEvent e, boolean isMoving) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_UP:
                mazePanel.movePlayer(KeyEvent.VK_UP, isMoving);
                break;
            case KeyEvent.VK_DOWN:
                mazePanel.movePlayer(KeyEvent.VK_DOWN, isMoving);
                break;
            case KeyEvent.VK_LEFT:
                mazePanel.movePlayer(KeyEvent.VK_LEFT, isMoving);
                break;
            case KeyEvent.VK_RIGHT:
                mazePanel.movePlayer(KeyEvent.VK_RIGHT, isMoving);
                break;
        }
    }
}