package me.melonsboy.spwn.ide;

import javax.swing.*;

public abstract class file {
    public abstract JPanel get_instance(String filepath, boolean is_template, stringholder contexttext);
    public abstract ImageIcon get_icon();
}
