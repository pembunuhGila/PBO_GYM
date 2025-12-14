import javax.swing.*;

public class MainGymApp extends JFrame {
    private FormJadwalKelas jadwalPanel;
    private FormPendaftaranKelas pendaftaranPanel;

    public MainGymApp() {
        setTitle("Aplikasi Gym");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Tambah tab
        tabbedPane.addTab("Registrasi Member", new FormRegistrasiMemberGym());
        tabbedPane.addTab("Data Instruktur", new InstrukturGymApp());
        
        jadwalPanel = new FormJadwalKelas();
        tabbedPane.addTab("Jadwal Kelas", jadwalPanel);
        
        pendaftaranPanel = new FormPendaftaranKelas();
        tabbedPane.addTab("Pendaftaran Kelas", pendaftaranPanel);

        // Event ketika pindah tab
        tabbedPane.addChangeListener(e -> {
            int tab = tabbedPane.getSelectedIndex();
            
            if (tab == 2) { // Tab Jadwal Kelas
                jadwalPanel.loadInstruktur();
                jadwalPanel.loadTable();
                jadwalPanel.reset();
            } 
            else if (tab == 3) { // Tab Pendaftaran Kelas
                pendaftaranPanel.loadComboBox();
                pendaftaranPanel.loadTable();
                pendaftaranPanel.reset();
            }
        });

        add(tabbedPane);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGymApp());
    }
}