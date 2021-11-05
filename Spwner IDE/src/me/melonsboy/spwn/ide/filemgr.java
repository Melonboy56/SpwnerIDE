package me.melonsboy.spwn.ide;

import me.melonsboy.spwn.ide.*;
import me.melonsboy.spwn.ide.editors.json;
import me.melonsboy.spwn.ide.editors.spwn;
import me.melonsboy.spwn.ide.editors.txt;
import me.melonsboy.spwn.ide.exceptions.extension_already_exists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class filemgr {
    private static HashMap<String,file> stringfileHashMap = new HashMap<>();
    public static void add_file_ext_instance(String extension, file file) throws extension_already_exists {
        if (stringfileHashMap.containsKey(extension)) {
            throw new extension_already_exists(extension);
        } else {
            stringfileHashMap.put(extension,file);
        }
    }
    public static boolean is_ext_in_system(String extension) {
        return stringfileHashMap.containsKey(extension);
    }
    public static Icon get_icon(String filepath) {
        if (stringfileHashMap.containsKey(util.get_extension(filepath))) {
            return util.scaleImage(stringfileHashMap.get(util.get_extension(filepath)).get_icon(),32,32);
        } else {
            return null;
        }
    }
    public static JPanel load_file(String filepath, boolean is_template, stringholder text) throws IOException {
        if (stringfileHashMap.containsKey(util.get_extension(filepath))) {
            return stringfileHashMap.get(util.get_extension(filepath)).get_instance(filepath, is_template, text);
        } else {
            // show unknown file format
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel label = new JLabel("Unknown file format: "+util.get_extension(filepath),JLabel.CENTER);
            panel.add(label,BorderLayout.CENTER);

            JLabel close_button = new JLabel("Close file",JLabel.CENTER);
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
                    e.getComponent().setForeground(Color.GREEN);
                    e.getComponent().repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    e.getComponent().setForeground(null);
                    e.getComponent().repaint();
                }
            });
            panel.add(close_button,BorderLayout.PAGE_END);

            return panel;
        }
    }
}
