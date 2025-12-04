import javax.swing.*;

public class MainGymApp extends JFrame {
    public MainGymApp() {
        setTitle("Aplikasi Gym");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Membuat JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Registrasi Member Gym
        RegistrasiMemberGym memberPanel = new RegistrasiMemberGym();
        tabbedPane.addTab("Daftar Member Gym", memberPanel.getContentPane());

        // Tab 2: Data Instruktur Gym
        InstrukturGymApp instrukturPanel = new InstrukturGymApp();
        tabbedPane.addTab("Data Instruktur Gym", instrukturPanel.getContentPane());

        // Tab 3: Jadwal Kelas
        FormJadwalKelas jadwalPanel = new FormJadwalKelas();
        tabbedPane.addTab("Daftar Kelas Gym", jadwalPanel.getContentPane());

        // Tab 4: Pendaftaran Kelas
        FormPendaftaranKelas pendaftaranPanel = new FormPendaftaranKelas();
        tabbedPane.addTab("Pendaftaran Kelas Gym", pendaftaranPanel.getContentPane());

        add(tabbedPane);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGymApp());
    }
}
