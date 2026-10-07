package FProject;

import javax.swing.*;
import java.awt.*;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imageFileName, LayoutManager layout) {
        super(layout);
        try {
            backgroundImage = new ImageIcon(getClass().getResource("iscp_bg.jpg")).getImage();
        } catch (Exception e) {
            try {
                backgroundImage = new ImageIcon("iscp_bg.jpg").getImage();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(
                    new GradientPaint(0, 0, new Color(40, 60, 90), getWidth(), getHeight(), new Color(20, 30, 50)));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}