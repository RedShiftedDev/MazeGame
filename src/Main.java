import javax.swing.*;
import java.awt.*;

public class Main {

    private static final int SCREEN_WIDTH = 1710;
    private static final int SCREEN_HEIGHT = 1050;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Maze Game");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
            frame.add(new GameMenu(frame));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    if (JOptionPane.showConfirmDialog(frame,
                            "Are you sure you want to exit?", "Confirm Exit",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                }
            });
            frame.setVisible(true);
        });
    }
}
