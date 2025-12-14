import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormPendaftaranKelas extends JPanel {
    private JTextField txtIdPendaftaran, txtCatatan;
    private JComboBox<String> cmbMember, cmbKelas;
    private JSpinner spinnerTanggal;
    private JTable table;
    private DefaultTableModel model;
    private Connection conn;
    
    private static final String URL = "jdbc:postgresql://localhost:5432/PBO_GYM";
    private static final String USER = "postgres";
    private static final String PASSWORD = "crazyMamad13*";

    public FormPendaftaranKelas() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        connectDB();
        initUI();
        loadComboBox();
        loadTable();
    }

    private void connectDB() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi gagal: " + e.getMessage());
        }
    }

    private void initUI() {
        // PANEL INPUT
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Form Pendaftaran Kelas"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Inisialisasi komponen
        txtIdPendaftaran = new JTextField(20);
        txtIdPendaftaran.setEditable(false);
        txtIdPendaftaran.setBackground(Color.LIGHT_GRAY);
        
        cmbMember = new JComboBox<>();
        cmbKelas = new JComboBox<>();
        
        spinnerTanggal = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerTanggal, "dd-MM-yyyy");
        spinnerTanggal.setEditor(editor);
        
        txtCatatan = new JTextField(20);

        // Tambah field ke panel
        addField(inputPanel, c, "ID Pendaftaran:", txtIdPendaftaran, 0);
        addField(inputPanel, c, "Member:", cmbMember, 1);
        addField(inputPanel, c, "Kelas Gym:", cmbKelas, 2);
        addField(inputPanel, c, "Tanggal Daftar:", spinnerTanggal, 3);
        addField(inputPanel, c, "Catatan:", txtCatatan, 4);

        // PANEL TOMBOL
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.add(createButton("Simpan", new Color(46, 204, 113), e -> simpan()));
        btnPanel.add(createButton("Delete", new Color(231, 76, 60), e -> delete()));
        btnPanel.add(createButton("Reset", new Color(52, 152, 219), e -> reset()));

        // PANEL TABEL
        model = new DefaultTableModel(
            new String[]{"ID", "Member", "Kelas", "Tanggal", "Catatan"}, 0
        ) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new JTable(model);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                loadToForm();
            }
        });

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Data Pendaftaran"));
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

    // Helper: Buat tombol dengan warna
    private JButton createButton(String text, Color bgColor, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(100, 30));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.addActionListener(action);
        return btn;
    }

    public void loadComboBox() {
        // Load Member
        try {
            cmbMember.removeAllItems();
            cmbMember.addItem("-- Pilih Member --");
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT id_member, nama FROM member_gym ORDER BY id_member ASC"
            );
            while (rs.next()) {
                cmbMember.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error load member: " + e.getMessage());
        }
        
        // Load Kelas
        try {
            cmbKelas.removeAllItems();
            cmbKelas.addItem("-- Pilih Kelas --");
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT j.id_kelas, j.nama_kelas, j.hari, j.jam_kelas " +
                "FROM jadwal_kelas j ORDER BY j.id_kelas ASC"
            );
            while (rs.next()) {
                cmbKelas.addItem(rs.getInt(1) + " - " + rs.getString(2) + 
                    " (" + rs.getString(3) + " " + rs.getTime(4) + ")");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error load kelas: " + e.getMessage());
        }
    }

    public void loadTable() {
        model.setRowCount(0);
        try {
            String sql = "SELECT p.id_pendaftaran, m.nama, j.nama_kelas, " +
                        "p.tanggal_daftar, p.catatan " +
                        "FROM pendaftaran_kelas p " +
                        "JOIN member_gym m ON p.id_member = m.id_member " +
                        "JOIN jadwal_kelas j ON p.id_kelas = j.id_kelas " +
                        "ORDER BY p.id_pendaftaran ASC";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getDate(4), rs.getString(5)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error load table: " + e.getMessage());
        }
    }

    private void simpan() {
        if (cmbMember.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih member!");
            return;
        }
        if (cmbKelas.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih kelas!");
            return;
        }
        
        try {
            int idMember = Integer.parseInt(cmbMember.getSelectedItem().toString().split(" - ")[0]);
            int idKelas = Integer.parseInt(cmbKelas.getSelectedItem().toString().split(" - ")[0]);
            java.sql.Date tanggal = new java.sql.Date(((java.util.Date)spinnerTanggal.getValue()).getTime());
            
            String sql = "INSERT INTO pendaftaran_kelas(id_member, id_kelas, tanggal_daftar, catatan) VALUES(?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idMember);
            ps.setInt(2, idKelas);
            ps.setDate(3, tanggal);
            ps.setString(4, txtCatatan.getText().isEmpty() ? null : txtCatatan.getText());
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            loadTable();
            reset();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error simpan: " + e.getMessage());
        }
    }

    private void delete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!");
            return;
        }
        
        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus data ini?", 
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM pendaftaran_kelas WHERE id_pendaftaran = ?"
                );
                ps.setInt(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                loadTable();
                reset();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error hapus: " + e.getMessage());
            }
        }
    }

    public void reset() {
        txtIdPendaftaran.setText("");
        cmbMember.setSelectedIndex(0);
        cmbKelas.setSelectedIndex(0);
        spinnerTanggal.setValue(new java.util.Date());
        txtCatatan.setText("");
        table.clearSelection();
    }

    private void loadToForm() {
        int row = table.getSelectedRow();
        if (row != -1) {
            txtIdPendaftaran.setText(model.getValueAt(row, 0).toString());
            
            String member = model.getValueAt(row, 1).toString();
            for (int i = 0; i < cmbMember.getItemCount(); i++) {
                if (cmbMember.getItemAt(i).contains(member)) {
                    cmbMember.setSelectedIndex(i);
                    break;
                }
            }
            
            String kelas = model.getValueAt(row, 2).toString();
            for (int i = 0; i < cmbKelas.getItemCount(); i++) {
                if (cmbKelas.getItemAt(i).contains(kelas)) {
                    cmbKelas.setSelectedIndex(i);
                    break;
                }
            }
            
            try {
                java.sql.Date date = (java.sql.Date) model.getValueAt(row, 3);
                spinnerTanggal.setValue(new java.util.Date(date.getTime()));
            } catch (Exception e) {}
            
            Object catatan = model.getValueAt(row, 4);
            txtCatatan.setText(catatan != null ? catatan.toString() : "");
        }
    }
}