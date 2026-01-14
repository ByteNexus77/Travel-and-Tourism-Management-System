

package travelmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;
import java.util.Date;

public class CarRentalPage extends JFrame {
    String loggedUser;
    JComboBox<String> carBox;
    JDateChooser pickup, drop;
    JLabel lblPrice, lblTotal; 
    Connection conn;

    public CarRentalPage(String username) {
        this.loggedUser = username;
        conn = javaconnect.connectdb();
        
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(null);

        
        JPanel bg = new JPanel(null);
        bg.setBackground(new Color(135, 206, 250));
        bg.setBounds(0, 0, 1000, 600);
        add(bg);

        
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBounds(100, 50, 800, 450);
        bg.add(panel);

        
        JLabel img = new JLabel();
        img.setBounds(0, 0, 370, 450);
        panel.add(img);
        ImageResizer.setScaledImage(img, "/icon/car1.jpeg", 370, 450);

        
        JLabel title = new JLabel("CAR RENTAL & BILLING");
        title.setFont(new Font("Apple Chancery", Font.BOLD, 24));
        title.setForeground(new Color(102, 102, 255));
        title.setBounds(420, 30, 350, 40);
        panel.add(title);

        
        carBox = new JComboBox<>();
        carBox.setBounds(520, 100, 230, 30);
        panel.add(carBox);
        loadCars();

        
        lblPrice = new JLabel("Price: 0 BDT");
        lblPrice.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblPrice.setBounds(520, 130, 230, 20);
        panel.add(lblPrice);

        
        pickup = new JDateChooser();
        pickup.setBounds(520, 160, 230, 30);
        panel.add(pickup);

        
        drop = new JDateChooser();
        drop.setBounds(520, 220, 230, 30);
        panel.add(drop);

        
        lblTotal = new JLabel("Total Bill: 0 BDT");
        lblTotal.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblTotal.setForeground(new Color(102, 102, 255));
        lblTotal.setBounds(520, 270, 230, 25);
        panel.add(lblTotal);

        
        JButton btnRent = new JButton("Rent & Get Bill");
        btnRent.setBounds(520, 320, 180, 40);
        btnRent.setBackground(new Color(102, 102, 255));
        btnRent.setForeground(Color.WHITE);
        panel.add(btnRent);

        JButton btnBack = new JButton("Back");
        btnBack.setBounds(420, 320, 90, 40);
        panel.add(btnBack);

       
        carBox.addActionListener(e -> updateCalculations());
        pickup.addPropertyChangeListener(e -> updateCalculations());
        drop.addPropertyChangeListener(e -> updateCalculations());

        btnBack.addActionListener(e -> { new MainDashboard(loggedUser).setVisible(true); this.dispose(); });
        btnRent.addActionListener(e -> rentAndBill());
    }

    private void loadCars() {
        try {
            ResultSet rs = conn.prepareStatement("SELECT car_type, available_cars FROM car_inventory").executeQuery();
            while (rs.next()) {
                carBox.addItem(rs.getString(1) + " (" + rs.getInt(2) + ")");
            }
            updateCalculations();
        } catch (Exception e) {}
    }

    private int getPrice() {
        if (carBox.getSelectedItem() == null) return 0;
        String s = carBox.getSelectedItem().toString();
        if (s.contains("SUV")) return 5000;
        if (s.contains("Sedan")) return 3000;
        return 2000;
    }

    private void updateCalculations() {
        int price = getPrice();
        lblPrice.setText("Price Per Day: " + price + " BDT");
        if (pickup.getDate() != null && drop.getDate() != null) {
            long days = (drop.getDate().getTime() - pickup.getDate().getTime()) / (1000 * 60 * 60 * 24);
            if (days <= 0) days = 1;
            lblTotal.setText("Total: " + (days * price) + " BDT (" + days + " Days)");
        }
    }

    private void rentAndBill() {
        try {
            if (pickup.getDate() == null || drop.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Select Dates!");
                return;
            }
            String type = carBox.getSelectedItem().toString().split(" ")[0];
            long days = (drop.getDate().getTime() - pickup.getDate().getTime()) / (1000 * 60 * 60 * 24);
            if (days <= 0) days = 1;
            int total = (int) days * getPrice();

            PreparedStatement pst = conn.prepareStatement("INSERT INTO car_rent(username, car_type, pickup, return_date, total_bill, status) VALUES (?,?,?,?,?,?)");
            pst.setString(1, loggedUser);
            pst.setString(2, type);
            pst.setDate(3, new java.sql.Date(pickup.getDate().getTime()));
            pst.setDate(4, new java.sql.Date(drop.getDate().getTime()));
            pst.setInt(5, total);
            pst.setString(6, "RENTED");
            pst.executeUpdate();

            conn.prepareStatement("UPDATE car_inventory SET available_cars=available_cars-1 WHERE car_type='"+type+"'").executeUpdate();
            JOptionPane.showMessageDialog(this, "Rent Successful!\nTotal: " + total + " BDT");
            new MainDashboard(loggedUser).setVisible(true);
            this.dispose();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error!"); }
    }
}