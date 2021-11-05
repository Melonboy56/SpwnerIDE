package me.melonsboy.spwn.ide;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;

public class setfont {
    public static void setfont(Font font, JComponent component) {
        for (Component i : component.getComponents()) {
            i.setFont(font);
            if (i instanceof JComponent) {
                setfont(font,(JComponent)i);
            }
        }
    }
    public static void setfont(Font font, JMenuBar menuBar) {
        if (!(menuBar==null)) {
            setfont(font,menuBar.getAccessibleContext());
        }
    }
    public static void setfont(Font font, AccessibleContext accessibleContext) {
        accessibleContext.getAccessibleComponent().setFont(font);
        for (int i = 0; i < accessibleContext.getAccessibleChildrenCount(); i++) {
            setfont(font,accessibleContext.getAccessibleChild(i).getAccessibleContext());
        }
    }

    public static void setfont(Font font, JFrame windowframe) {
        for (Component i : windowframe.getComponents()) {
            i.setFont(new Font(font.getFontName(),i.getFont().getStyle(),font.getSize()));
            setfont(font,(JComponent)i);
        }
        setfont(font,windowframe.getJMenuBar());
    }
}
