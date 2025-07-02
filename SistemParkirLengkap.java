import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Kelas Utama untuk GUI Sistem Parkir Mall dalam satu file.
 * VERSI DENGAN TAMBAHAN RIWAYAT PARKIR (LOG TRANSAKSI).
 */
public class SistemParkirLengkap extends JFrame {

    // Nested class untuk model Kendaraan (tidak berubah)
    private static class Kendaraan {
        private String nomorPolisi;
        private String jenisKendaraan;
        private LocalDateTime waktuMasuk;

        public Kendaraan(String nomorPolisi, String jenisKendaraan, LocalDateTime waktuMasuk) {
            this.nomorPolisi = nomorPolisi;
            this.jenisKendaraan = jenisKendaraan;
            this.waktuMasuk = waktuMasuk;
        }
        public String getNomorPolisi() { return nomorPolisi; }
        public String getJenisKendaraan() { return jenisKendaraan; }
        public LocalDateTime getWaktuMasuk() { return waktuMasuk; }
    }

    // Konstanta tarif (tidak berubah)
    private static final int TARIF_AWAL_MOTOR = 2000;
    private static final int TARIF_PER_JAM_MOTOR = 1000;
    private static final int TARIF_AWAL_MOBIL = 5000;
    private static final int TARIF_PER_JAM_MOBIL = 3000;

    // Komponen GUI
    private JTextField tfNomorPolisiMasuk, tfNomorPolisiKeluar;
    private JRadioButton rbMotor, rbMobil;
    private ButtonGroup groupJenisKendaraan;
    private JButton btnMasuk, btnKeluar;

    // Model dan Tabel untuk kendaraan AKTIF
    private JTable tabelParkirAktif;
    private DefaultTableModel modelTabelAktif;

    // (BARU) Model dan Tabel untuk RIWAYAT parkir
    private JTable tabelRiwayat;
    private DefaultTableModel modelRiwayat;

