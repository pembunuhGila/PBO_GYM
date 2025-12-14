import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalTime;

public class FormJadwalKelas extends JPanel {
    private JTextField txtNamaKelas, txtJamKelas;
    private JComboBox<String> cbHari, cbInstruktur;
    private JTable table;
    private DefaultTableModel model;
    private Connection conn;

    public FormJadwalKelas() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        connectDB();
        initUI();
        loadInstruktur();
        loadTable();
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
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Data"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        txtNamaKelas = new JTextField(20);
        txtJamKelas = new JTextField(20);
        cbHari = new JComboBox<>(new String[]{"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"});
        cbInstruktur = new JComboBox<>();

        addField(inputPanel, c, "Nama Kelas:", txtNamaKelas, 0);
        addField(inputPanel, c, "Hari:", cbHari, 1);
        addField(inputPanel, c, "Jam (HH:MM:SS):", txtJamKelas, 2);
        addField(inputPanel, c, "Instruktur:", cbInstruktur, 3);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.add(createButton("Simpan", e -> simpan()));
        btnPanel.add(createButton("Update", e -> update()));
        btnPanel.add(createButton("Delete", e -> delete()));
        btnPanel.add(createButton("Reset", e -> reset()));

        model = new DefaultTableModel(new String[]{"ID", "Nama Kelas", "Hari", "Jam", "Instruktur"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                fillFormFromTable();
            }
        });

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Data Jadwal"));
        tablePanel.add(new JScrollPane(table));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void addField(JPanel panel, GridBagConstraints c, String label, JComponent field, int row) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(field, c);
    }

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(100, 30));
        btn.addActionListener(action);
        return btn;
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row != -1) {
            txtNamaKelas.setText(model.getValueAt(row, 1).toString());
            cbHari.setSelectedItem(model.getValueAt(row, 2).toString());
            txtJamKelas.setText(model.getValueAt(row, 3).toString());
            cbInstruktur.setSelectedItem(model.getValueAt(row, 4).toString());
        }
    }

    public void loadInstruktur() {
        try {
            cbInstruktur.removeAllItems();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM instruktur_gym");
            while (rs.next()) {
                cbInstruktur.addItem(rs.getInt("id_instruktur") + " - " + rs.getString("nama"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error load instruktur: " + e.getMessage());
        }
    }

    public void loadTable() {
        try {
            model.setRowCount(0);
            String sql = "SELECT j.id_kelas, j.nama_kelas, j.hari, j.jam_kelas, i.nama " +
                        "FROM jadwal_kelas j JOIN instruktur_gym i ON j.id_instruktur=i.id_instruktur";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error load table: " + e.getMessage());
        }
    }

    private void simpan() {
        try {
            if (!txtJamKelas.getText().matches("\\d{2}:\\d{2}:\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Format jam: HH:MM:SS (contoh: 09:30:00)");
                return;
            }

            int idInstruktur = Integer.parseInt(cbInstruktur.getSelectedItem().toString().split(" - ")[0]);
            
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO jadwal_kelas (nama_kelas, hari, jam_kelas, id_instruktur) VALUES (?,?,?,?)"
            );
            ps.setString(1, txtNamaKelas.getText());
            ps.setString(2, cbHari.getSelectedItem().toString());
            ps.setTime(3, Time.valueOf(LocalTime.parse(txtJamKelas.getText())));
            ps.setInt(4, idInstruktur);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            loadTable();
            reset();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error simpan: " + e.getMessage());
        }
    }

    private void update() {
        try {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih data yang akan diupdate!");
                return;
            }
            if (!txtJamKelas.getText().matches("\\d{2}:\\d{2}:\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Format jam: HH:MM:SS");
                return;
            }

            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            int idInstruktur = Integer.parseInt(cbInstruktur.getSelectedItem().toString().split(" - ")[0]);

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE jadwal_kelas SET nama_kelas=?, hari=?, jam_kelas=?, id_instruktur=? WHERE id_kelas=?"
            );
            ps.setString(1, txtNamaKelas.getText());
            ps.setString(2, cbHari.getSelectedItem().toString());
            ps.setTime(3, Time.valueOf(LocalTime.parse(txtJamKelas.getText())));
            ps.setInt(4, idInstruktur);
            ps.setInt(5, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
            loadTable();
            reset();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error update: " + e.getMessage());
        }
    }

    private void delete() {
        try {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!");
                return;
            }

            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin hapus data ini?", 
                "Konfirmasi", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM jadwal_kelas WHERE id_kelas=?");
                ps.setInt(1, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                loadTable();
                reset();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error delete: " + e.getMessage());
        }
    }

    public void reset() {
        txtNamaKelas.setText("");
        txtJamKelas.setText("");
        cbHari.setSelectedIndex(0);
        if (cbInstruktur.getItemCount() > 0) cbInstruktur.setSelectedIndex(0);
        table.clearSelection();
    }
}