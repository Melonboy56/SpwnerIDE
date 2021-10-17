package me.melonsboy.spwn.ide;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class compile extends Thread {
    private JPanel textArea;
    private String filepath;
    private String console_log = "";
    public String command = "";
    private List<String> console_colors = new ArrayList<>();
    public compile(JPanel textArea, String filepath) {
        this.textArea = textArea;
        this.filepath = filepath;
        command = Main.getJarfolder_path()+"/compile.exe build \""+filepath+"\"";
        console_log = console_log+command+"\n";
        textArea.removeAll();
        JLabel label = new JLabel(command);
        label.setBackground(Color.DARK_GRAY);
        label.setForeground(Color.WHITE);
        label.setFont(Main.font);
        textArea.setLayout(new BoxLayout(textArea,BoxLayout.PAGE_AXIS));
        textArea.add(label);
        textArea.updateUI();

        // reset
        console_colors.add("\033[0m");
        // normal colors
        console_colors.add("\033[30m");
        console_colors.add("\033[31m");
        console_colors.add("\033[32m");
        console_colors.add("\033[33m");
        console_colors.add("\033[34m");
        console_colors.add("\033[35m");
        console_colors.add("\033[36m");
        console_colors.add("\033[37m");
        // bold
        console_colors.add("\033[30m");
        console_colors.add("\033[31m");
        console_colors.add("\033[32m");
        console_colors.add("\033[33m");
        console_colors.add("\033[34m");
        console_colors.add("\033[35m");
        console_colors.add("\033[36m");
        console_colors.add("\033[37m");
        // underline
        console_colors.add("\033[30m");
        console_colors.add("\033[31m");
        console_colors.add("\033[32m");
        console_colors.add("\033[33m");
        console_colors.add("\033[34m");
        console_colors.add("\033[35m");
        console_colors.add("\033[36m");
        console_colors.add("\033[37m");
        // background
        console_colors.add("\033[40m");
        console_colors.add("\033[41m");
        console_colors.add("\033[42m");
        console_colors.add("\033[43m");
        console_colors.add("\033[44m");
        console_colors.add("\033[45m");
        console_colors.add("\033[46m");
        console_colors.add("\033[47m");
        // high intensity
        console_colors.add("\033[90m");
        console_colors.add("\033[90m");
        console_colors.add("\033[90m");
        console_colors.add("\033[90m");
        console_colors.add("\033[90m");
        console_colors.add("\033[90m");
        console_colors.add("\033[90m");
        console_colors.add("\033[90m");
        // bold high intensity
        console_colors.add("\033[90m");
        console_colors.add("\033[91m");
        console_colors.add("\033[92m");
        console_colors.add("\033[93m");
        console_colors.add("\033[94m");
        console_colors.add("\033[95m");
        console_colors.add("\033[96m");
        console_colors.add("\033[97m");
        // high intensity backgrounds
        console_colors.add("\033[100m");
        console_colors.add("\033[101m");
        console_colors.add("\033[102m");
        console_colors.add("\033[103m");
        console_colors.add("\033[104m");
        console_colors.add("\033[105m");
        console_colors.add("\033[106m");
        console_colors.add("\033[107m");
    }

    @Override
    public void run() {
        try {
            File compiler_file = new File(Main.getJarfolder_path()+"/compile.exe");
            if (compiler_file.exists() && compiler_file.isFile()) {
                ProcessBuilder builder = new ProcessBuilder(Main.getJarfolder_path()+"/compile.exe","build",filepath);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                InputStream is = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    String replaced_line = line.replaceAll("\u001B\\[[;\\d]*m", "");
                    try {
                        JLabel label = new JLabel(replaced_line);
                        label.setBackground(Color.DARK_GRAY);
                        label.setForeground(Color.WHITE);
                        label.setFont(Main.font);
                        textArea.setLayout(new BoxLayout(textArea,BoxLayout.PAGE_AXIS));
                        textArea.add(label);
                        textArea.updateUI();
                    } catch (NullPointerException exception) {
                        exception.printStackTrace();
                    }
                    SwingUtilities.invokeLater(()->textArea.updateUI());
                }
            } else {
                JOptionPane.showMessageDialog(Main.idewindow.windowframe,"compile.exe doesn't exist at ("+Main.getJarfolder_path()+"/compile.exe"+") or compile.exe is not a file");
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
