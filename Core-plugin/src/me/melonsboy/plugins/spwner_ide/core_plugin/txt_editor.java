package me.melonsboy.plugins.spwner_ide.core_plugin;

import me.melonsboy.spwn.ide.*;
import me.melonsboy.spwn.ide.custom.save_button;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

class txt_editor extends JPanel {
    private RSyntaxTextArea textArea;
    private void save_file(boolean is_template, String filepath,String[] text) throws IOException {
        if (!is_template) {
            FileWriter fileWriter = new FileWriter(filepath);
            fileWriter.write(textArea.getText());
            fileWriter.close();
            text[0] = textArea.getText();
        }
    }

    public txt_editor(String filepath, boolean is_template, stringholder contenttext) throws Exception {
        StringBuilder textstring = new StringBuilder();
        if (!is_template) {
            FileReader textfile = new FileReader(filepath);
            Scanner readfile = new Scanner(textfile);
            while (readfile.hasNextLine()) {
                textstring.append(readfile.nextLine()).append("\n");
            }
            readfile.close();
            if (!(textstring.length() == 0)) {
                textstring.deleteCharAt(textstring.length() - 1);
            }
        }
        if (is_template) {
            for (int i = 0; i < textstring.length(); i++) {
                textstring.deleteCharAt(i);
            }
            textstring.append(contenttext.data);
        }
        setLayout(new BorderLayout());
        textArea = new RSyntaxTextArea();
        Theme.load(new ByteArrayInputStream(theme_loader.selected_theme.get_rsyntaxtextarea_theme().getBytes(StandardCharsets.UTF_8))).apply(textArea);
        setfont.setfont(Main.font, textArea.getAccessibleContext());
        textArea.setText(textstring.toString());
        textArea.setCodeFoldingEnabled(true);
        add(new RTextScrollPane(textArea), BorderLayout.CENTER);
        final String[] text = {textArea.getText()};
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!is_template) {
                    if (text[0].equals(textArea.getText())) {
                        if (Main.idewindow.thepanel instanceof editor) {
                            ((editor) Main.idewindow.thepanel).textEditor.toggle_save_state(filepath, new File(filepath).getName(), true);
                        }
                    } else {
                        ((editor) Main.idewindow.thepanel).textEditor.toggle_save_state(filepath, new File(filepath).getName(), false);
                        if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                            if (e.getKeyCode() == KeyEvent.VK_S) {
                                try {
                                    save_file(is_template,filepath,text);
                                    ((editor) Main.idewindow.thepanel).textEditor.toggle_save_state(filepath, new File(filepath).getName(), true);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    contenttext.data = textArea.getText();
                }
            }
        });
        JLabel close_button = new JLabel("Close file", JLabel.CENTER);
        close_button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    save_file(is_template,filepath,text);
                    if (Main.idewindow.thepanel instanceof editor) {
                        ((editor) Main.idewindow.thepanel).textEditor.file_deleted(filepath);
                    } else if (Main.idewindow.thepanel instanceof templates) {
                        ((templates) Main.idewindow.thepanel).fileEditor.file_deleted(filepath);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                e.getComponent().setForeground(Color.GREEN);
                e.getComponent().repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setForeground(null);
                e.getComponent().repaint();
            }
        });
        add(close_button, BorderLayout.PAGE_END);
        save_button hidden_save_button = new save_button("s");
        hidden_save_button.addActionListener(e -> {
            try {
                if (!is_template) {
                    save_file(is_template,filepath,text);
                    ((editor) Main.idewindow.thepanel).textEditor.toggle_save_state(filepath, new File(filepath).getName(), true);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        hidden_save_button.setPreferredSize(new Dimension(0, 0));
        hidden_save_button.setVisible(false);
    }
}
