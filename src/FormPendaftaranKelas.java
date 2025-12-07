import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FormPendaftaranKelas extends JFrame {
    private JTextField txtIdPendaftaran, txtCatatan;
    private JComboBox<String> cmbMember, cmbKelas;
    private JSpinner spinnerTanggal;
    private JTable table;
    private DefaultTableModel model;
    private Connection conn;
    
    private static final String URL = "jdbc:postgresql://localhost:5433/db_gym";
    private static final String USER = "postgres";
    private static final String PASSWORD = "audyna11";

    public FormPendaftaranKelas() {
        setTitle("Form Pendaftaran Kelas Gym");
        setSize(900, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        connectDB();
        initUI();
        loadComboBox();
        loadTable();
    }

    private void connectDB() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Koneksi berhasil!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi gagal: " + e.getMessage());
        }
    }

    private void initUI() {
        JPanel pForm = new JPanel(new GridLayout(6, 2, 10, 10));
        pForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // ID Pendaftaran
        pForm.add(new JLabel("ID Pendaftaran:"));
        txtIdPendaftaran = new JTextField();
        txtIdPendaftaran.setEditable(false);
        txtIdPendaftaran.setBackground(Color.LIGHT_GRAY);
        pForm.add(txtIdPendaftaran);
        
        // Member
        pForm.add(new JLabel("Member:"));
        cmbMember = new JComboBox<>();
        pForm.add(cmbMember);
        
        // Kelas
        pForm.add(new JLabel("Kelas Gym:"));
        cmbKelas = new JComboBox<>();
        pForm.add(cmbKelas);
        
        // Tanggal
        pForm.add(new JLabel("Tanggal Daftar:"));
        spinnerTanggal = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerTanggal, "dd-MM-yyyy");
        spinnerTanggal.setEditor(editor);
        pForm.add(spinnerTanggal);
        
        // Catatan
        pForm.add(new JLabel("Catatan:"));
        txtCatatan = new JTextField();
        pForm.add(txtCatatan);
        
        // Tombol
        JPanel pBtn = new JPanel(new FlowLayout());
        JButton btnSimpan = new JButton("Simpan");
        JButton btnDelete = new JButton("Delete");
        JButton btnReset = new JButton("Reset");
        
        btnSimpan.setBackground(new Color(46, 204, 113));
        btnSimpan.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);
        btnReset.setBackground(new Color(52, 152, 219));
        btnReset.setForeground(Color.WHITE);
        
        pBtn.add(btnSimpan);
        pBtn.add(btnDelete);
        pBtn.add(btnReset);
        pForm.add(new JLabel());
        pForm.add(pBtn);
        
        add(pForm, BorderLayout.NORTH);
        
        // Tabel
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
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        btnSimpan.addActionListener(e -> simpan());
        btnDelete.addActionListener(e -> delete());
        btnReset.addActionListener(e -> reset());
    }

    private void loadComboBox() {
        // Load Member
        try {
            cmbMember.removeAllItems();
            cmbMember.addItem("-- Pilih Member --");
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT id_member, nama FROM member_gym ORDER BY nama"
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
                "SELECT id_kelas, nama_kelas, hari, jam_kelas FROM jadwal_kelas ORDER BY nama_kelas"
            );
            while (rs.next()) {
                cmbKelas.addItem(rs.getInt(1) + " - " + rs.getString(2) + 
                    " (" + rs.getString(3) + " " + rs.getTime(4) + ")");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error load kelas: " + e.getMessage());
        }
    }

    private void loadTable() {
        model.setRowCount(0);
        try {
            String sql = 
                "SELECT p.id_pendaftaran, m.nama, j.nama_kelas, p.tanggal_daftar, p.catatan " +
                "FROM pendaftaran_kelas p " +
                "JOIN member_gym m ON p.id_member = m.id_member " +
                "JOIN jadwal_kelas j ON p.id_kelas = j.id_kelas " +
                "ORDER BY p.id_pendaftaran DESC";
            
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getDate(4),
                    rs.getString(5)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error load table: " + e.getMessage());
        }
    }

    private void simpan() {

        // VALIDASI MEMBER
        if (cmbMember.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih member!");
            return;
        }

        // VALIDASI KELAS
        if (cmbKelas.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih kelas!");
            return;
        }

        // VALIDASI TANGGAL
        java.util.Date today = new java.util.Date();
        java.util.Date selected = (java.util.Date) spinnerTanggal.getValue();

        if (selected.before(today)) {
            JOptionPane.showMessageDialog(this, "Tanggal tidak boleh sebelum hari ini!");
            return;
        }

        // VALIDASI CATATAN (maksimal 100 karakter)
        if (txtCatatan.getText().trim().length() > 100) {
            JOptionPane.showMessageDialog(this, "Catatan maksimal 100 karakter!");
            return;
        }
        
        try {
            int idMember = Integer.parseInt(cmbMember.getSelectedItem().toString().split(" - ")[0]);
            int idKelas = Integer.parseInt(cmbKelas.getSelectedItem().toString().split(" - ")[0]);
            java.sql.Date tanggal = new java.sql.Date(selected.getTime());
            
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

    private void reset() {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormPendaftaranKelas().setVisible(true));
    }
}
