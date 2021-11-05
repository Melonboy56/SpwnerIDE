package me.melonsboy.spwn.ide;

import me.melonsboy.spwn.ide.custom.compiler;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

public class compiler_config {
    private static void refresh_config_sel_list(JPanel configview, JSONObject compilerconfigs, JComboBox<String> compilers_combobox, boolean[] is_compent_enabled, Object refresh_combobox_sel,JDialog configdialog,String[] selected_label) {
        configview.removeAll();
        for (Object i : compilerconfigs.entrySet()) {
            JLabel label = new JLabel(((Map.Entry)i).getKey().toString(),JLabel.LEFT);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem renameconfig = new JMenuItem("Rename configuration");
                        renameconfig.addActionListener(e1 -> {
                            String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type new compile configuration name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
                            if (output==null) {return;}
                            if (output.replace(" ","").equalsIgnoreCase("")) {
                                JOptionPane.showMessageDialog(Main.idewindow.windowframe,"You can't have a empty name",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            if (compilerconfigs.containsKey(output)) {
                                JOptionPane.showMessageDialog(Main.idewindow.windowframe,"That configuration already exists",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            if (is_compent_enabled[0]) {
                                if (!(compilers_combobox.getSelectedIndex()==-1)) {
                                    Main.compilerArrayList.get(compilers_combobox.getSelectedIndex()).save_settings((JSONObject) ((JSONObject)compilerconfigs.get(selected_label[0])).get("settings"));
                                }
                            }
                            Object oldnameobject = compilerconfigs.get(label.getText());
                            compilerconfigs.put(output,oldnameobject);
                            compilerconfigs.remove(label.getText());
                            is_compent_enabled[0]=false;
                            compilers_combobox.setEnabled(false);
                            compilers_combobox.setSelectedIndex(-1);
                            refresh_config_sel_list(configview, compilerconfigs, compilers_combobox, is_compent_enabled, refresh_combobox_sel, configdialog, selected_label);
                            refresh_combobox_sel.toString();
                        });
                        popupMenu.add(renameconfig);
                        JMenuItem deleteconfig = new JMenuItem("Delete configuration");
                        deleteconfig.addActionListener(e1 -> {
                            int output = JOptionPane.showConfirmDialog(configdialog,"Are you sure you want to remove this configuration?",configdialog.getTitle(),JOptionPane.YES_NO_OPTION);
                            if (output==JOptionPane.YES_OPTION) {
                                compilerconfigs.remove(label.getText());
                                is_compent_enabled[0]=false;
                                compilers_combobox.setSelectedIndex(-1);
                                compilers_combobox.setEnabled(false);
                                refresh_config_sel_list(configview, compilerconfigs, compilers_combobox, is_compent_enabled, refresh_combobox_sel, configdialog, selected_label);
                                refresh_combobox_sel.toString();
                            }
                        });
                        popupMenu.add(deleteconfig);

                        popupMenu.show(configdialog,configdialog.getMousePosition().x,configdialog.getMousePosition().y);
                    } else {
                        if (is_compent_enabled[0]) {
                            Main.compilerArrayList.get(compilers_combobox.getSelectedIndex()).save_settings((JSONObject) ((JSONObject)compilerconfigs.get(selected_label[0])).get("settings"));
                        }
                        selected_label[0] =label.getText();
                        compilers_combobox.setEnabled(true);
                        compilers_combobox.setSelectedIndex(-1);
                        is_compent_enabled[0] =true;
                        for (compiler compiler : Main.compilerArrayList) {
                            if (((String)((JSONObject)compilerconfigs.get(selected_label[0])).get("compilername")).equalsIgnoreCase(compiler.compiler_name)) {
                                compilers_combobox.setSelectedItem(compiler.compiler_name);
                                break;
                            }
                        }
                        //compilers_combobox.setSelectedItem(compilers_combobox.getSelectedIndex());//.save_settings(((JSONObject)((JSONObject)projectjson.get("compileconfigs")).get(selected_label[0])).get("compilername"));
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    e.getComponent().setForeground(Color.GREEN);
                    e.getComponent().repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    e.getComponent().setForeground(null);
                    e.getComponent().repaint();
                }
            });
            configview.add(label);
        }
        configview.updateUI();
    }
    public static void show_compiler_config(JSONObject compilerconfigs, Object refresh_combobox_sel) {
        JDialog configdialog = new JDialog(Main.idewindow.windowframe,"Compile Configurations",true);
        configdialog.setSize(700,500);
        configdialog.setLayout(new BorderLayout());
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ESCAPE) {
                    configdialog.setVisible(false);
                    configdialog.dispose();
                }
            }
        };
        configdialog.addKeyListener(keyAdapter);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.3);
        configdialog.add(splitPane);

        JPanel configview = new JPanel();
        configview.setLayout(new BoxLayout(configview,BoxLayout.PAGE_AXIS));
        configview.setPreferredSize(new Dimension(200,configdialog.getHeight()));
        splitPane.add(configview,JSplitPane.LEFT);

        JPanel settingspanel = new JPanel();
        settingspanel.setLayout(new BorderLayout());
        JComboBox<String> compilers_combobox = new JComboBox<>();
        for (compiler i : Main.compilerArrayList) {
            compilers_combobox.addItem(i.compiler_name);
        }
        boolean is_compent_enabled[] = {false};
        String selected_label[] = {null};
        compilers_combobox.addItemListener(e12 -> {
            if (is_compent_enabled[0]) {
                ((JSONObject)compilerconfigs.get(selected_label[0])).put("compilername",e12.getItem().toString());
                // shows the compiler settings panel
                for (compiler i : Main.compilerArrayList) {
                    if (i.compiler_name.equalsIgnoreCase(e12.getItem().toString())) {
                        for (Component j : settingspanel.getComponents()) {
                            if (j instanceof JPanel) {
                                settingspanel.remove(j);
                                break;
                            }
                        }
                        settingspanel.add(i);
                        i.updateUI();
                        i.load_settings((JSONObject) ((JSONObject)(compilerconfigs).get(selected_label[0])).get("settings"));
                        SwingUtilities.updateComponentTreeUI(i);
                    }
                }
            } else if (compilers_combobox.getSelectedIndex()==-1){
                for (Component j : settingspanel.getComponents()) {
                    if (j instanceof JPanel) {
                        settingspanel.remove(j);
                        break;
                    }
                }
                settingspanel.updateUI();
            }
        });
        compilers_combobox.setSelectedIndex(-1);
        compilers_combobox.setEnabled(false);
        settingspanel.add(compilers_combobox,BorderLayout.PAGE_START);
        splitPane.add(settingspanel,JSplitPane.RIGHT);

        refresh_config_sel_list(configview,compilerconfigs,compilers_combobox,is_compent_enabled,refresh_combobox_sel,configdialog,selected_label);
        configdialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (is_compent_enabled[0]) {
                    Main.compilerArrayList.get(compilers_combobox.getSelectedIndex()).save_settings((JSONObject) ((JSONObject)compilerconfigs.get(selected_label[0])).get("settings"));
                }
                //if (is_compent_enabled[0]) {
                //    ((JSONObject)((JSONObject)projectjson.get("compileconfigs")).get(selected_label[0])).put("path",script_file_path.getText());
                //    ((JSONObject)((JSONObject)projectjson.get("compileconfigs")).get(selected_label[0])).put("compilerversion",compile_version_selector.getSelectedItem());
                // }
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    JMenuItem createconfig = new JMenuItem("Create compile configuration");
                    createconfig.addActionListener(e1 -> {
                        String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type new compile configuration name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
                        if (output==null) {return;}
                        if (output.replace(" ","").equalsIgnoreCase("")) {
                            JOptionPane.showMessageDialog(Main.idewindow.windowframe,"You can't have a empty name",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        if (compilerconfigs.containsKey(output)) {
                            JOptionPane.showMessageDialog(Main.idewindow.windowframe,"That configuration already exists",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("settings",new JSONObject());
                        jsonObject.put("compilername","");
                        compilerconfigs.put(output,jsonObject);
                        refresh_config_sel_list(configview,compilerconfigs,compilers_combobox,is_compent_enabled,refresh_combobox_sel,configdialog,selected_label);
                        refresh_combobox_sel.toString();
                    });
                    popupMenu.add(createconfig);
                    popupMenu.show(configdialog, configdialog.getMousePosition().x, configdialog.getMousePosition().y);
                }
            }
        };
        configview.addMouseListener(mouseAdapter);
        configdialog.setVisible(true);
    }
}
