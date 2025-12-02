import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormPendaftaranKelas extends JFrame {
    private JTextField txtIdPendaftaran, txtCatatan;
    private JComboBox<String> cmbMember, cmbKelas;
    private JSpinner spinnerTanggal;
    private JTable tablePendaftaran;
    private DefaultTableModel tableModel;
    private JButton btnSimpan, btnDelete, btnReset;
    
    // Database connection details
    private static final String URL = "jdbc:postgresql://localhost:5433/pbo_gym";
    private static final String USER = "postgres";
    private static final String PASSWORD = "audyna11"; // Ganti dengan password Anda
    
    private Connection connection;

    public FormPendaftaranKelas() {
        setTitle("Form Pendaftaran Kelas Gym");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Koneksi database
        connectDatabase();
        
        // Inisialisasi komponen
        initComponents();
        
        // Load data
        loadTableData();
        loadComboBoxData();
    }
    
    private void connectDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Koneksi database berhasil!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi database gagal: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initComponents() {
        // Panel utama
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel Form Input
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Input Data Pendaftaran"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // ID Pendaftaran
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ID Pendaftaran:"), gbc);
        gbc.gridx = 1;
        txtIdPendaftaran = new JTextField(20);
        txtIdPendaftaran.setEditable(false);
        txtIdPendaftaran.setBackground(Color.LIGHT_GRAY);
        formPanel.add(txtIdPendaftaran, gbc);
        
        // Member
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Member:"), gbc);
        gbc.gridx = 1;
        cmbMember = new JComboBox<>();
        formPanel.add(cmbMember, gbc);
        
        // Kelas Gym
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Kelas Gym:"), gbc);
        gbc.gridx = 1;
        cmbKelas = new JComboBox<>();
        formPanel.add(cmbKelas, gbc);
        
        // Tanggal Daftar
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Tanggal Daftar:"), gbc);
        gbc.gridx = 1;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        spinnerTanggal = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerTanggal, "yyyy-MM-dd");
        spinnerTanggal.setEditor(dateEditor);
        formPanel.add(spinnerTanggal, gbc);
        
        // Catatan
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Catatan:"), gbc);
        gbc.gridx = 1;
        txtCatatan = new JTextField(20);
        formPanel.add(txtCatatan, gbc);
        
        // Panel Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnSimpan = new JButton("Simpan");
        btnDelete = new JButton("Delete");
        btnReset = new JButton("Reset");
        
        btnSimpan.setBackground(new Color(46, 204, 113));
        btnSimpan.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);
        btnReset.setBackground(new Color(52, 152, 219));
        btnReset.setForeground(Color.WHITE);
        
        buttonPanel.add(btnSimpan);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnReset);
        
        // Panel Table
        String[] columnNames = {"ID", "Member", "Kelas", "Tanggal Daftar", "Catatan"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablePendaftaran = new JTable(tableModel);
        tablePendaftaran.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablePendaftaran.getSelectedRow() != -1) {
                loadDataToForm();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tablePendaftaran);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Data Pendaftaran Kelas"));
        
        // Add panels to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Event listeners
        btnSimpan.addActionListener(e -> simpanData());
        btnDelete.addActionListener(e -> deleteData());
        btnReset.addActionListener(e -> resetForm());
    }
    
    private void loadComboBoxData() {
        // Load Member
        try {
            String query = "SELECT id_member, nama FROM member_gym ORDER BY nama";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            cmbMember.removeAllItems();
            cmbMember.addItem("-- Pilih Member --");
            
            while (rs.next()) {
                String item = rs.getInt("id_member") + " - " + rs.getString("nama");
                cmbMember.addItem(item);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading member: " + e.getMessage());
        }
        
        // Load Kelas
        try {
            String query = "SELECT id_kelas, nama_kelas, hari, jam_kelas FROM jadwal_kelas ORDER BY nama_kelas";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            cmbKelas.removeAllItems();
            cmbKelas.addItem("-- Pilih Kelas --");
            
            while (rs.next()) {
                String item = rs.getInt("id_kelas") + " - " + rs.getString("nama_kelas") + 
                             " (" + rs.getString("hari") + " " + rs.getTime("jam_kelas") + ")";
                cmbKelas.addItem(item);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading kelas: " + e.getMessage());
        }
    }
    
    private void loadTableData() {
        tableModel.setRowCount(0);
        
        try {
            String query = "SELECT p.id_pendaftaran, m.nama as nama_member, " +
                          "j.nama_kelas, p.tanggal_daftar, p.catatan " +
                          "FROM pendaftaran_kelas p " +
                          "JOIN member_gym m ON p.id_member = m.id_member " +
                          "JOIN jadwal_kelas j ON p.id_kelas = j.id_kelas " +
                          "ORDER BY p.id_pendaftaran DESC";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_pendaftaran"),
                    rs.getString("nama_member"),
                    rs.getString("nama_kelas"),
                    rs.getDate("tanggal_daftar"),
                    rs.getString("catatan")
                };
                tableModel.addRow(row);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void simpanData() {
        // Validasi input
        if (cmbMember.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih member terlebih dahulu!");
            return;
        }
        
        if (cmbKelas.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih kelas terlebih dahulu!");
            return;
        }
        
        try {
            // Ambil ID dari ComboBox
            String memberText = cmbMember.getSelectedItem().toString();
            int idMember = Integer.parseInt(memberText.split(" - ")[0]);
            
            String kelasText = cmbKelas.getSelectedItem().toString();
            int idKelas = Integer.parseInt(kelasText.split(" - ")[0]);
            
            // Format tanggal
            java.util.Date utilDate = (java.util.Date) spinnerTanggal.getValue();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            
            String catatan = txtCatatan.getText().trim();
            
            String query = "INSERT INTO pendaftaran_kelas (id_member, id_kelas, tanggal_daftar, catatan) " +
                          "VALUES (?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, idMember);
            pstmt.setInt(2, idKelas);
            pstmt.setDate(3, sqlDate);
            pstmt.setString(4, catatan.isEmpty() ? null : catatan);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
                loadTableData();
                resetForm();
            }
            
            pstmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error menyimpan data: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    private void deleteData() {
        int selectedRow = tablePendaftaran.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!");
            return;
        }
        
        int idPendaftaran = (int) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus data ini?", 
            "Konfirmasi Hapus", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM pendaftaran_kelas WHERE id_pendaftaran = ?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, idPendaftaran);
                
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                    loadTableData();
                    resetForm();
                }
                
                pstmt.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error menghapus data: " + e.getMessage());
            }
        }
    }
    
    private void resetForm() {
        txtIdPendaftaran.setText("");
        cmbMember.setSelectedIndex(0);
        cmbKelas.setSelectedIndex(0);
        spinnerTanggal.setValue(new java.util.Date());
        txtCatatan.setText("");
        tablePendaftaran.clearSelection();
    }
    
    private void loadDataToForm() {
        int selectedRow = tablePendaftaran.getSelectedRow();
        
        if (selectedRow != -1) {
            txtIdPendaftaran.setText(tableModel.getValueAt(selectedRow, 0).toString());
            
            // Set member dan kelas berdasarkan nama
            String memberName = tableModel.getValueAt(selectedRow, 1).toString();
            String kelasName = tableModel.getValueAt(selectedRow, 2).toString();
            
            for (int i = 0; i < cmbMember.getItemCount(); i++) {
                if (cmbMember.getItemAt(i).contains(memberName)) {
                    cmbMember.setSelectedIndex(i);
                    break;
                }
            }
            
            for (int i = 0; i < cmbKelas.getItemCount(); i++) {
                if (cmbKelas.getItemAt(i).contains(kelasName)) {
                    cmbKelas.setSelectedIndex(i);
                    break;
                }
            }
            
            // Set tanggal
            try {
                java.sql.Date sqlDate = (java.sql.Date) tableModel.getValueAt(selectedRow, 3);
                spinnerTanggal.setValue(new java.util.Date(sqlDate.getTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Set catatan
            Object catatan = tableModel.getValueAt(selectedRow, 4);
            txtCatatan.setText(catatan != null ? catatan.toString() : "");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FormPendaftaranKelas form = new FormPendaftaranKelas();
            form.setVisible(true);
        });
    }
}