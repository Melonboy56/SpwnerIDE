package me.melonsboy.spwn.ide.custom;

import me.melonsboy.spwn.ide.util;

import javax.swing.*;

public class iconButton {
    public int placement;
    public boolean show_in_templates;
    private JLabel iconlabel;

    public void button_clicked(boolean is_right_click) {

    }
    public ImageIcon get_icon() {
        return null;
    }
    public String get_tooltip() {
        return "ToolTip Example";
    }
    protected final void update_icon() {
        iconlabel.setIcon(util.scaleImage(get_icon(),32,32));
    }
    public final void put_label(JLabel label) {
        iconlabel=label;
    }
}
