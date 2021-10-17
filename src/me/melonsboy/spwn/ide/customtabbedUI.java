package me.melonsboy.spwn.ide;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class customtabbedUI extends BasicTabbedPaneUI {
    private Color backgroundColor;

    public customtabbedUI(Color backgroundColor) {
        super();
        this.backgroundColor = backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Rectangle bounds = tabPane.getBounds();
        g.setColor(this.backgroundColor);
        g.fillRect(0, 0, bounds.width, bounds.height);

        super.paint(g, c); // Call parent...
    }
}