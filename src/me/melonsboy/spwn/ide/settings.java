package me.melonsboy.spwn.ide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class settings {

    public static void settings() throws IOException {
        JPanel settingspanel = new JPanel();
        settingspanel.setPreferredSize(new Dimension(800,400));

        JPanel fontsetting_panel = new JPanel();
        JLabel fontsetting_panel_label = new JLabel("Font size");
        fontsetting_panel_label.setPreferredSize(new Dimension(190,Main.font.getSize()));
        fontsetting_panel_label.setForeground(Color.WHITE);
        fontsetting_panel_label.setFont(Main.font);
        JTextField fontsetting_panel_input = new JTextField(Main.configjson.get("fontsize").toString()) {
            /**
             * code below is copied from <a href="https://stackoverflow.com/a/11609982">https://stackoverflow.com/a/11609982</a>
             */
            public void processKeyEvent(KeyEvent ev) {
                char c = ev.getKeyChar();
                try {
                    // Ignore all non-printable characters. Just check the printable ones.
                    if (c > 31 && c < 127) {
                        Integer.parseInt(c + "");
                    }
                    super.processKeyEvent(ev);
                }
                catch (NumberFormatException nfe) {
                    // Do nothing. Character inputted is not a number, so ignore it.
                }
            }
        };
        fontsetting_panel_input.setPreferredSize(new Dimension(settingspanel.getPreferredSize().width-200,Main.font.getSize()+10));
        fontsetting_panel_input.setFont(Main.font);
        fontsetting_panel.add(fontsetting_panel_label);
        fontsetting_panel.add(fontsetting_panel_input);
        fontsetting_panel.setBounds(0,(Main.font.getSize()+10)*2,settingspanel.getWidth(),Main.font.getSize()+15);
        settingspanel.add(fontsetting_panel);

        JPanel openproject_on_startup_panel = new JPanel();
        JCheckBox openproject_on_startup_panel_checkbox = new JCheckBox("Open last opened project on startup");
        openproject_on_startup_panel_checkbox.setPreferredSize(new Dimension(settingspanel.getPreferredSize().width-10,Main.font.getSize()));
        openproject_on_startup_panel_checkbox.setForeground(Color.WHITE);
        openproject_on_startup_panel_checkbox.setFont(Main.font);
        openproject_on_startup_panel.setBounds(0,(Main.font.getSize()+10)*3,settingspanel.getPreferredSize().width,Main.font.getSize()+15);
        openproject_on_startup_panel.add(openproject_on_startup_panel_checkbox);
        settingspanel.add(openproject_on_startup_panel);

        openproject_on_startup_panel_checkbox.setSelected((Boolean) Main.configjson.get("open_project_on_startup"));
        String[] options = new String[] {"Ok"};
        JOptionPane.showOptionDialog(Main.idewindow.windowframe,settingspanel,Main.idewindow.windowframe.getTitle(),JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
        Main.configjson.put("fontsize",Integer.parseInt(fontsetting_panel_input.getText()));
        Main.configjson.put("open_project_on_startup",openproject_on_startup_panel_checkbox.isSelected());
        Main.save_config();
        Main.font = new Font(new JLabel().getFont().getFontName(),Font.BOLD,Integer.parseInt(Main.configjson.get("fontsize").toString()));
        setfont.setfont(Main.font,Main.idewindow.windowframe);
    }
}
