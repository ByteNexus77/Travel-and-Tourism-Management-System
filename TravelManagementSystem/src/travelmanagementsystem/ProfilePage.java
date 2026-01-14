

package travelmanagementsystem;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.security.MessageDigest;

public class ProfilePage extends JFrame {
    String loggedUser;
    JTextField txtPhone, txtEmail, txtUser;
    JPasswordField txtPass;
    JLabel lblTotalBookings, imgLabel; 
    Connection conn;
    String selectedImagePath = null; 

    public ProfilePage(String username) {
        this.loggedUser = username;
        conn = javaconnect.connectdb(); 
        
        setTitle("Travel System - My Profile");
        setSize(1000, 650); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JPanel bg = new JPanel(null);
        bg.setBackground(new Color(135, 206, 250));
        bg.setBounds(0, 0, 1000, 650);
        add(bg);

        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBounds(100, 50, 800, 520);
        bg.add(panel);

        
        
        
        
        imgLabel = new JLabel();
        imgLabel.setBounds(20, 60, 330, 330);
        imgLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        panel.add(imgLabel);

        
        JButton btnUpload = new JButton("Change Photo");
        btnUpload.setBounds(110, 410, 150, 30);
        panel.add(btnUpload);

        JLabel title = new JLabel("MANAGE PROFILE");
        title.setFont(new Font("Tahoma", Font.BOLD, 26));
        title.setForeground(new Color(102, 102, 255));
        title.setBounds(420, 20, 350, 40);
        panel.add(title);

        JLabel l1 = new JLabel("Username:");
        l1.setBounds(420, 80, 100, 30);
        panel.add(l1);
        txtUser = new JTextField(loggedUser);
        txtUser.setEditable(false);
        txtUser.setBounds(530, 80, 220, 30);
        panel.add(txtUser);

        JLabel l2 = new JLabel("Phone:");
        l2.setBounds(420, 140, 100, 30);
        panel.add(l2);
        txtPhone = new JTextField();
        txtPhone.setBounds(530, 140, 220, 30);
        panel.add(txtPhone);

        JLabel l3 = new JLabel("Email:");
        l3.setBounds(420, 200, 100, 30);
        panel.add(l3);
        txtEmail = new JTextField();
        txtEmail.setBounds(530, 200, 220, 30);
        panel.add(txtEmail);

        JLabel l4 = new JLabel("New Password:");
        l4.setBounds(420, 260, 120, 30);
        panel.add(l4);
        txtPass = new JPasswordField();
        txtPass.setBounds(530, 260, 220, 30);
        panel.add(txtPass);

        lblTotalBookings = new JLabel("Loading Bookings...");
        lblTotalBookings.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblTotalBookings.setForeground(new Color(0, 102, 51));
        lblTotalBookings.setBounds(420, 330, 350, 30);
        panel.add(lblTotalBookings);

        JButton btnUp = new JButton("Update Profile");
        btnUp.setBounds(530, 410, 150, 40);
        btnUp.setBackground(new Color(102, 102, 255));
        btnUp.setForeground(Color.WHITE);
        panel.add(btnUp);

        JButton btnBack = new JButton("Back");
        btnBack.setBounds(420, 410, 100, 40);
        panel.add(btnBack);

        fetchUserData();

      
        btnUpload.addActionListener(e -> selectImage());

        btnBack.addActionListener(e -> { new MainDashboard(loggedUser).setVisible(true); this.dispose(); });
        btnUp.addActionListener(e -> updateProfile());
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "png", "jpeg");
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();
           
            
            ImageIcon icon = new ImageIcon(selectedImagePath);
            Image img = icon.getImage().getScaledInstance(imgLabel.getWidth(), imgLabel.getHeight(), Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(img));
        }
    }

    private void fetchUserData() {
        try {
            PreparedStatement pst1 = conn.prepareStatement("SELECT * FROM THBS WHERE UserName = ?");
            pst1.setString(1, loggedUser);
            ResultSet rs1 = pst1.executeQuery();
            if (rs1.next()) {
                txtPhone.setText(rs1.getString("Phone"));
                txtEmail.setText(rs1.getString("Email"));
                
                
                
                String dbImagePath = rs1.getString("ProfilePic");
                if (dbImagePath != null && !dbImagePath.isEmpty()) {
                    ImageIcon icon = new ImageIcon(dbImagePath);
                    Image img = icon.getImage().getScaledInstance(imgLabel.getWidth(), imgLabel.getHeight(), Image.SCALE_SMOOTH);
                    imgLabel.setIcon(new ImageIcon(img));
                } else {
                   
                    
                    
                    ImageResizer.setScaledImage(imgLabel, "/icon/user_profile.jpg", 330, 330);
                }
            }
            
            
            
            PreparedStatement pst2 = conn.prepareStatement("SELECT COUNT(*) FROM hotel_booking WHERE username = ?");
            pst2.setString(1, loggedUser);
            ResultSet rs2 = pst2.executeQuery();
            int hotelCount = rs2.next() ? rs2.getInt(1) : 0;

            PreparedStatement pst3 = conn.prepareStatement("SELECT COUNT(*) FROM car_rent WHERE username = ?");
            pst3.setString(1, loggedUser);
            ResultSet rs3 = pst3.executeQuery();
            int carCount = rs3.next() ? rs3.getInt(1) : 0;

            lblTotalBookings.setText("Total: " + (hotelCount + carCount) + " (Hotel: " + hotelCount + ", Car: " + carCount + ")");
        } catch (Exception e) { e.printStackTrace(); }
    }

    
    
    private void updateProfile() {
        try {
            String phone = txtPhone.getText();
            String email = txtEmail.getText();
            String pass = new String(txtPass.getPassword());

            if (phone.equals("") || email.equals("")) {
                JOptionPane.showMessageDialog(this, "Phone and Email are required!");
                return;
            }

            StringBuilder query = new StringBuilder("UPDATE THBS SET Phone = ?, Email = ?");
            if (!pass.isEmpty()) query.append(", Password = ?");
            if (selectedImagePath != null) query.append(", ProfilePic = ?");
            query.append(" WHERE UserName = ?");

            PreparedStatement pst = conn.prepareStatement(query.toString());
            int paramIndex = 1;
            pst.setString(paramIndex++, phone);
            pst.setString(paramIndex++, email);
            
            if (!pass.isEmpty()) {
                pst.setString(paramIndex++, hashPassword(pass));
            }
            if (selectedImagePath != null) {
                pst.setString(paramIndex++, selectedImagePath);
            }
            pst.setString(paramIndex, loggedUser);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Profile Updated Successfully!");
            txtPass.setText(""); 
            fetchUserData();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Update Failed!");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }
}