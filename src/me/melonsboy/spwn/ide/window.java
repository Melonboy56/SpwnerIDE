package me.melonsboy.spwn.ide;

import com.bulenkov.darcula.DarculaLaf;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class window {
    public JFrame windowframe;
    public JPanel thepanel;
    private JMenuBar menuBar;
    public window() {
        try {
            UIManager.setLookAndFeel(new DarculaLaf());
            UIManager.put("OptionPane.messageFont", Main.font);
            UIManager.put("OptionPane.buttonFont", Main.font);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        windowframe = new JFrame("Spwner IDE (v1.0 Alpha)");
        windowframe.setIconImage(new ImageIcon(Main.class.getResource("assets/icon.png")).getImage());
        this.set_size(900,600);
        windowframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        windowframe.getContentPane().setBackground(Color.BLACK);
        windowframe.setVisible(true);
        windowframe.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!(thepanel==null)) {
                    thepanel.updateUI();
                }
            }
        });
        windowframe.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                if (!(thepanel==null)) {
                    thepanel.updateUI();
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (!(thepanel==null)) {
                    thepanel.setVisible(false);
                    if (thepanel.isVisible()) {
                        return;
                    }
                }
                System.exit(0);
            }
        });
    }
    public void set_size(int width, int height) {
        windowframe.setSize(width,height);
    }
    public void set_screen(JPanel panel) {
        if (!(thepanel==null)) {
            windowframe.remove(thepanel);
        }
        windowframe.add(panel,BorderLayout.CENTER);
        panel.updateUI();
        thepanel = panel;
    }
    public void set_menubar(JMenuBar jMenuBar) {
        windowframe.setJMenuBar(jMenuBar);
        menuBar = jMenuBar;
    }
}
