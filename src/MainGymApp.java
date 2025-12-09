import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainGymApp extends JFrame {

    public MainGymApp() {
        setTitle("Aplikasi Gym");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Membuat JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Registrasi Member Gym
        From1_RegistrasiMemberGym memberPanel = new From1_RegistrasiMemberGym();
        tabbedPane.addTab("Daftar Member Gym", memberPanel.getContentPane());

        // Tab 2: Data Instruktur Gym
        InstrukturGymApp instrukturPanel = new InstrukturGymApp();
        tabbedPane.addTab("Data Instruktur Gym", instrukturPanel.getContentPane());

        // Tab 3: Jadwal Kelas Gym
        final FormJadwalKelas jadwalPanel = new FormJadwalKelas();
        tabbedPane.addTab("Daftar Kelas Gym", jadwalPanel.getContentPane());

        // Tab 4: Pendaftaran Kelas Gym
        final FormPendaftaranKelas pendaftaranPanel = new FormPendaftaranKelas(); 
        tabbedPane.addTab("Pendaftaran Kelas Gym", pendaftaranPanel.getContentPane());


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
