package me.melonsboy.spwn.ide.editors;

import me.melonsboy.spwn.ide.Main;
import me.melonsboy.spwn.ide.editor;
import me.melonsboy.spwn.ide.stringholder;
import me.melonsboy.spwn.ide.templates;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Scanner;

public class txt extends JPanel {
    private RSyntaxTextArea textArea;
    public String get_text() {
        return textArea.getText();
    }
    public txt(String filepath, boolean is_template, stringholder contenttext) throws FileNotFoundException {
        super();
        StringBuilder textstring = new StringBuilder();
        if (!is_template) {
            FileReader textfile = new FileReader(filepath);
            Scanner readfile = new Scanner(textfile);
            while (readfile.hasNextLine()) {
                textstring.append(readfile.nextLine()).append("\n");
            }
            readfile.close();
            if (!(textstring.length()==0)) { textstring.deleteCharAt(textstring.length()-1); }
        }
        if (is_template) {
            for (int i = 0; i < textstring.length(); i++) {
                textstring.deleteCharAt(i);
            }
            textstring.append(contenttext.data);
        }
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());
        textArea = new RSyntaxTextArea();
        textArea.setFont(Main.font);
        textArea.setText(textstring.toString());
        textArea.setBackground(Color.DARK_GRAY);
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);
        textArea.setCurrentLineHighlightColor(Color.BLACK);
        textArea.setCodeFoldingEnabled(true);
        add(new RTextScrollPane(textArea),BorderLayout.CENTER);
        final String[] text = {textArea.getText()};
        class savefile {
            public void savefile() throws IOException {
                if (!is_template) {
                    FileWriter fileWriter = new FileWriter(filepath);
                    fileWriter.write(textArea.getText());
                    fileWriter.close();
                    text[0] = textArea.getText();
                }
            }
        }
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!is_template) {
                    if (text[0].equals(textArea.getText())) {
                        if (Main.idewindow.thepanel instanceof editor) {
                            ((editor)Main.idewindow.thepanel).textEditor.toggle_save_state(filepath,new File(filepath).getName(),true);
                        }
                    } else {
                        ((editor)Main.idewindow.thepanel).textEditor.toggle_save_state(filepath,new File(filepath).getName(),false);
                        if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                            if (e.getKeyCode()==KeyEvent.VK_S) {
                                try {
                                    new savefile().savefile();
                                    ((editor)Main.idewindow.thepanel).textEditor.toggle_save_state(filepath,new File(filepath).getName(),true);
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
        JLabel close_button = new JLabel("Close file",JLabel.CENTER);
        close_button.setForeground(Color.WHITE);
        close_button.setBackground(Color.BLACK);
        close_button.setFont(Main.font);
        close_button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    new savefile().savefile();
                    if (Main.idewindow.thepanel instanceof editor) {
                        ((editor)Main.idewindow.thepanel).textEditor.file_deleted(filepath);
                    } else if (Main.idewindow.thepanel instanceof templates) {
                        ((templates)Main.idewindow.thepanel).fileEditor.file_deleted(filepath);
                    }
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
        add(close_button,BorderLayout.PAGE_END);
        JButton hidden_save_button = new JButton("s");
        hidden_save_button.addActionListener(e -> {
            try {
                if (!is_template) {
                    new savefile().savefile();
                    ((editor)Main.idewindow.thepanel).textEditor.toggle_save_state(filepath,new File(filepath).getName(),true);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        hidden_save_button.setPreferredSize(new Dimension(0,0));
        hidden_save_button.setVisible(false);
    }
}
