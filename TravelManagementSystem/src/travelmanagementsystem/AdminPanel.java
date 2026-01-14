


package travelmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminPanel extends JFrame {

    Connection conn;
    PreparedStatement pst;
    ResultSet rs;
    
    
    JTable userTable;
    DefaultTableModel userModel;
    
  
    JTable invTable;
    DefaultTableModel invModel;

    public AdminPanel() {
        conn = javaconnect.connectdb(); 

        setTitle("Travel System - Admin Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        
        
        JPanel userPanel = createUserApprovalPanel();
        tabbedPane.addTab("User Approvals", userPanel);

        
        
        JPanel inventoryPanel = createInventoryPanel();
        tabbedPane.addTab("Inventory Status", inventoryPanel);

        add(tabbedPane);
        
        loadPendingUsers();
        loadInventoryData();
    }

   
    
    private JPanel createUserApprovalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(135, 206, 250));

        JLabel title = new JLabel("Pending User Approval", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        userModel = new DefaultTableModel(new String[]{"ID", "UserName", "Email", "Role", "Status"}, 0);
        userTable = new JTable(userModel);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton approveBtn = new JButton("Approve User");
        JButton rejectBtn = new JButton("Reject User");
        JButton refreshBtn = new JButton("Refresh List");

        btnPanel.add(approveBtn); btnPanel.add(rejectBtn); btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> updateUserStatus(1));
        rejectBtn.addActionListener(e -> updateUserStatus(2));
        refreshBtn.addActionListener(e -> loadPendingUsers());

        return panel;
    }

    
    
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Current Room & Car Availability", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        
        
        invModel = new DefaultTableModel(new String[]{"Category", "Type", "Available Count"}, 0);
        invTable = new JTable(invModel);
        panel.add(new JScrollPane(invTable), BorderLayout.CENTER);

        
        
        JButton refreshInvBtn = new JButton("Refresh Inventory Data");
        refreshInvBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JPanel southPanel = new JPanel();
        southPanel.add(refreshInvBtn);
        panel.add(southPanel, BorderLayout.SOUTH);

        refreshInvBtn.addActionListener(e -> loadInventoryData());
        
        return panel;
    }

    
    
    void loadInventoryData() {
        try {
            invModel.setRowCount(0);
            
            
            
            ResultSet rsH = conn.prepareStatement("SELECT room_type, available_rooms FROM hotel_inventory").executeQuery();
            while(rsH.next()) invModel.addRow(new Object[]{"Hotel Room", rsH.getString(1), rsH.getInt(2)});
            
           
            
            ResultSet rsC = conn.prepareStatement("SELECT car_type, available_cars FROM car_inventory").executeQuery();
            while(rsC.next()) invModel.addRow(new Object[]{"Car", rsC.getString(1), rsC.getInt(2)});
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    
    
    void loadPendingUsers() {
        try {
            userModel.setRowCount(0);
            pst = conn.prepareStatement("SELECT id, UserName, Email, Role, Status FROM THBS WHERE Status=0");
            rs = pst.executeQuery();
            while (rs.next()) {
                userModel.addRow(new Object[]{rs.getInt("id"), rs.getString("UserName"), rs.getString("Email"), rs.getString("Role"), rs.getInt("Status")});
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e); }
    }

    
    
    void updateUserStatus(int newStatus) {
        int row = userTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a user first"); return; }
        int userId = (int) userModel.getValueAt(row, 0);
        try {
            pst = conn.prepareStatement("UPDATE THBS SET Status=? WHERE id=?");
            pst.setInt(1, newStatus); pst.setInt(2, userId);
            pst.executeUpdate();
            loadPendingUsers();
            JOptionPane.showMessageDialog(this, "Status Updated!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        new AdminPanel().setVisible(true);
    }
}