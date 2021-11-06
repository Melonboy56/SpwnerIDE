package me.melonsboy.spwn.ide;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

public class window {
    public JFrame windowframe;
    public JPanel thepanel;
    private JMenuBar menuBar;
    public window(String title) {
        try {
            UIManager.setLookAndFeel(theme_loader.selected_theme.get_look_and_feel());
            Enumeration keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof FontUIResource) {
                    UIManager.put(key, new FontUIResource(Main.font));
                }
            }

        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        windowframe = new JFrame(title);
        windowframe.setFont(Main.font);
        windowframe.setIconImage(new ImageIcon(Main.class.getResource("assets/icon.png")).getImage());
        this.set_size(900,600);
        windowframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        windowframe.setVisible(true);
        windowframe.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!(thepanel==null)) {
                    thepanel.updateUI();
                }
            }
        });
        /*
         * windowframe.addComponentListener(new ComponentAdapter() {
         *             @Override
         *             public void componentResized(ComponentEvent e) {
         *                 if (windowframe.getState() == Frame.ICONIFIED) {
         *                     System.out.println("RESIZED TO ICONIFIED");
         *                 } else if (windowframe.getState() == Frame.NORMAL) {
         *                     System.out.println("RESIZED TO NORMAL");
         *                 } else {
         *                     System.out.println("RESIZED TO MAXIMIZED");
         *                 }
         *             }
         *         });
         */
        windowframe.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (windowframe.getExtendedState()==JFrame.MAXIMIZED_BOTH) {
                    Main.configjson.put("mainwindow_is_maxed",true);
                } else {
                    Main.configjson.put("mainwindow_is_maxed",false);
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
                Main.disable_plugins();
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
