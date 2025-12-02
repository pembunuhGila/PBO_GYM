package javaapplication2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class JavaApplication2 extends JFrame {

    // --- KONFIGURASI DATABASE ---
    // User default XAMPP biasanya "root" dan password kosong ""
    static final String DB_URL = "jdbc:mysql://localhost:3306/gymfield_db";
    static final String DB_USER = "root";
    static final String DB_PASS = ""; 

    // --- KOMPONEN GUI ---
    JTextField tNama = new JTextField();
    JTextField tUsia = new JTextField();
    JTextField tKeahlian = new JTextField();
    JTextField tTelp = new JTextField();

    JButton bSimpan = new JButton("Simpan");
    JButton bUbah = new JButton("Ubah");
    JButton bHapus = new JButton("Hapus");
    JButton bReset = new JButton("Reset");

    DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Nama", "Usia", "Keahlian", "Telp"}, 0);
    JTable tabel = new JTable(model);

    // Variabel bantu untuk menyimpan ID yang sedang diedit
    String selectedId = "";

    public JavaApplication2() {
        super("GymField App - Kelompok Kami"); // Judul Aplikasi
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. PANEL FORM (Input Data) ---
        JPanel pForm = new JPanel(new GridLayout(6, 2, 10, 10)); // 6 baris, 2 kolom
        pForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        pForm.add(new JLabel("Nama Instruktur:")); pForm.add(tNama);
        pForm.add(new JLabel("Usia:")); pForm.add(tUsia);
        pForm.add(new JLabel("Keahlian:")); pForm.add(tKeahlian);
        pForm.add(new JLabel("No Telepon:")); pForm.add(tTelp);
        
        // Panel Tombol
        JPanel pTombol = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pTombol.add(bSimpan);
        pTombol.add(bUbah);
        pTombol.add(bHapus);
        pTombol.add(bReset);
        
        pForm.add(new JLabel("")); // Kosong (Spacer)
        pForm.add(pTombol);

        add(pForm, BorderLayout.NORTH);

        // --- 2. TABEL DATA ---
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        // --- 3. EVENT LISTENER (Logika) ---
        
        // Saat Tabel Diklik -> Pindahkan data ke Form input
        tabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tabel.getSelectedRow();
                if (row != -1) {
                    selectedId = model.getValueAt(row, 0).toString();
                    tNama.setText(model.getValueAt(row, 1).toString());
                    tUsia.setText(model.getValueAt(row, 2).toString());
                    tKeahlian.setText(model.getValueAt(row, 3).toString());
                    tTelp.setText(model.getValueAt(row, 4).toString());
                    
                    tombolModeEdit(true); // Aktifkan mode edit
                }
            }
        });

        // Tombol Simpan
        bSimpan.addActionListener(e -> {
            if (validasi()) jalankanSQL("INSERT INTO instruktur (nama, usia, keahlian, no_telp) VALUES (?,?,?,?)");
        });

        // Tombol Ubah
        bUbah.addActionListener(e -> {
            if (validasi()) jalankanSQL("UPDATE instruktur SET nama=?, usia=?, keahlian=?, no_telp=? WHERE id=?");
        });

        // Tombol Hapus
        bHapus.addActionListener(e -> {
            if (!selectedId.isEmpty() && JOptionPane.showConfirmDialog(this, "Hapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                hapusData();
            }
        });

        // Tombol Reset
        bReset.addActionListener(e -> resetForm());

        // Inisialisasi awal saat aplikasi dibuka
        resetForm();
        loadData();
    }

    // --- KONEKSI DATABASE ---
    private Connection connect() throws SQLException {
        try {
            // MEMAKSA load driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver"); 
        } catch (ClassNotFoundException ex) {
            System.out.println("Driver tidak ditemukan! Cek Library.");
        }
        
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // --- FUNGSI LOAD DATA (READ) ---
    private void loadData() {
        model.setRowCount(0); // Bersihkan tabel GUI
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM instruktur")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("nama"),
                    rs.getString("usia"),
                    rs.getString("keahlian"),
                    rs.getString("no_telp")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal Load Data: " + e.getMessage());
        }
    }

    // --- FUNGSI EKSEKUSI SQL (CREATE & UPDATE) ---
    private void jalankanSQL(String sql) {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tNama.getText());
            ps.setInt(2, Integer.parseInt(tUsia.getText()));
            ps.setString(3, tKeahlian.getText());
            ps.setString(4, tTelp.getText());

            // Jika UPDATE, parameter ke-5 adalah ID
            if (sql.contains("UPDATE")) {
                ps.setString(5, selectedId);
            }

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan/Diubah!");
            loadData();
            resetForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Database: " + e.getMessage());
        }
    }

    // --- FUNGSI HAPUS (DELETE) ---
    private void hapusData() {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement("DELETE FROM instruktur WHERE id=?")) {
            ps.setString(1, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data Dihapus!");
            loadData();
            resetForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal Hapus: " + e.getMessage());
        }
    }

    // --- VALIDASI & RESET ---
    private boolean validasi() {
        if (tNama.getText().isEmpty() || tUsia.getText().isEmpty() || tKeahlian.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama, Usia, dan Keahlian harus diisi!");
            return false;
        }
        return true;
    }

    private void resetForm() {
        tNama.setText(""); tUsia.setText(""); tKeahlian.setText(""); tTelp.setText("");
        selectedId = "";
        tabel.clearSelection();
        tombolModeEdit(false);
    }

    private void tombolModeEdit(boolean isEdit) {
        bSimpan.setEnabled(!isEdit);  // Simpan mati saat edit
        bUbah.setEnabled(isEdit);     // Ubah hidup saat edit
        bHapus.setEnabled(isEdit);    // Hapus hidup saat edit
    }

    // --- MAIN METHOD ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JavaApplication2().setVisible(true));
    }
}