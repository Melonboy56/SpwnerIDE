package me.melonsboy.spwn.ide.custom;

import me.melonsboy.spwn.ide.Plugin;

import java.awt.event.ActionListener;
import java.util.ArrayList;

public class menuitem {
    public String menuname;
    public boolean show_on_templates = true;
    public String path;
    public Plugin plugin;
    public menuitem(String name) {
        this.menuname=name;
        path="File/Plugins/{pluginname}";
    }
    public void menu_clicked() {

    }
}
