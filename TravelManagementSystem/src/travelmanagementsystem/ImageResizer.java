package travelmanagementsystem;

import javax.swing.*;
import java.awt.*;

public class ImageResizer {
    
    
    
    
    
    public static void setScaledImage(JLabel label, String imagePath, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(ImageResizer.class.getResource(imagePath));
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            System.out.println("Image path not found: " + imagePath);
        }
    }
}