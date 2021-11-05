package me.melonsboy.spwn.ide.themes;

import javax.swing.*;

public class native_theme extends Theme {
    private LookAndFeel lookAndFeel;

    public native_theme() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> lnfClass = Class.forName(UIManager.getSystemLookAndFeelClassName(), true, Thread.currentThread().getContextClassLoader());
        lookAndFeel = (LookAndFeel)(lnfClass.newInstance());
    }

    @Override
    public LookAndFeel get_look_and_feel() {
        return lookAndFeel;
    }

    @Override
    public String get_rsyntaxtextarea_theme() {
        return null;
    }

    @Override
    public String get_theme_name() {
        return "Native theme";
    }
}
