import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class InstrukturGymApp extends JPanel {
    private JTextField txtNama, txtUsia, txtKeahlian, txtTelp;
    private JTable table;
    private DefaultTableModel model;
    private Connection conn;
    private String selectedId = "";

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/PBO_GYM";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "crazyMamad13*";

    public InstrukturGymApp() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        connectDB();
        initUI();
        loadData();
    }

    private void connectDB() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi Gagal: " + e.getMessage());
        }
    }

    private void initUI() {
        // PANEL INPUT
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Form Data Instruktur"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Inisialisasi komponen
        txtNama = new JTextField(20);
        txtUsia = new JTextField(20);
        txtKeahlian = new JTextField(20);
        
        txtTelp = new JTextField(20);
        txtTelp.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume();
                    return;
                }
                if (txtTelp.getText().length() >= 13) {
                    e.consume();
                    JOptionPane.showMessageDialog(InstrukturGymApp.this,
                        "Nomor telepon maksimal 13 karakter!");
                }
            }
        });

        // Tambah field ke panel
        addField(inputPanel, c, "Nama Instruktur:", txtNama, 0);
        addField(inputPanel, c, "Usia:", txtUsia, 1);
        addField(inputPanel, c, "Keahlian:", txtKeahlian, 2);
        addField(inputPanel, c, "No Telepon:", txtTelp, 3);

        // PANEL TOMBOL
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnSimpan = createButton("Simpan", e -> simpan());
        JButton btnUpdate = createButton("Update", e -> update());
        JButton btnDelete = createButton("Delete", e -> delete());
        JButton btnReset = createButton("Reset", e -> resetForm());
        
        btnPanel.add(btnSimpan);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnReset);

        // Set mode awal (Simpan aktif, Update/Delete non-aktif)
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        // PANEL TABEL
        model = new DefaultTableModel(
            new String[]{"ID", "Nama", "Usia", "Keahlian", "No Telepon"}, 0
        ) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new JTable(model);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedId = model.getValueAt(row, 0).toString();
                    txtNama.setText(model.getValueAt(row, 1).toString());
                    txtUsia.setText(model.getValueAt(row, 2).toString());
                    txtKeahlian.setText(model.getValueAt(row, 3).toString());
                    txtTelp.setText(model.getValueAt(row, 4).toString());
                    
                    // Mode Edit: Simpan OFF, Update/Delete ON
                    btnSimpan.setEnabled(false);
                    btnUpdate.setEnabled(true);
                    btnDelete.setEnabled(true);
                }
            }
        });

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Data Instruktur"));
        tablePanel.add(new JScrollPane(table));

        // GABUNGKAN PANEL
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    // Helper: Tambah field ke panel
    private void addField(JPanel panel, GridBagConstraints c, String label, JComponent field, int row) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(field, c);
    }

    // Helper: Buat tombol
    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(100, 30));
        btn.addActionListener(action);
        return btn;
    }

    public void loadData() {
        model.setRowCount(0);
        try {
            String sql = "SELECT * FROM instruktur_gym ORDER BY id_instruktur";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_instruktur"),
                    rs.getString("nama"),
                    rs.getInt("usia"),
                    rs.getString("keahlian"),
                    rs.getString("nomor_telepon")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal Load Data: " + e.getMessage());
        }
    }

    private boolean validasi() {
        if (txtNama.getText().isEmpty() || 
            txtUsia.getText().isEmpty() || 
            txtKeahlian.getText().isEmpty() || 
            txtTelp.getText().isEmpty()) {
            
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return false;
        }

        try {
            int usia = Integer.parseInt(txtUsia.getText());
            if (usia <= 0) {
                JOptionPane.showMessageDialog(this, "Usia harus angka positif!");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Usia harus berupa angka!");
            return false;
        }

        return true;
    }

    private void simpan() {
        if (!validasi()) return;

        try {
            String sql = "INSERT INTO instruktur_gym(nama, usia, keahlian, nomor_telepon) VALUES (?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNama.getText());
            ps.setInt(2, Integer.parseInt(txtUsia.getText()));
            ps.setString(3, txtKeahlian.getText());
            ps.setString(4, txtTelp.getText());
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan!");
            loadData();
            resetForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void update() {
        if (!validasi()) return;

        try {
            String sql = "UPDATE instruktur_gym SET nama=?, usia=?, keahlian=?, nomor_telepon=? WHERE id_instruktur=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNama.getText());
            ps.setInt(2, Integer.parseInt(txtUsia.getText()));
            ps.setString(3, txtKeahlian.getText());
            ps.setString(4, txtTelp.getText());
            ps.setInt(5, Integer.parseInt(selectedId));
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Data Berhasil Diupdate!");
            loadData();
            resetForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void delete() {
        if (selectedId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Hapus data ini?", 
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM instruktur_gym WHERE id_instruktur=?");
                ps.setInt(1, Integer.parseInt(selectedId));
                ps.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Data Dihapus!");
                loadData();
                resetForm();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal Hapus: " + e.getMessage());
            }
        }
    }

    public void resetForm() {
        txtNama.setText("");
        txtUsia.setText("");
        txtKeahlian.setText("");
        txtTelp.setText("");
        selectedId = "";
        table.clearSelection();
        
        // Kembalikan ke mode Simpan
        Component[] components = ((JPanel)((JPanel)getComponent(0)).getComponent(1)).getComponents();
        ((JButton)components[0]).setEnabled(true);  // Simpan ON
        ((JButton)components[1]).setEnabled(false); // Update OFF
        ((JButton)components[2]).setEnabled(false); // Delete OFF
    }
}