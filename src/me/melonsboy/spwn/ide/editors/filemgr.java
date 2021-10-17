package me.melonsboy.spwn.ide.editors;

import me.melonsboy.spwn.ide.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;

public class filemgr {
    public static JPanel load_file(String filepath, boolean is_template, stringholder text) throws FileNotFoundException {
        if (util.get_extension(filepath).equalsIgnoreCase("txt")) {
            return new txt(filepath,is_template,text);
        } else if (util.get_extension(filepath).equalsIgnoreCase("spwn")) {
            return new spwn(filepath,is_template,text);
        } else if (util.get_extension(filepath).equalsIgnoreCase("json")) {
            return new json(filepath,is_template,text);
        } else {
            // show unknown file format
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel label = new JLabel("Unknown file format: "+util.get_extension(filepath),JLabel.CENTER);
            label.setFont(Main.font);
            label.setForeground(Color.WHITE);
            panel.add(label,BorderLayout.CENTER);
            panel.setBackground(Color.DARK_GRAY);

            JLabel close_button = new JLabel("Close file",JLabel.CENTER);
            close_button.setForeground(Color.WHITE);
            close_button.setBackground(Color.BLACK);
            close_button.setFont(Main.font);
            String finalFilepath = filepath;
            close_button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if (Main.idewindow.thepanel instanceof editor) {
                            ((editor)Main.idewindow.thepanel).textEditor.file_deleted(finalFilepath);
                        } else if (Main.idewindow.thepanel instanceof templates) {
                            ((templates)Main.idewindow.thepanel).fileEditor.file_deleted(finalFilepath);
                        }
                    } catch (Exception ex) {
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
            panel.add(close_button,BorderLayout.PAGE_END);

            return panel;
        }
    }
}
