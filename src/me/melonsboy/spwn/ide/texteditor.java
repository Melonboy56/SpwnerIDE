package me.melonsboy.spwn.ide;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class texteditor extends JPanel {
    public JTabbedPane filetabs;
    private HashMap<String, JPanel> textfiles = new HashMap<>();
    private List<String> opened_files = new ArrayList<>();
    public texteditor() {
        super();
        this.setLayout(new BorderLayout());
        this.setBackground(Color.DARK_GRAY);
        filetabs = new JTabbedPane();
        filetabs.setFont(Main.font);
        filetabs.setBackground(Color.DARK_GRAY);
        filetabs.setForeground(Color.WHITE);
        filetabs.setUI(new customtabbedUI(Color.DARK_GRAY));
        this.add(filetabs,BorderLayout.CENTER);
    }
    public void file_deleted(String filepath) {
        if (!(textfiles.get(filepath)==null)) {
            filetabs.remove(textfiles.get(filepath));
            textfiles.remove(filepath);
        }
    }
    public void save_all() {
        for (Map.Entry<String, JPanel> i : textfiles.entrySet()) {
            JPanel panel = i.getValue();
            for (Component j : panel.getComponents()) {
                if (j instanceof JButton) {
                    ((JButton)j).doClick();
                }
            }
        }
    }
    public String get_selected_filepath() {
        filetabs.getSelectedIndex();
        return opened_files.get(filetabs.getSelectedIndex());
    }
    public void add_tab(String filepath) throws FileNotFoundException {
        if (opened_files.contains(filepath)) {
            filetabs.setSelectedComponent(textfiles.get(filepath));
            return;
        }
        FileReader textfile = new FileReader(filepath);
        Scanner readfile = new Scanner(textfile);
        StringBuilder textstring = new StringBuilder();
        while (readfile.hasNextLine()) {
            textstring.append(readfile.nextLine()).append("\n");
        }
        if (!(textstring.length()==0)) { textstring.deleteCharAt(textstring.length()-1); };
        JPanel textpanel = new JPanel();
        textpanel.setBackground(Color.DARK_GRAY);
        textpanel.setLayout(new BorderLayout());
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setFont(Main.font);
        textArea.setText(textstring.toString());
        textArea.setBackground(Color.DARK_GRAY);
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);
        textArea.setCurrentLineHighlightColor(Color.BLACK);
        textArea.setCodeFoldingEnabled(true);
        textpanel.add(new RTextScrollPane(textArea),BorderLayout.CENTER);
        filetabs.addTab(new File(filepath).getName(),textpanel);
        int index = filetabs.getSelectedIndex();
        String title = filetabs.getTitleAt(index);
        final String[] text = {textArea.getText()};
        class savefile {
            int _index = index;
            public void savefile() throws IOException {
                FileWriter fileWriter = new FileWriter(filepath);
                fileWriter.write(textArea.getText());
                fileWriter.close();
                filetabs.setTitleAt(_index,title);
                text[0] = textArea.getText();
            }
        }
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (text[0].equalsIgnoreCase(textArea.getText())) {
                    filetabs.setTitleAt(index,title);
                } else {
                    filetabs.setTitleAt(index,"*"+title);
                    if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                        if (e.getKeyCode()==KeyEvent.VK_S) {
                            try {
                                new savefile().savefile();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        JLabel close_button = new JLabel("Close file",JLabel.CENTER);
        close_button.setForeground(Color.WHITE);
        close_button.setBackground(Color.BLACK);
        close_button.setFont(Main.font);
        close_button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    new savefile().savefile();
                    textfiles.remove(filepath);
                    opened_files.remove(filepath);
                    filetabs.remove(textpanel);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                close_button.setForeground(Color.LIGHT_GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                close_button.setForeground(Color.WHITE);
            }
        });
        textpanel.add(close_button,BorderLayout.PAGE_END);
        JButton hidden_save_button = new JButton("s");
        hidden_save_button.addActionListener(e -> {
            try {
                new savefile().savefile();
                filetabs.setTitleAt(index,title);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        hidden_save_button.setPreferredSize(new Dimension(0,0));
        hidden_save_button.setVisible(false);
        textpanel.add(hidden_save_button,BorderLayout.PAGE_START);
        textfiles.put(filepath,textpanel);
        filetabs.setSelectedComponent(textpanel);
        opened_files.add(filepath);
    }
}
