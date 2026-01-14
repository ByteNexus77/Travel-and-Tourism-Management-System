package travelmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MyBookingsPage extends JFrame {
    String loggedUser;
    Connection conn;
    JTable table;
    DefaultTableModel model;
    JComboBox<String> typeBox;

    public MyBookingsPage(String username) {
        this.loggedUser = username;
        conn = javaconnect.connectdb();

        setTitle("My Travel History & Cancellation");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);

        
        JPanel bg = new JPanel(null);
        bg.setBackground(new Color(135, 206, 250));
        bg.setBounds(0, 0, 1000, 600);
        add(bg);

        
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setBounds(50, 50, 900, 460);
        bg.add(panel);

        JLabel title = new JLabel("My Bookings & History");
        title.setFont(new Font("Apple Chancery", Font.BOLD, 26));
        title.setForeground(new Color(102, 102, 255));
        title.setBounds(300, 20, 400, 40);
        panel.add(title);

        
        JLabel lbl = new JLabel("View History For:");
        lbl.setFont(new Font("Tahoma", Font.BOLD, 14));
        lbl.setBounds(50, 80, 150, 30);
        panel.add(lbl);

        String[] options = {"Hotel Bookings", "Car Rentals"};
        typeBox = new JComboBox<>(options);
        typeBox.setBounds(200, 80, 150, 30);
        panel.add(typeBox);

        
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(50, 130, 800, 220);
        panel.add(sp);

       
        JButton btnCancel = new JButton(" Cancel Selected Booking");
        btnCancel.setBounds(380, 370, 240, 45);
        btnCancel.setBackground(new Color(255, 102, 102));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(btnCancel);

        JButton btnBack = new JButton("Back to Home");
        btnBack.setBounds(50, 370, 160, 45);
        btnBack.setFont(new Font("Tahoma", Font.BOLD, 12));
        panel.add(btnBack);

       
        loadHistory();

        
        
        
        typeBox.addActionListener(e -> loadHistory());
        btnBack.addActionListener(e -> { 
            new MainDashboard(loggedUser).setVisible(true); 
            this.dispose(); 
        });
        btnCancel.addActionListener(e -> cancelBooking());
    }

    
    
    private void loadHistory() {
        model.setRowCount(0); 
        String selection = typeBox.getSelectedItem().toString();

        try {
            if (selection.equals("Hotel Bookings")) {
              
                model.setColumnIdentifiers(new String[]{"ID", "Room Type", "Check-in", "Check-out", "Total Bill"});
                
                
                String query = "SELECT id, room_type, checkin, checkout, total_bill FROM hotel_booking WHERE username = ?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, loggedUser);
                ResultSet rs = pst.executeQuery();
                
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"), 
                        rs.getString("room_type"), 
                        rs.getString("checkin"), 
                        rs.getString("checkout"), 
                        rs.getInt("total_bill")
                    });
                }
            } else {
               
                model.setColumnIdentifiers(new String[]{"ID", "Car Type", "Pickup", "Return", "Total Bill"});
                
              
                
                String query = "SELECT id, car_type, pickup, return_date, total_bill FROM car_rent WHERE username = ?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, loggedUser);
                ResultSet rs = pst.executeQuery();
                
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"), 
                        rs.getString("car_type"), 
                        rs.getString("pickup"), 
                        rs.getString("return_date"), 
                        rs.getInt("total_bill")
                    });
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
            JOptionPane.showMessageDialog(this, "Error: Could not load data from database!");
        }
    }

    private void cancelBooking() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select  first!");
            return;
        }

       
        int id = (int) model.getValueAt(row, 0);
        String typeName = model.getValueAt(row, 1).toString();
        String selection = typeBox.getSelectedItem().toString();

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this booking?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (selection.equals("Hotel Bookings")) {
                 
                    PreparedStatement pstDel = conn.prepareStatement("DELETE FROM hotel_booking WHERE id=?");
                    pstDel.setInt(1, id);
                    pstDel.executeUpdate();
                    

                    PreparedStatement pstInv = conn.prepareStatement("UPDATE hotel_inventory SET available_rooms = available_rooms + 1 WHERE room_type = ?");
                    pstInv.setString(1, typeName);
                    pstInv.executeUpdate();
                } else {
                    
                    
                    PreparedStatement pstDel = conn.prepareStatement("DELETE FROM car_rent WHERE id=?");
                    pstDel.setInt(1, id);
                    pstDel.executeUpdate();
                    
                   
                    
                    PreparedStatement pstInv = conn.prepareStatement("UPDATE car_inventory SET available_cars = available_cars + 1 WHERE car_type = ?");
                    pstInv.setString(1, typeName);
                    pstInv.executeUpdate();
                }
                
                JOptionPane.showMessageDialog(this, "Booking Cancelled and Inventory Updated!");
                loadHistory(); 
                
                
            } catch (Exception e) { 
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Cancellation failed! Error: " + e.getMessage());
            }
        }
    }
}