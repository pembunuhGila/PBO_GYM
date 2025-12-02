import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FormJadwalKelas extends JFrame {
    private JTextField txtNamaKelas, txtJamKelas;
    private JComboBox<String> cbHari, cbInstruktur;
    private JTable table;
    private DefaultTableModel model;

    private Connection conn;

    public FormJadwalKelas() {
        setTitle("Form Jadwal Kelas Gym");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        connectDB();
        initUI();
        loadInstruktur();
        loadTable();
    }

    private void connectDB() {
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost/PBO_GYM", "postgres", "crazyMamad13*");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi Gagal: " + e.getMessage());
        }
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        txtNamaKelas = new JTextField();
        txtJamKelas = new JTextField();

        cbHari = new JComboBox<>(new String[]{
            "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
        });

        cbInstruktur = new JComboBox<>();

        panel.add(new JLabel("Nama Kelas:"));
        panel.add(txtNamaKelas);
        panel.add(new JLabel("Hari:"));
        panel.add(cbHari);
        panel.add(new JLabel("Jam Kelas (HH:MM:SS):"));
        panel.add(txtJamKelas);
        panel.add(new JLabel("Instruktur:"));
        panel.add(cbInstruktur);

        JButton btnSimpan = new JButton("Simpan");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnReset = new JButton("Reset");

        panel.add(btnSimpan);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnReset);

        add(panel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{
            "ID", "Nama Kelas", "Hari", "Jam", "Instruktur"
        }, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnSimpan.addActionListener(e -> simpan());
        btnUpdate.addActionListener(e -> update());
        btnDelete.addActionListener(e -> delete());
        btnReset.addActionListener(e -> reset());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                txtNamaKelas.setText(model.getValueAt(row, 1).toString());
                cbHari.setSelectedItem(model.getValueAt(row, 2).toString());
                txtJamKelas.setText(model.getValueAt(row, 3).toString());
                cbInstruktur.setSelectedItem(model.getValueAt(row, 4).toString());
            }
        });
    }

    private void loadInstruktur() {
        try {
            cbInstruktur.removeAllItems();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM instruktur_gym");
            while (rs.next()) {
                cbInstruktur.addItem(rs.getInt("id_instruktur") + " - " + rs.getString("nama"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load instruktur: " + e.getMessage());
        }
    }

    private void loadTable() {
        try {
            model.setRowCount(0);
            String sql = "SELECT j.id_kelas, j.nama_kelas, j.hari, j.jam_kelas, i.nama FROM jadwal_kelas j JOIN instruktur_gym i ON j.id_instruktur=i.id_instruktur";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load table: " + e.getMessage());
        }
    }

    private void simpan() {
        try {
            String instruktur = cbInstruktur.getSelectedItem().toString();
            int idInstruktur = Integer.parseInt(instruktur.split(" - ")[0]);

            String sql = "INSERT INTO jadwal_kelas (nama_kelas, hari, jam_kelas, id_instruktur) VALUES (?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNamaKelas.getText());
            ps.setString(2, cbHari.getSelectedItem().toString());
            ps.setString(3, txtJamKelas.getText());
            ps.setInt(4, idInstruktur);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            loadTable(); reset();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal simpan: " + e.getMessage());
        }
    }

    private void update() {
        try {
            int row = table.getSelectedRow();
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());

            String instruktur = cbInstruktur.getSelectedItem().toString();
            int idInstruktur = Integer.parseInt(instruktur.split(" - ")[0]);

            String sql = "UPDATE jadwal_kelas SET nama_kelas=?, hari=?, jam_kelas=?, id_instruktur=? WHERE id_kelas=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNamaKelas.getText());
            ps.setString(2, cbHari.getSelectedItem().toString());
            ps.setString(3, txtJamKelas.getText());
            ps.setInt(4, idInstruktur);
            ps.setInt(5, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!");
            loadTable(); reset();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal update: " + e.getMessage());
        }
    }

    private void delete() {
        try {
            int row = table.getSelectedRow();
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());

            String sql = "DELETE FROM jadwal_kelas WHERE id_kelas=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
            loadTable(); reset();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal delete: " + e.getMessage());
        }
    }

    private void reset() {
        txtNamaKelas.setText("");
        txtJamKelas.setText("");
        cbHari.setSelectedIndex(0);
        if (cbInstruktur.getItemCount() > 0) cbInstruktur.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormJadwalKelas().setVisible(true));
    }
}
