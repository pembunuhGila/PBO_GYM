import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FormRegistrasiMemberGym extends JPanel {
    private JTextField txtNama, txtUsia, txtTelepon;
    private JTextArea txtAlamat;
    private JComboBox<String> cbJK;
    private JTable table;
    private DefaultTableModel model;
    private Connection conn;

    public FormRegistrasiMemberGym() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        connectDB();
        initUI();
        loadData();
    }

    private void connectDB() {
        try {
            conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/PBO_GYM",
                "postgres",
                "crazyMamad13*"
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi Gagal: " + e.getMessage());
        }
    }

    private void initUI() {
        // PANEL INPUT
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Form Registrasi Member"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Inisialisasi komponen
        txtNama = new JTextField(20);
        txtUsia = new JTextField(20);
        cbJK = new JComboBox<>(new String[]{"L", "P"});
        
        txtTelepon = new JTextField(20);
        txtTelepon.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume();
                    return;
                }
                if (txtTelepon.getText().length() >= 13) {
                    e.consume();
                    JOptionPane.showMessageDialog(FormRegistrasiMemberGym.this,
                        "Nomor telepon maksimal 13 karakter!");
                }
            }
        });

        txtAlamat = new JTextArea(3, 20);
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        JScrollPane spAlamat = new JScrollPane(txtAlamat);

        // Tambah field ke panel
        addField(inputPanel, c, "Nama:", txtNama, 0);
        addField(inputPanel, c, "Usia:", txtUsia, 1);
        addField(inputPanel, c, "Jenis Kelamin:", cbJK, 2);
        addField(inputPanel, c, "No Telepon:", txtTelepon, 3);
        
        c.gridx = 0; c.gridy = 4; c.weightx = 0;
        inputPanel.add(new JLabel("Alamat:"), c);
        c.gridx = 1; c.weightx = 1;
        inputPanel.add(spAlamat, c);

        // PANEL TOMBOL
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.add(createButton("Simpan", e -> simpanData()));
        btnPanel.add(createButton("Hapus", e -> hapusData()));
        btnPanel.add(createButton("Reset", e -> resetForm()));

        // PANEL TABEL
        model = new DefaultTableModel(
            new String[]{"ID", "Nama", "Usia", "JK", "Telepon", "Alamat"}, 0
        ) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new JTable(model);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Data Member"));
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
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM member_gym ORDER BY id_member");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_member"),
                    rs.getString("nama"),
                    rs.getInt("usia"),
                    rs.getString("jenis_kelamin"),
                    rs.getString("nomor_telepon"),
                    rs.getString("alamat")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Load Data: " + e.getMessage());
        }
    }

    private boolean validasiForm() {
        if (txtNama.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama harus diisi!");
            return false;
        }
        if (txtUsia.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usia harus diisi!");
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
        if (txtTelepon.getText().length() < 10 || txtTelepon.getText().length() > 13) {
            JOptionPane.showMessageDialog(this, 
                "Nomor telepon harus 10-13 digit!");
            return false;
        }
        if (txtAlamat.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alamat harus diisi!");
            return false;
        }
        return true;
    }

    private void simpanData() {
        if (!validasiForm()) return;

        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO member_gym(nama, usia, jenis_kelamin, nomor_telepon, alamat) VALUES (?,?,?,?,?)"
            );
            ps.setString(1, txtNama.getText());
            ps.setInt(2, Integer.parseInt(txtUsia.getText()));
            ps.setString(3, cbJK.getSelectedItem().toString());
            ps.setString(4, txtTelepon.getText());
            ps.setString(5, txtAlamat.getText());

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            loadData();
            resetForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal Simpan Data: " + e.getMessage());
        }
    }

    private void hapusData() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus data ini?", 
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM member_gym WHERE id_member = ?");
                ps.setInt(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                loadData();
                resetForm();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal Hapus Data: " + e.getMessage());
            }
        }
    }

    public void resetForm() {
        txtNama.setText("");
        txtUsia.setText("");
        txtTelepon.setText("");
        txtAlamat.setText("");
        cbJK.setSelectedIndex(0);
        table.clearSelection();
    }
}