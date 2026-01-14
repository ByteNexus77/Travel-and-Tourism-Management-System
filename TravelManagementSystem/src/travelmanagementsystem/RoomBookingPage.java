
package travelmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;
import java.util.Date;

public class RoomBookingPage extends JFrame {
    String loggedUser;
    JComboBox<String> roomBox;
    JComboBox<Integer> personBox; 
    JDateChooser checkIn, checkOut;
    JLabel lblPrice, lblTotal; 
    Connection conn;

    public RoomBookingPage(String username) {
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
        panel.setBounds(100, 40, 800, 480);
        bg.add(panel);

        JLabel img = new JLabel();
        img.setBounds(0, 0, 370, 480);
        panel.add(img);
        ImageResizer.setScaledImage(img, "/icon/hotel3.jpg", 370, 480);

        JLabel title = new JLabel("HOTEL BOOKING & BILLING");
        title.setFont(new Font("Apple Chancery", Font.BOLD, 24));
        title.setForeground(new Color(102, 102, 255));
        title.setBounds(420, 20, 350, 40);
        panel.add(title);

       
        
        JLabel l1 = new JLabel("Select Room:");
        l1.setBounds(420, 80, 100, 30);
        panel.add(l1);
        roomBox = new JComboBox<>();
        roomBox.setBounds(530, 80, 220, 30);
        panel.add(roomBox);

        
        lblPrice = new JLabel("Price Per Day: 0 BDT");
        lblPrice.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblPrice.setForeground(Color.DARK_GRAY);
        lblPrice.setBounds(530, 110, 220, 20);
        panel.add(lblPrice);

        
        
        JLabel l2 = new JLabel("Persons:");
        l2.setBounds(420, 140, 100, 30);
        panel.add(l2);
        Integer[] persons = {1, 2, 3, 4};
        personBox = new JComboBox<>(persons);
        personBox.setBounds(530, 140, 220, 30);
        panel.add(personBox);

        
        
        JLabel l3 = new JLabel("Check-in:");
        l3.setBounds(420, 190, 100, 30);
        panel.add(l3);
        checkIn = new JDateChooser();
        checkIn.setBounds(530, 190, 220, 30);
        panel.add(checkIn);

        
        
        JLabel l4 = new JLabel("Check-out:");
        l4.setBounds(420, 240, 100, 30);
        panel.add(l4);
        checkOut = new JDateChooser();
        checkOut.setBounds(530, 240, 220, 30);
        panel.add(checkOut);

        
        
        lblTotal = new JLabel("Total Bill: 0 BDT");
        lblTotal.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblTotal.setForeground(new Color(0, 153, 51));
        lblTotal.setBounds(420, 300, 330, 30);
        panel.add(lblTotal);

       
        
        JButton btnBook = new JButton("Book & Generate Bill");
        btnBook.setBounds(530, 360, 180, 40);
        btnBook.setBackground(new Color(102, 102, 255));
        btnBook.setForeground(Color.WHITE);
        panel.add(btnBook);

        JButton btnBack = new JButton("Back");
        btnBack.setBounds(420, 360, 90, 40);
        panel.add(btnBack);

        loadRooms();

        
        
        roomBox.addActionListener(e -> updateCalculations());
        personBox.addActionListener(e -> updateCalculations());
       
        
        checkIn.addPropertyChangeListener(e -> updateCalculations());
        checkOut.addPropertyChangeListener(e -> updateCalculations());

        btnBack.addActionListener(e -> { new MainDashboard(loggedUser).setVisible(true); this.dispose(); });
        btnBook.addActionListener(e -> bookAndBill());
    }

    private void loadRooms() {
        try {
            ResultSet rs = conn.prepareStatement("SELECT room_type, available_rooms FROM hotel_inventory").executeQuery();
            while (rs.next()) {
                roomBox.addItem(rs.getString(1) + " (" + rs.getInt(2) + ")");
            }
            updateCalculations(); 
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int getPrice() {
        if (roomBox.getSelectedItem() == null) return 0;
        String selected = roomBox.getSelectedItem().toString();
        if (selected.contains("Deluxe")) return 5000;
        if (selected.contains("Double")) return 2000;
        return 1000; 
        
    }

    private void updateCalculations() {
        int price = getPrice();
        lblPrice.setText("Price Per Day: " + price + " BDT");

        if (checkIn.getDate() != null && checkOut.getDate() != null) {
            long diff = checkOut.getDate().getTime() - checkIn.getDate().getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            if (days <= 0) days = 1; 

            int total = (int) (days * price);
            lblTotal.setText("Total Bill: " + total + " BDT (" + days + " Days)");
        }
    }

    private void bookAndBill() {
        try {
            Date d1 = checkIn.getDate();
            Date d2 = checkOut.getDate();

            if (d1 == null || d2 == null) {
                JOptionPane.showMessageDialog(this, "Please select Dates!");
                return;
            }

            String selected = roomBox.getSelectedItem().toString();
            String type = selected.split(" ")[0];
            int persons = (int) personBox.getSelectedItem();
            
            long days = (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24);
            if (days <= 0) days = 1;

            int price = getPrice();
            int total = (int) days * price;

            
            
            String query = "INSERT INTO hotel_booking(username, room_type, persons, checkin, checkout, total_bill, status) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, loggedUser);
            pst.setString(2, type);
            pst.setInt(3, persons);
            pst.setDate(4, new java.sql.Date(d1.getTime()));
            pst.setDate(5, new java.sql.Date(d2.getTime()));
            pst.setInt(6, total);
            pst.setString(7, "BOOKED");
            pst.executeUpdate();

           
            
            conn.prepareStatement("UPDATE hotel_inventory SET available_rooms = available_rooms - 1 WHERE room_type = '" + type + "'").executeUpdate();

            String receipt = "---------- RECEIPT ----------\n" +
                             "Customer: " + loggedUser + "\n" +
                             "Room Type: " + type + "\n" +
                             "Persons: " + persons + "\n" +
                             "Duration: " + days + " Days\n" +
                             "Total Bill: " + total + " BDT\n" +
                             "-----------------------------------";
            
            JOptionPane.showMessageDialog(this, receipt, "Booking Successful", JOptionPane.INFORMATION_MESSAGE);
            
           
            
            new MainDashboard(loggedUser).setVisible(true);
            this.dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error in Booking! Check Database columns.");
        }
    }
}