    private Map<String, Kendaraan> daftarKendaraan;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SistemParkirLengkap() {
        super("Sistem Parkir Mall Central (Plus Riwayat)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 700); // Ukuran window sedikit diperbesar
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        daftarKendaraan = new HashMap<>();
        initUI();
    }

    private void initUI() {
        JPanel panelUtama = new JPanel(new BorderLayout(10, 10));
        panelUtama.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(panelUtama);

        // Panel Atas (Input Kendaraan Masuk) - tidak berubah
        JPanel panelMasuk = new JPanel(new GridBagLayout());
        panelMasuk.setBorder(new TitledBorder("Input Kendaraan Masuk"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; panelMasuk.add(new JLabel("Nomor Polisi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; tfNomorPolisiMasuk = new JTextField(15); panelMasuk.add(tfNomorPolisiMasuk, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panelMasuk.add(new JLabel("Jenis Kendaraan:"), gbc);
        JPanel panelRadio = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rbMotor = new JRadioButton("Motor"); rbMobil = new JRadioButton("Mobil");
        rbMotor.setSelected(true);
        groupJenisKendaraan = new ButtonGroup(); groupJenisKendaraan.add(rbMotor); groupJenisKendaraan.add(rbMobil);
        panelRadio.add(rbMotor); panelRadio.add(rbMobil);
        gbc.gridx = 1; gbc.gridy = 1; panelMasuk.add(panelRadio, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END; gbc.fill = GridBagConstraints.NONE;
        btnMasuk = new JButton("Catat Masuk"); panelMasuk.add(btnMasuk, gbc);
        panelUtama.add(panelMasuk, BorderLayout.NORTH);

        // === (PERUBAHAN) Panel Tengah dengan Dua Tabel ===

        // 1. Tabel Parkir Aktif
        String[] kolomAktif = {"Nomor Polisi", "Jenis Kendaraan", "Waktu Masuk"};
        modelTabelAktif = new DefaultTableModel(kolomAktif, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelParkirAktif = new JTable(modelTabelAktif);
        JScrollPane scrollPaneAktif = new JScrollPane(tabelParkirAktif);
        scrollPaneAktif.setBorder(new TitledBorder("Daftar Kendaraan Sedang Parkir"));

        // 2. (BARU) Tabel Riwayat Parkir
        String[] kolomRiwayat = {"No. Polisi", "Jenis", "Waktu Masuk", "Waktu Keluar", "Durasi (Jam)", "Biaya (Rp)"};
        modelRiwayat = new DefaultTableModel(kolomRiwayat, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelRiwayat = new JTable(modelRiwayat);
        JScrollPane scrollPaneRiwayat = new JScrollPane(tabelRiwayat);
        scrollPaneRiwayat.setBorder(new TitledBorder("Riwayat Parkir Kendaraan Keluar"));

        // 3. (BARU) Menggabungkan dua tabel dengan JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneAktif, scrollPaneRiwayat);
        splitPane.setResizeWeight(0.5); // Membagi area 50:50 pada awalnya
        panelUtama.add(splitPane, BorderLayout.CENTER);

        // Panel Bawah (Proses Keluar) - tidak berubah
        JPanel panelKeluar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelKeluar.setBorder(new TitledBorder("Proses Kendaraan Keluar"));
        panelKeluar.add(new JLabel("Nomor Polisi:"));
        tfNomorPolisiKeluar = new JTextField(15);
        panelKeluar.add(tfNomorPolisiKeluar);
        btnKeluar = new JButton("Hitung Biaya & Keluar");
        btnKeluar.setBackground(new Color(217, 83, 79));
        btnKeluar.setForeground(Color.WHITE);
        panelKeluar.add(btnKeluar);
        panelUtama.add(panelKeluar, BorderLayout.SOUTH);

        tambahListenerAksi();
    }

    private void tambahListenerAksi() {
        btnMasuk.addActionListener(e -> prosesMasuk());
        btnKeluar.addActionListener(e -> prosesKeluar());
    }

    private void prosesMasuk() {
        String noPol = tfNomorPolisiMasuk.getText().toUpperCase().trim();
        if (noPol.isEmpty() || !rbMotor.isSelected() && !rbMobil.isSelected()) {
            JOptionPane.showMessageDialog(this, "Nomor Polisi dan Jenis Kendaraan harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (daftarKendaraan.containsKey(noPol)) {
            JOptionPane.showMessageDialog(this, "Kendaraan dengan nomor polisi " + noPol + " sudah terdaftar parkir.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String jenis = rbMotor.isSelected() ? "Motor" : "Mobil";
        LocalDateTime waktuMasuk = LocalDateTime.now();
        Kendaraan kendaraanBaru = new Kendaraan(noPol, jenis, waktuMasuk);
        daftarKendaraan.put(noPol, kendaraanBaru);

        modelTabelAktif.addRow(new Object[]{
                kendaraanBaru.getNomorPolisi(),
                kendaraanBaru.getJenisKendaraan(),
                kendaraanBaru.getWaktuMasuk().format(formatter)
        });

        JOptionPane.showMessageDialog(this, "Kendaraan " + jenis + " dengan plat " + noPol + " berhasil masuk.", "Info", JOptionPane.INFORMATION_MESSAGE);
        tfNomorPolisiMasuk.setText("");
        rbMotor.setSelected(true);
    }

    private void prosesKeluar() {
        String noPol = tfNomorPolisiKeluar.getText().toUpperCase().trim();
        if (noPol.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan Nomor Polisi kendaraan yang akan keluar!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!daftarKendaraan.containsKey(noPol)) {
            JOptionPane.showMessageDialog(this, "Kendaraan dengan nomor polisi " + noPol + " tidak ditemukan di parkiran.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Kendaraan kendaraan = daftarKendaraan.get(noPol);
        LocalDateTime waktuKeluar = LocalDateTime.now();
        long durasiMenit = Duration.between(kendaraan.getWaktuMasuk(), waktuKeluar).toMinutes();
        long totalJam = (long) Math.ceil(durasiMenit / 60.0);
        if (totalJam == 0) totalJam = 1;

        long biaya;
        if (kendaraan.getJenisKendaraan().equals("Motor")) {
            biaya = TARIF_AWAL_MOTOR + Math.max(0, totalJam - 1) * TARIF_PER_JAM_MOTOR;
        } else {
            biaya = TARIF_AWAL_MOBIL + Math.max(0, totalJam - 1) * TARIF_PER_JAM_MOBIL;
        }

        // (BARU) Menambahkan data ke tabel riwayat
        modelRiwayat.addRow(new Object[]{
            kendaraan.getNomorPolisi(),
            kendaraan.getJenisKendaraan(),
            kendaraan.getWaktuMasuk().format(formatter),
            waktuKeluar.format(formatter), // Menambahkan waktu keluar
            totalJam,
            String.format("%,d", biaya) // Format biaya dengan pemisah ribuan
        });

        // Hapus dari daftar dan tabel aktif
        daftarKendaraan.remove(noPol);
        for (int i = 0; i < modelTabelAktif.getRowCount(); i++) {
            if (modelTabelAktif.getValueAt(i, 0).equals(noPol)) {
                modelTabelAktif.removeRow(i);
                break;
            }
        }

        // Tampilkan struk
        String detailBiaya = String.format(
            "===== Struk Parkir =====\n" +
            "Nomor Polisi: %s\n" +
            "Waktu Keluar: %s\n" +
            "Durasi: %d jam\n" +
            "Total Biaya: Rp %,d",
            kendaraan.getNomorPolisi(),
            waktuKeluar.format(formatter),
            totalJam,
            biaya
        );
        JOptionPane.showMessageDialog(this, new JTextArea(detailBiaya), "Detail Biaya Parkir", JOptionPane.INFORMATION_MESSAGE);

        tfNomorPolisiKeluar.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SistemParkirLengkap gui = new SistemParkirLengkap();
            gui.setVisible(true);
        });
    }
}