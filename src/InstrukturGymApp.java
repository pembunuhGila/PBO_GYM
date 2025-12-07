import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class InstrukturGymApp extends JFrame {

    // --- KONFIGURASI DATABASE POSTGRESQL ---
    static final String DB_URL = "jdbc:postgresql://localhost:5433/db_gym";
    static final String DB_USER = "postgres";
    static final String DB_PASS = "audyna11";

    // --- KOMPONEN GUI ---
    JTextField tNama = new JTextField();
    JTextField tUsia = new JTextField();
    JTextField tKeahlian = new JTextField();
    JTextField tTelp = new JTextField();

    JButton bSimpan = new JButton("Simpan");
    JButton bUbah = new JButton("Ubah");
    JButton bHapus = new JButton("Hapus");
    JButton bReset = new JButton("Reset");

    DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Nama", "Usia", "Keahlian", "No Telepon"}, 0
    );
    JTable tabel = new JTable(model);

    // Variabel bantu untuk menyimpan ID yang sedang diedit
    String selectedId = "";

    public InstrukturGymApp() {
        super("Aplikasi Instruktur Gym");
        setSize(750, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- PANEL FORM (Input Data) ---
        JPanel pForm = new JPanel(new GridLayout(6, 2, 10, 10));
        pForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        pForm.add(new JLabel("Nama Instruktur:")); pForm.add(tNama);
        pForm.add(new JLabel("Usia:")); pForm.add(tUsia);
        pForm.add(new JLabel("Keahlian:")); pForm.add(tKeahlian);
        pForm.add(new JLabel("No Telepon:")); pForm.add(tTelp);

        // Panel Tombol
        JPanel pTombol = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pTombol.add(bSimpan); pTombol.add(bUbah); pTombol.add(bHapus); pTombol.add(bReset);

        pForm.add(new JLabel("")); // Spacer
        pForm.add(pTombol);

        add(pForm, BorderLayout.NORTH);

        // --- TABEL DATA ---
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        // --- EVENT LISTENER ---
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
                    tombolModeEdit(true);
                }
            }
        });

        // Tombol Simpan
        bSimpan.addActionListener(e -> {
            if (validasi()) {
                jalankanSQL("INSERT INTO instruktur_gym(nama, usia, keahlian, nomor_telepon) VALUES (?,?,?,?)");
            }
        });

        // Tombol Ubah
        bUbah.addActionListener(e -> {
            if (validasi()) {
                jalankanSQL("UPDATE instruktur_gym SET nama=?, usia=?, keahlian=?, nomor_telepon=? WHERE id_instruktur=?");
            }
        });

        // Tombol Hapus
        bHapus.addActionListener(e -> {
            if (!selectedId.isEmpty() && JOptionPane.showConfirmDialog(this, "Hapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                hapusData();
            }
        });

        // Tombol Reset
        bReset.addActionListener(e -> resetForm());

        // Inisialisasi awal
        resetForm();
        loadData();

        setVisible(true);
    }

    // --- KONEKSI DATABASE ---
    private Connection connect() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Driver PostgreSQL tidak ditemukan! Cek Library.");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // --- LOAD DATA ---
    public void loadData() {
        model.setRowCount(0);
        String sql = "SELECT * FROM instruktur_gym ORDER BY id_instruktur";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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

    // --- EKSEKUSI SQL (INSERT & UPDATE) ---
    private void jalankanSQL(String sql) {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tNama.getText());
            ps.setInt(2, Integer.parseInt(tUsia.getText()));
            ps.setString(3, tKeahlian.getText());
            ps.setString(4, tTelp.getText());

            if (sql.toUpperCase().contains("UPDATE")) {
                ps.setInt(5, Integer.parseInt(selectedId));
            }

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan/Diubah!");
            loadData();
            resetForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Database: " + e.getMessage());
        }
    }

    // --- HAPUS DATA ---
    private void hapusData() {
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement("DELETE FROM instruktur_gym WHERE id_instruktur=?")) {
            ps.setInt(1, Integer.parseInt(selectedId));
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
        if (tNama.getText().isEmpty() || tUsia.getText().isEmpty() || tKeahlian.getText().isEmpty() || tTelp.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return false;
        }

        // Batasi nomor telepon maksimal 13 digit
        if (!tTelp.getText().matches("\\d{1,13}")) {
            JOptionPane.showMessageDialog(this, "Nomor telepon harus angka dan maksimal 13 digit!");
            return false;
        }

        return true;
    }

    public void resetForm() {
        tNama.setText(""); tUsia.setText(""); tKeahlian.setText(""); tTelp.setText("");
        selectedId = "";
        tabel.clearSelection();
        tombolModeEdit(false);
    }

    private void tombolModeEdit(boolean isEdit) {
        bSimpan.setEnabled(!isEdit);
        bUbah.setEnabled(isEdit);
        bHapus.setEnabled(isEdit);
    }

    // --- MAIN METHOD ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InstrukturGymApp());
    }
}