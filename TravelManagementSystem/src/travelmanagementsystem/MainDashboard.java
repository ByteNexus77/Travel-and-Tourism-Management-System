

package travelmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.io.File;
import javax.imageio.ImageIO;





public class MainDashboard extends JFrame {
    String loggedUser;
    Connection conn;
    JLabel lblProfilePic; 

    public MainDashboard(String username) {
        this.loggedUser = username;
        conn = javaconnect.connectdb();
        
        setTitle("Travel System - Dashboard");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JPanel bg = new JPanel(null);
        bg.setBackground(new Color(135, 206, 250));
        bg.setBounds(0, 0, 1000, 600);
        add(bg);

        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBounds(100, 50, 800, 480);
        bg.add(panel);

        JLabel imgSide = new JLabel();
        imgSide.setBounds(0, 0, 370, 480);
        panel.add(imgSide);
        ImageResizer.setScaledImage(imgSide, "/icon/dask4.jpg", 370, 480);

        lblProfilePic = new JLabel();
        lblProfilePic.setBounds(550, 20, 70, 70); 
        panel.add(lblProfilePic);
        
        loadUserProfilePic();

        JLabel welcome = new JLabel("WELCOME, " + loggedUser.toUpperCase());
        welcome.setFont(new Font("Apple Chancery", Font.BOLD, 20));
        welcome.setForeground(new Color(102, 102, 255));
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        welcome.setBounds(420, 95, 330, 30); 
        panel.add(welcome);

        
        
        
        int rCount = getCount("SELECT SUM(available_rooms) FROM hotel_inventory");
        JButton btnRoom = new JButton("BOOK HOTEL ROOM (" + rCount + ")");
        btnRoom.setBounds(420, 140, 330, 45);
        btnRoom.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnRoom.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(btnRoom);

        
        int cCount = getCount("SELECT SUM(available_cars) FROM car_inventory");
        JButton btnCar = new JButton(" RENT A CAR (" + cCount + ")");
        btnCar.setBounds(420, 195, 330, 45);
        btnCar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnCar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(btnCar);

        
        JButton btnProfile = new JButton(" MY PROFILE MANAGEMENT");
        btnProfile.setBounds(420, 250, 330, 45);
        btnProfile.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnProfile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(btnProfile);

        
        
        
        JButton btnHistory = new JButton(" MY HISTORY & CANCEL");
        btnHistory.setBounds(420, 305, 330, 45);
        btnHistory.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnHistory.setBackground(new Color(245, 245, 245));
        btnHistory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(btnHistory);

        
       
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBounds(535, 390, 100, 30);
        btnLogout.setBackground(new Color(255, 102, 102));
        btnLogout.setForeground(Color.WHITE);
        panel.add(btnLogout);

        
        btnRoom.addActionListener(e -> { new RoomBookingPage(loggedUser).setVisible(true); this.dispose(); });
        btnCar.addActionListener(e -> { new CarRentalPage(loggedUser).setVisible(true); this.dispose(); });
        btnProfile.addActionListener(e -> { new ProfilePage(loggedUser).setVisible(true); this.dispose(); });
        
        
        btnHistory.addActionListener(e -> { 
            new MyBookingsPage(loggedUser).setVisible(true); 
            this.dispose(); 
        });

        btnLogout.addActionListener(e -> { new login().setVisible(true); this.dispose(); });
    }

    
    
    
    private ImageIcon getRoundImage(String path, int size) {
        try {
            BufferedImage master = ImageIO.read(new File(path));
            int diameter = Math.min(master.getWidth(), master.getHeight());
            BufferedImage mask = new BufferedImage(master.getWidth(), master.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = mask.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.fill(new Ellipse2D.Double(0, 0, diameter, diameter));
            g2d.dispose();
            BufferedImage rounded = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            g2d = rounded.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setClip(new Ellipse2D.Double(0, 0, diameter, diameter));
            g2d.drawImage(master, 0, 0, diameter, diameter, null);
            g2d.dispose();
            return new ImageIcon(rounded.getScaledInstance(size, size, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    
    
    private void loadUserProfilePic() {
        try {
            PreparedStatement pst = conn.prepareStatement("SELECT ProfilePic FROM THBS WHERE UserName = ?");
            pst.setString(1, loggedUser);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String imagePath = rs.getString("ProfilePic");
                if (imagePath != null && !imagePath.isEmpty()) {
                    ImageIcon roundIcon = getRoundImage(imagePath, 70);
                    if (roundIcon != null) lblProfilePic.setIcon(roundIcon);
                    else setDefaultProfilePic();
                } else setDefaultProfilePic();
            }
        } catch (Exception e) { setDefaultProfilePic(); }
    }

    private void setDefaultProfilePic() {
        lblProfilePic.setText("Profile");
        lblProfilePic.setHorizontalAlignment(SwingConstants.CENTER);
        lblProfilePic.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }

    
    
    private int getCount(String query) {
        try {
            ResultSet rs = conn.prepareStatement(query).executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { }
        return 0;
    }
}