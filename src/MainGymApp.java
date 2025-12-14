import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainGymApp extends JFrame {

    public MainGymApp() {
        setTitle("Aplikasi Gym");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        // TAB 1: Registrasi Member
        From1_RegistrasiMemberGym memberPanel = new From1_RegistrasiMemberGym();
        tabbedPane.addTab("Daftar Member Gym", memberPanel);

        // TAB 2: Instruktur
        InstrukturGymApp instrukturPanel = new InstrukturGymApp();
        tabbedPane.addTab("Data Instruktur Gym", instrukturPanel);

        // TAB 3: Jadwal Kelas
        FormJadwalKelas jadwalPanel = new FormJadwalKelas();
        tabbedPane.addTab("Daftar Kelas Gym", jadwalPanel);

        // TAB 4: Pendaftaran Kelas
        FormPendaftaranKelas pendaftaranPanel = new FormPendaftaranKelas();
        tabbedPane.addTab("Pendaftaran Kelas Gym", pendaftaranPanel);

        // EVENT SAAT PINDAH TAB
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int index = tabbedPane.getSelectedIndex();
                String title = tabbedPane.getTitleAt(index);

                switch (title) {
                    case "Daftar Kelas Gym":
                        jadwalPanel.loadInstruktur();
                        jadwalPanel.loadTable();
                        jadwalPanel.reset();
                        break;

                    case "Pendaftaran Kelas Gym":
                        pendaftaranPanel.loadComboBox();
                        pendaftaranPanel.loadTable();
                        pendaftaranPanel.reset();
                        break;
                }
            }
        });

        add(tabbedPane);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGymApp());
    }
}
