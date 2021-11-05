package me.melonsboy.plugins.spwner_ide.core_plugin.custom;

import javax.swing.*;

public class console {
    public String get_name() {
        return "Console Name example";
    }
    public JPanel get_instance() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Console Example"));
        return panel;
    }
}
