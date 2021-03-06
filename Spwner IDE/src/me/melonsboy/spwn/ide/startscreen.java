package me.melonsboy.spwn.ide;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class startscreen extends JPanel {
    private JSONArray projectarray;
    public startscreen() {
        super();
        Main.idewindow.windowframe.setTitle(Main.program_name);
        Main.idewindow.set_menubar(null);
        this.setLayout(new BorderLayout());
        this.updateUI();
    }

    @Override
    public void setVisible(boolean aFlag) {
        if (!aFlag) {
            try {
                Main.configjson.put("mainwindow_width",Main.idewindow.windowframe.getWidth());
                Main.configjson.put("mainwindow_height",Main.idewindow.windowframe.getHeight());
                Main.save_config();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        super.setVisible(aFlag);
    }

    @Override
    public void updateUI() {
        this.removeAll();
        this.add(get_project_list(),BorderLayout.CENTER);
        this.add(get_buttons_panel(),BorderLayout.PAGE_END);
        super.updateUI();
    }

    private JPanel get_project_list() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        projectarray = ((JSONArray)Main.configjson.get("project_paths"));
        for (int i = 0; i < projectarray.size(); i++) {
            try {
                String path = (String) projectarray.get(i);
                FileReader fileReader = new FileReader(path + "/"+Main.project_json_name);
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(fileReader);
                fileReader.close();
                String name = (String) jsonObject.get("projectname");
                JPanel buttonpanel = make_button_panel(name, path, i);
                buttonpanel.setBounds(5, (i * ((Main.font.getSize()*2)+5)) + 5, Main.idewindow.windowframe.getWidth() - 33, Main.font.getSize()*2);
                panel.add(buttonpanel);
            } catch (FileNotFoundException e) {
                String name = "Project not found";
                String path = (String) projectarray.get(i);
                JPanel buttonpanel = make_button_panel(name, path, i);
                buttonpanel.setBounds(5, (i * ((Main.font.getSize()*2)+5)) + 5, Main.idewindow.windowframe.getWidth() - 33, Main.font.getSize()*2);
                panel.add(buttonpanel);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
        return panel;
    }
    private void remove_item_from_list(int i) {
        Main.remove_recent_project_path((String)projectarray.get(i));
        projectarray.remove(i);
        try {
            Main.save_config();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Main.idewindow.set_screen(new startscreen());
    }
    private void open_project(String filepath,int i) {
        if (!Main.open_project(filepath)) {
            // false
            int output = JOptionPane.showConfirmDialog(Main.idewindow.windowframe,"That project doesn't exist anymore, remove it from the list?",Main.idewindow.windowframe.getTitle(),JOptionPane.YES_NO_OPTION);
            if (output==JOptionPane.YES_OPTION) {
                remove_item_from_list(i);
            }
        }
    }
    private JPanel make_button_panel(String name, String filepath, int i) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        JLabel namelabel = new JLabel(name,SwingConstants.LEFT);
        JLabel filepathlabel = new JLabel(filepath,SwingConstants.LEFT);
        namelabel.setBounds(0,0,Main.idewindow.windowframe.getWidth(),Main.font.getSize());
        filepathlabel.setBounds(0,Main.font.getSize(), Main.idewindow.windowframe.getWidth(),Main.font.getSize());
        panel.add(namelabel);
        panel.add(filepathlabel);
        panel.setOpaque(true);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    JMenuItem open_project = new JMenuItem("Open project");
                    JMenuItem remove_project = new JMenuItem("Remove project from list");
                    open_project.addActionListener(e1 -> {
                        open_project(filepath,i);
                    });
                    remove_project.addActionListener(e1 -> {
                        remove_item_from_list(i);
                        Main.remove_recent_project_path((String) projectarray.get(i));
                    });
                    popupMenu.add(open_project);
                    popupMenu.add(remove_project);
                    popupMenu.show(Main.idewindow.windowframe,e.getX(),e.getY());
                } else {
                    open_project(filepath,i);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(panel.getBackground().darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(null);
            }
        });
        return panel;
    }
    private JPanel get_buttons_panel() {
        JPanel panel = new JPanel();
        JButton newproject = new JButton("New project");
        JButton openproject = new JButton("Open");
        JButton settingsbutton = new JButton("Settings");
        JButton templatesbutton = new JButton("Templates");
        panel.add(newproject);
        panel.add(openproject);
        panel.add(settingsbutton);
        panel.add(templatesbutton);
        newproject.addActionListener(e -> {
            try {
                me.melonsboy.spwn.ide.newproject.newproject();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        openproject.addActionListener(e -> {
            try {
                me.melonsboy.spwn.ide.openproject.openproject();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        settingsbutton.addActionListener(e -> {
            try {
                settings.settings();
            } catch (IOException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        });
        templatesbutton.addActionListener(e -> {
            try {
                Main.idewindow.set_screen(new templates());
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });
        return panel;
    }
}
