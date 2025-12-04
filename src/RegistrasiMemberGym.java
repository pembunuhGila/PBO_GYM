// package src;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;

public class RegistrasiMemberGym extends JFrame {

    Connection conn;
    PreparedStatement pst;
    ResultSet rs;

    JTextField txtNama, txtUsia, txtTelepon;
    JTextArea txtAlamat;
    JComboBox<String> cbJK;
    JTable table;
    DefaultTableModel model;

    public RegistrasiMemberGym() {
        setTitle("Form Registrasi Member Gym");
        setSize(750, 500);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Koneksi Database PostgreSQL
        koneksiDatabase();

        // Labels & Inputs
        JLabel lblNama = new JLabel("Nama:");
        lblNama.setBounds(20, 20, 120, 25);
        add(lblNama);
        txtNama = new JTextField();
        txtNama.setBounds(150, 20, 350, 25);
        add(txtNama);

        JLabel lblUsia = new JLabel("Usia:");
        lblUsia.setBounds(20, 60, 120, 25);
        add(lblUsia);
        txtUsia = new JTextField();
        txtUsia.setBounds(150, 60, 350, 25);
        add(txtUsia);

        JLabel lblJK = new JLabel("Jenis Kelamin:");
        lblJK.setBounds(20, 100, 120, 25);
        add(lblJK);
        cbJK = new JComboBox<>(new String[]{"L", "P"});
        cbJK.setBounds(150, 100, 350, 25);
        add(cbJK);

        JLabel lblTelepon = new JLabel("No Telepon:");
        lblTelepon.setBounds(20, 140, 120, 25);
        add(lblTelepon);
        txtTelepon = new JTextField();
        txtTelepon.setBounds(150, 140, 350, 25);
        add(txtTelepon);

        // Batasi input nomor telepon
        txtTelepon.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume(); 
                }
                if (txtTelepon.getText().length() >= 13) {
                    e.consume(); 
                }
            }
        });

        JLabel lblAlamat = new JLabel("Alamat:");
        lblAlamat.setBounds(20, 180, 120, 25);
        add(lblAlamat);
        txtAlamat = new JTextArea();
        JScrollPane spAlamat = new JScrollPane(txtAlamat);
        spAlamat.setBounds(150, 180, 350, 70);
        add(spAlamat);

        // Tombol
        JButton btnSimpan = new JButton("Simpan");
        btnSimpan.setBounds(150, 280, 100, 35);
        add(btnSimpan);

        JButton btnHapus = new JButton("Hapus");
        btnHapus.setBounds(270, 280, 100, 35);
        add(btnHapus);

        JButton btnReset = new JButton("Reset");
        btnReset.setBounds(390, 280, 100, 35);
        add(btnReset);

        // Tabel
        model = new DefaultTableModel(new String[]{
                "ID", "Nama", "Usia", "JK", "Telepon", "Alamat"
        }, 0);
        table = new JTable(model);
        JScrollPane spTable = new JScrollPane(table);
        spTable.setBounds(20, 320, 700, 150);
        add(spTable);

        // Load data awal
        loadData();

        // Event tombol
        btnSimpan.addActionListener(e -> simpanData());
        btnHapus.addActionListener(e -> hapusData());
        btnReset.addActionListener(e -> resetForm());

        setVisible(true);
    }

    // Koneksi database PostgreSQL
    void koneksiDatabase() {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/pbo_gym",
                    "postgres",
                    "waely1234"
            );
            System.out.println("Koneksi Database Berhasil!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Koneksi Gagal: " + e.getMessage());
        }
    }

    // Load data ke JTable
    void loadData() {
        model.setRowCount(0);
        try {
            pst = conn.prepareStatement("SELECT * FROM member_gym ORDER BY id_member");
            rs = pst.executeQuery();
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
            JOptionPane.showMessageDialog(null, "Gagal Load Data: " + e.getMessage());
        }
    }

    // Simpan data
    void simpanData() {
        try {
            // Validasi form
            if (txtNama.getText().isEmpty() || txtUsia.getText().isEmpty() ||
                    txtTelepon.getText().isEmpty() || txtAlamat.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Semua field wajib diisi!");
                return;
            }

            // Validasi nomor telepon
            if (txtTelepon.getText().length() < 10 || txtTelepon.getText().length() > 13) {
                JOptionPane.showMessageDialog(null, "Nomor telepon harus 10-13 digit!");
                return;
            }

            pst = conn.prepareStatement(
                    "INSERT INTO member_gym(nama, usia, jenis_kelamin, nomor_telepon, alamat) VALUES (?,?,?,?,?)"
            );
            pst.setString(1, txtNama.getText());
            pst.setInt(2, Integer.parseInt(txtUsia.getText()));
            pst.setString(3, cbJK.getSelectedItem().toString());
            pst.setString(4, txtTelepon.getText());
            pst.setString(5, txtAlamat.getText());

            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Data berhasil disimpan!");
            loadData();
            resetForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Simpan Data: " + e.getMessage());
        }
    }

    // Hapus data
    void hapusData() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(null, "Pilih data di tabel!");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        try {
            pst = conn.prepareStatement("DELETE FROM member_gym WHERE id_member = ?");
            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Data berhasil dihapus!");
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Hapus Data: " + e.getMessage());
        }
    }

    // Reset form
    void resetForm() {
        txtNama.setText("");
        txtUsia.setText("");
        txtTelepon.setText("");
        txtAlamat.setText("");
        cbJK.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        new RegistrasiMemberGym();
    }
}
