import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class ParkiranMallGUI {

    private static final int TOTAL_SLOT = 15;
    private static int slotTersedia = TOTAL_SLOT;
    private static final Map<String, Kendaraan> dataParkir = new LinkedHashMap<>();
    private static final DecimalFormat formatUang = new DecimalFormat("Rp#,###");
    private static final String FILE_PATH = "data_parkir.txt";

    static class Kendaraan {
        String tipe;
        int jamMasuk;

        Kendaraan(String tipe, int jamMasuk) {
            this.tipe = tipe;
            this.jamMasuk = jamMasuk;
        }

        @Override
        public String toString() {
            return tipe + ";" + jamMasuk;
        }

        public static Kendaraan fromString(String data) {
            String[] parts = data.split(";");
            return new Kendaraan(parts[0], Integer.parseInt(parts[1]));
        }
    }

    public static void main(String[] args) {
        bacaDataDariFile();
        SwingUtilities.invokeLater(ParkiranMallGUI::buatGUI);
    }

    private static void buatGUI() {
        JFrame frame = new JFrame("Sistem Parkir Mall");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JButton masukBtn = new JButton("Kendaraan Masuk");
        JButton keluarBtn = new JButton("Kendaraan Keluar");
        JButton cekParkiranBtn = new JButton("Cek Parkiran");
        JButton keluarProgramBtn = new JButton("Keluar Program");

        masukBtn.addActionListener(e -> kendaraanMasuk());
        keluarBtn.addActionListener(e -> kendaraanKeluar());
        cekParkiranBtn.addActionListener(e -> tampilkanParkiran());
        keluarProgramBtn.addActionListener(e -> {
            simpanDataKeFile();
            System.exit(0);
        });

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.add(masukBtn);
        panel.add(keluarBtn);
        panel.add(cekParkiranBtn);
        panel.add(keluarProgramBtn);

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void kendaraanMasuk() {
        String plat = JOptionPane.showInputDialog(null, "Masukkan plat nomor:").toUpperCase();
        if (plat == null || plat.isBlank()) return;

        if (dataParkir.containsKey(plat)) {
            JOptionPane.showMessageDialog(null, "Kendaraan sudah parkir.");
            return;
        }

        if (slotTersedia <= 0) {
            JOptionPane.showMessageDialog(null, "Slot parkir penuh.");
            return;
        }

        String tipe = JOptionPane.showInputDialog(null, "Jenis kendaraan (motor/mobil):").toLowerCase();
        if (!tipe.equals("motor") && !tipe.equals("mobil")) {
            JOptionPane.showMessageDialog(null, "Jenis tidak valid.");
            return;
        }

        String jamStr = JOptionPane.showInputDialog(null, "Jam masuk (0-23):");
        int jamMasuk;
        try {
            jamMasuk = Integer.parseInt(jamStr);
            if (jamMasuk < 0 || jamMasuk > 23) throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Jam tidak valid.");
            return;
        }

        dataParkir.put(plat, new Kendaraan(tipe, jamMasuk));
        slotTersedia--;
        simpanDataKeFile();

        JOptionPane.showMessageDialog(null,
                tipe.toUpperCase() + " " + plat + " berhasil masuk.\nSlot tersisa: " + slotTersedia);
    }

    private static void kendaraanKeluar() {
        String plat = JOptionPane.showInputDialog(null, "Masukkan plat nomor:").toUpperCase();
        if (plat == null || !dataParkir.containsKey(plat)) {
            JOptionPane.showMessageDialog(null, "Kendaraan tidak ditemukan.");
            return;
        }

        Kendaraan k = dataParkir.get(plat);

        String jamStr = JOptionPane.showInputDialog(null, "Jam keluar (0-23):");
        int jamKeluar;
        try {
            jamKeluar = Integer.parseInt(jamStr);
            if (jamKeluar < 0 || jamKeluar > 23) throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Jam keluar tidak valid.");
            return;
        }

        int durasi = (jamKeluar - k.jamMasuk + 24) % 24;
        if (durasi == 0) durasi = 1;

        int tarifAwal = k.tipe.equals("motor") ? 4000 : 5000;
        int tarifTambahan = k.tipe.equals("motor") ? 4000 : 5000;
        int total = tarifAwal + (durasi - 1) * tarifTambahan;

        String struk = "STRUK PARKIR\n"
                + "=====================\n"
                + "Plat Nomor : " + plat + "\n"
                + "Jenis      : " + k.tipe + "\n"
                + "Jam Masuk  : " + k.jamMasuk + ".00\n"
                + "Jam Keluar : " + jamKeluar + ".00\n"
                + "Durasi     : " + durasi + " jam\n"
                + "Total Bayar: " + formatUang.format(total) + ",-";

        JOptionPane.showMessageDialog(null, struk);
        dataParkir.remove(plat);
        slotTersedia++;
        simpanDataKeFile();
    }

    private static void tampilkanParkiran() {
        StringBuilder sb = new StringBuilder();
        sb.append("Parkiran Tersedia: ").append(slotTersedia).append(" slot\n");
        sb.append("============================\n");

        if (dataParkir.isEmpty()) {
            sb.append("Belum ada kendaraan yang parkir.");
        } else {
            sb.append("Kendaraan yang sedang parkir:\n");
            int nomor = 1;
            for (Map.Entry<String, Kendaraan> entry : dataParkir.entrySet()) {
                sb.append(nomor++).append(". ")
                        .append(entry.getKey()).append(" (")
                        .append(entry.getValue().tipe).append(")\n");
            }
        }

        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private static void simpanDataKeFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Map.Entry<String, Kendaraan> entry : dataParkir.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue().toString());
            }
        } catch (IOException e) {
            System.err.println("❌ Gagal menyimpan data ke file.");
        }
    }

    private static void bacaDataDariFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    dataParkir.put(parts[0], Kendaraan.fromString(parts[1]));
                }
            }
            slotTersedia = TOTAL_SLOT - dataParkir.size();
        } catch (IOException e) {
            // File mungkin belum ada, abaikan
        }
    }
}
