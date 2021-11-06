package me.melonsboy.spwn.ide;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import me.melonsboy.spwn.ide.custom.compilers_source;
import me.melonsboy.spwn.ide.themes.Theme;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static me.melonsboy.spwn.ide.compile.*;

public class settings {
    private static HashMap<String,JPanel> panels = new HashMap<>();
    private static ArrayList<String> panels_string_path = new ArrayList<>();
    private static ArrayList<JComponent> smallcomponents = new ArrayList<>();
    private static boolean is_init = false;
    private static String appearanceandbehavior_text = "Appearance and Behavior";
    private static String appearance_text = "Appearance";
    private static String behavior_text = "Behavior";
    private static String compilers_text = "Compilers";
    private static String network_text = "Network";
    private static String plugin_settings_text = "Plugin Settings";

    private static JTextField fontsetting_panel_input;
    private static JCheckBox openproject_on_startup_panel_checkbox;
    private static JComboBox<String> themes_comboBox;
    private static JRadioButton no_proxy_radio_button;
    private static JRadioButton auto_detect_proxy_radio_button;

    private static JTree menutree;

    private static void set_panel(JPanel rightpanel, JPanel panel) {
        rightpanel.removeAll();
        rightpanel.updateUI();
        if (panel==null) {return;}
        rightpanel.add(panel);
        rightpanel.updateUI();
    }
    private static void init_panels(JDialog dialog,JPanel panel1) {
        if (is_init) {return;}
        // plugins
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
            panels.put(plugin_settings_text,panel);
            panels_string_path.add(plugin_settings_text);
        }
        // adds the plugin's settings
        {
            for (Map.Entry<String,pluginloader> i : Main.pluginHashMap.entrySet()) {
                JPanel panel = i.getValue().plugin.get_settings_panel();
                if (panel==null) {continue;}
                panels.put(plugin_settings_text+"/"+i.getValue().name,panel);
                panels_string_path.add(plugin_settings_text+"/"+i.getValue().name);
            }
        }
        // network
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
            JLabel proxylabel = new JLabel("Proxy");
            proxylabel.setFont(new Font(Main.font.getFontName(),Font.BOLD,Main.font.getSize()));
            panel.add(proxylabel);
            {
                ButtonGroup buttonGroup = new ButtonGroup();
                no_proxy_radio_button = new JRadioButton("No Proxy");
                auto_detect_proxy_radio_button = new JRadioButton("Auto Detect System Proxy");
                buttonGroup.add(no_proxy_radio_button);
                buttonGroup.add(auto_detect_proxy_radio_button);
                panel.add(no_proxy_radio_button);
                panel.add(auto_detect_proxy_radio_button);
            }

            panels.put(appearanceandbehavior_text+"/"+network_text,panel);
            panels_string_path.add(appearanceandbehavior_text+"/"+network_text);
        }
        // appearance and behavior
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
            JLabel titlelabel = new JLabel("Appearance and behavior");
            titlelabel.setFont(new Font(Main.font.getFontName(),Font.BOLD,Main.font.getSize()));
            panel.add(titlelabel);
            JLabel descriptionlabel = new JLabel("These settings changes the appearance of the IDE and the behavior of it too!");
            descriptionlabel.setFont(new Font(Main.font.getFontName(),Font.PLAIN,Main.font.getSize()-10));
            panel.add(descriptionlabel);
            smallcomponents.add(descriptionlabel);
            panels.put(appearanceandbehavior_text,panel);
            panels_string_path.add(appearanceandbehavior_text);
        }
        // appearance
        {
            JPanel displaypanel = new JPanel();
            displaypanel.setLayout(null);
            JPanel panel = new JPanel();
            displaypanel.add(panel);
            panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
            {
                JPanel fontsetting_panel = new JPanel();
                fontsetting_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
                JLabel fontsetting_panel_label = new JLabel("Font size");
                fontsetting_panel_label.setPreferredSize(new Dimension(190,Main.font.getSize()));
                fontsetting_panel_input = new JTextField(Main.configjson.get("fontsize").toString()) {
                    /**
                     * code below is copied from <a href="https://stackoverflow.com/a/11609982">https://stackoverflow.com/a/11609982</a>
                     */
                    public void processKeyEvent(KeyEvent ev) {
                        char c = ev.getKeyChar();
                        try {
                            // Ignore all non-printable characters. Just check the printable ones.
                            if (c > 31 && c < 127) {
                                Integer.parseInt(c + "");
                            }
                            super.processKeyEvent(ev);
                        }
                        catch (NumberFormatException nfe) {
                            // Do nothing. Character inputted is not a number, so ignore it.
                        }
                    }
                };
                fontsetting_panel_input.setPreferredSize(new Dimension(200,Main.font.getSize()+10));
                fontsetting_panel.add(fontsetting_panel_label);
                fontsetting_panel.add(fontsetting_panel_input);
                //fontsetting_panel.setPreferredSize(new Dimension(600,Main.font.getSize()+10));
                panel.add(fontsetting_panel);

                JPanel theme_setting_panel = new JPanel();
                JLabel theme_setting_panel_label = new JLabel("Theme");
                theme_setting_panel.add(theme_setting_panel_label);
                theme_setting_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
                theme_setting_panel.setPreferredSize(new Dimension(200,Main.font.getSize()+10));
                themes_comboBox = new JComboBox<>();
                DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
                for (Theme i : theme_loader.list_themes()) {
                    comboBoxModel.addElement(i.get_theme_name());
                }
                comboBoxModel.setSelectedItem(theme_loader.selected_theme);
                themes_comboBox.addItemListener(e -> {
                    try {
                        theme_loader.show_theme(theme_loader.arrayList.get(themes_comboBox.getSelectedIndex()));
                        Main.font = new Font(new JLabel().getFont().getFontName(),Font.BOLD,Integer.parseInt(Main.configjson.get("fontsize").toString()));
                        setfont.setfont(Main.font,Main.idewindow.windowframe);
                        setfont.setfont(Main.font,dialog.getAccessibleContext());
                        for (Map.Entry<String,JPanel> entry : panels.entrySet()) {
                            setfont.setfont(Main.font,entry.getValue());
                            SwingUtilities.updateComponentTreeUI(entry.getValue());
                        }
                        refresh_components(panel1, dialog,menutree.getSelectionPath());
                    } catch (UnsupportedLookAndFeelException | IOException ex) {
                        ex.printStackTrace();
                    }
                });
                themes_comboBox.setModel(comboBoxModel);
                theme_setting_panel.add(themes_comboBox);

                panel.add(theme_setting_panel);
            }

            panel.setBounds(0,0,600,(Main.font.getSize()+10)*(panel.getComponents().length+1));

            panels.put(appearanceandbehavior_text+"/"+appearance_text,displaypanel);
            panels_string_path.add(appearanceandbehavior_text+"/"+appearance_text);
        }

        // behavior
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
            openproject_on_startup_panel_checkbox = new JCheckBox("Open last opened project on startup");
            //openproject_on_startup_panel_checkbox.setPreferredSize(new Dimension(settingspanel.getPreferredSize().width-10,Main.font.getSize()));
            panel.add(openproject_on_startup_panel_checkbox);
            panels.put(appearanceandbehavior_text+"/"+behavior_text,panel);
            panels_string_path.add(appearanceandbehavior_text+"/"+behavior_text);
        }
        // compilers
        {
            JPanel panel = new JPanel();
            JPanel listpanel = new JPanel();
            panel.setLayout(new BorderLayout());

            JButton download_compiler_button = new JButton("Download a compiler");
            download_compiler_button.addActionListener(e -> {
                {
                    JPanel choose_compilers_source = new JPanel();
                    choose_compilers_source.setLayout(new BorderLayout());
                    choose_compilers_source.setSize(400, Main.font.getSize() * 4);
                    JLabel choose_compilers_source_label = new JLabel("Please choose the compilers source");
                    choose_compilers_source.add(choose_compilers_source_label,BorderLayout.PAGE_START);
                    JComboBox<String> choose_compilers_source_combobox = new JComboBox<>();
                    ArrayList<compilers_source> compilers_sourceArrayList = new ArrayList<>();
                    for (Map.Entry<compilers_source,Plugin> i : Main.compilersSourcePluginHashMap.entrySet()) {
                        compilers_sourceArrayList.add(i.getKey());
                    }
                    for (compilers_source i : compilers_sourceArrayList) {
                        choose_compilers_source_combobox.addItem(i.get_name());
                    }
                    choose_compilers_source.add(choose_compilers_source_combobox,BorderLayout.CENTER);
                    {
                        String[] strings = new String[] {"Download list","Cancel"};
                        int output = JOptionPane.showOptionDialog(Main.idewindow.windowframe,choose_compilers_source,"Choose compilers source",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,strings,strings[0]);
                        if (output==JOptionPane.CLOSED_OPTION) {return;}
                        if (strings[output].equalsIgnoreCase(strings[1])) {return;}
                        if (strings[output].equalsIgnoreCase(strings[0])) {
                            compilers_source compilersSource = compilers_sourceArrayList.get(choose_compilers_source_combobox.getSelectedIndex());
                            JDialog downloading_content_dialog = new JDialog(Main.idewindow.windowframe,"Downloading compilers list",true);
                            downloading_content_dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                            downloading_content_dialog.setResizable(false);
                            downloading_content_dialog.setSize(400,Main.font.getSize()*5);
                            JLabel downloading_content_dialog_label = new JLabel("Please wait",JLabel.CENTER);
                            downloading_content_dialog.add(downloading_content_dialog_label);
                            AtomicBoolean download_completed = new AtomicBoolean(false);
                            DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
                            AtomicReference<JSONObject> jsonObject = new AtomicReference<>();

                            new Thread(() -> {
                                try {
                                    ArrayList<String> arrayList = compilersSource.get_list();
                                    for (String i : arrayList) {
                                        comboBoxModel.addElement(i);
                                    }
                                    download_completed.set(true);
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(downloading_content_dialog,"A error has occurred","Error",JOptionPane.ERROR_MESSAGE);
                                    downloading_content_dialog.setVisible(false);
                                    ex.printStackTrace();
                                }
                                downloading_content_dialog.setVisible(false);
                            }).start();
                            downloading_content_dialog.setVisible(true);
                            if (!download_completed.get()) {
                                return;
                            }
                            downloading_content_dialog.removeAll();
                            {
                                JPanel choosecompiler_panel = new JPanel();
                                choosecompiler_panel.setLayout(new BorderLayout());
                                JLabel label = new JLabel("Please choose a compiler");
                                choosecompiler_panel.add(label,BorderLayout.PAGE_START);
                                JComboBox<String> stringJComboBox = new JComboBox<>();
                                stringJComboBox.setModel(comboBoxModel);
                                choosecompiler_panel.add(stringJComboBox,BorderLayout.PAGE_END);
                                String[] strings1 = new String[] {"Download Compiler","Cancel"};
                                int output1 = JOptionPane.showOptionDialog(Main.idewindow.windowframe,choosecompiler_panel,"Choose a compiler",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,strings1,strings1[0]);
                                if (output1==0) {
                                    compilersSource.download_compiler(stringJComboBox.getSelectedIndex());
                                    refresh_compiler_list(listpanel,dialog);
                                    try {
                                        save_compilers_list();
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
                /*
                JDialog downloading_content_dialog = new JDialog(Main.idewindow.windowframe,"Downloading compilers list",true);
                downloading_content_dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                downloading_content_dialog.setResizable(false);
                downloading_content_dialog.setSize(400,Main.font.getSize()*5);
                JLabel downloading_content_dialog_label = new JLabel("Please wait",JLabel.CENTER);
                downloading_content_dialog.add(downloading_content_dialog_label);
                AtomicBoolean download_completed = new AtomicBoolean(false);
                DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
                AtomicReference<JSONObject> jsonObject = new AtomicReference<>();
                new Thread(() -> {
                    try {
                        jsonObject.set(download_compilers_list());
                        ArrayList<String> arrayList = new ArrayList<>();
                        for (Object i : jsonObject.get().keySet()) {
                            JSONObject jsonObject1 = (JSONObject) jsonObject.get().get(i);
                            if (util.can_download((JSONObject) jsonObject1.get("path"))) {
                                arrayList.add(i.toString());
                            }
                        }
                        Collections.sort(arrayList);
                        for (String i : arrayList) {
                            comboBoxModel.addElement(i);
                        }
                        download_completed.set(true);
                    } catch (IOException | ParseException ex) {
                        JOptionPane.showMessageDialog(downloading_content_dialog,"Could not connect to github","Error",JOptionPane.ERROR_MESSAGE);
                        downloading_content_dialog.setVisible(false);
                        ex.printStackTrace();
                    }
                    downloading_content_dialog.setVisible(false);
                }).start();
                downloading_content_dialog.setVisible(true);
                if (!download_completed.get()) {
                    return;
                }
                JDialog dialog1 = new JDialog(Main.idewindow.windowframe,"Download a compiler",true);
                dialog1.setSize(700,500);
                dialog1.setLayout(new BorderLayout());

                JPanel middlepanel = new JPanel();

                int label_width = 250;

                JPanel namepanel = new JPanel();
                namepanel.setPreferredSize(new Dimension(dialog1.getWidth(),Main.font.getSize()+15));
                JLabel namepanel_label = new JLabel("Name");
                JTextField namepanel_input = new JTextField();
                final boolean[] is_edited = {false};
                namepanel_input.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        is_edited[0]=true;
                    }
                });
                namepanel_label.setPreferredSize(new Dimension(label_width,Main.font.getSize()+10));
                namepanel_input.setPreferredSize(new Dimension(dialog1.getWidth()-label_width-30,Main.font.getSize()+10));
                namepanel.add(namepanel_label);
                namepanel.add(namepanel_input);
                middlepanel.add(namepanel);

                JPanel locationpanel = new JPanel();
                locationpanel.setPreferredSize(new Dimension(dialog1.getWidth(),Main.font.getSize()+15));
                JLabel locationpanel_label = new JLabel("Folder path");
                JTextField locationpanel_input = new JTextField();
                locationpanel_input.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        is_edited[0] =true;
                    }
                });
                locationpanel_label.setPreferredSize(new Dimension(label_width,Main.font.getSize()+10));
                locationpanel_input.setPreferredSize(new Dimension(dialog1.getWidth()-label_width-30,Main.font.getSize()+10));
                locationpanel.add(locationpanel_label);
                locationpanel.add(locationpanel_input);
                middlepanel.add(locationpanel);

                JPanel versionpanel = new JPanel();
                versionpanel.setPreferredSize(new Dimension(dialog1.getWidth(),Main.font.getSize()+15));
                JLabel versionpanel_label = new JLabel("Compiler version");
                JComboBox<String> versionpanel_input = new JComboBox<>();
                versionpanel_input.addItemListener(e12 -> {
                    if (!is_edited[0]) {
                        locationpanel_input.setText(Main.mainfolder_path + "/compilers/" + e12.getItem().toString());
                        namepanel_input.setText(e12.getItem().toString());
                    }
                });
                versionpanel_input.setModel(comboBoxModel);
                locationpanel_input.setText(Main.mainfolder_path + "/compilers/" + versionpanel_input.getItemAt(versionpanel_input.getSelectedIndex()));
                namepanel_input.setText(versionpanel_input.getItemAt(versionpanel_input.getSelectedIndex()));
                versionpanel_label.setPreferredSize(new Dimension(label_width,Main.font.getSize()+10));
                versionpanel_input.setPreferredSize(new Dimension(dialog1.getWidth()-label_width-30,Main.font.getSize()+10));
                versionpanel.add(versionpanel_label);
                versionpanel.add(versionpanel_input);
                middlepanel.add(versionpanel);

                JButton choosepathbutton = new JButton("Choose install folder");
                choosepathbutton.setPreferredSize(new Dimension(dialog1.getWidth()-20,Main.font.getSize()+15));
                choosepathbutton.addActionListener(e1 -> {
                    String output = choose_compiler_path(dialog1,JFileChooser.DIRECTORIES_ONLY);
                    if (output==null) {
                        return;
                    }
                    locationpanel_input.setText(output);
                    is_edited[0]=true;
                });
                middlepanel.add(choosepathbutton);

                dialog1.add(middlepanel);

                JPanel buttonspanel = new JPanel();
                JButton installbutton = new JButton("Download");
                JButton cancelbutton = new JButton("Cancel");
                installbutton.addActionListener(e1 -> {
                    dialog1.setVisible(false);
                    downloading_content_dialog_label.setText("Please wait");
                    Thread thread = new Thread(() -> {
                        downloading_content_dialog_label.setText("Downloading "+versionpanel_input.getItemAt(versionpanel_input.getSelectedIndex())+" Compiler, 0% complete");
                        downloading_content_dialog_label.updateUI();
                        // sets the download link and the main file
                        String download_url = null;
                        String mainfile = null;
                        String installfolder = locationpanel_input.getText();
                        JSONObject downloadjsonobject = (JSONObject) jsonObject.get().get(versionpanel_input.getItemAt(versionpanel_input.getSelectedIndex()));
                        if (PlatformUtil.isWindows()) {
                            download_url = ((JSONObject)downloadjsonobject.get("path")).get("windows").toString();
                            mainfile = ((JSONObject)downloadjsonobject.get("mainfile")).get("windows").toString();
                        } else if (PlatformUtil.isLinux()) {
                            download_url = ((JSONObject)downloadjsonobject.get("path")).get("linux").toString();
                            mainfile = ((JSONObject)downloadjsonobject.get("mainfile")).get("linux").toString();
                        } else if (PlatformUtil.isMac()) {
                            download_url = ((JSONObject)downloadjsonobject.get("path")).get("mac").toString();
                            mainfile = ((JSONObject)downloadjsonobject.get("mainfile")).get("mac").toString();
                        }
                        // downloads the file into memory
                        AtomicInteger atomicInteger = new AtomicInteger();
                        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
                        new Thread(() -> {
                            while (atomicBoolean.get()) {
                                downloading_content_dialog_label.setText("Downloading "+versionpanel_input.getItemAt(versionpanel_input.getSelectedIndex())+" Compiler, "+atomicInteger.get()+"% complete");
                                downloading_content_dialog_label.updateUI();
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }).start();
                        try {
                            if (new File(Main.mainfolder_path+"/downloaded_compilers/"+downloadjsonobject.get("foldername")).exists()) {
                                atomicBoolean.set(false);
                            } else {
                                compile.download_file(github_repo_url+"/"+download_url,Main.mainfolder_path+"/downloaded_compilers/"+downloadjsonobject.get("foldername"),atomicInteger);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        atomicBoolean.set(false);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        downloading_content_dialog_label.setText("Installing compliler..");
                        downloading_content_dialog_label.updateUI();
                        ZipFile zipFile = new ZipFile(Main.mainfolder_path+"/downloaded_compilers/"+downloadjsonobject.get("foldername"));
                        try {
                            zipFile.extractAll(installfolder);
                        } catch (ZipException ex) {
                            ex.printStackTrace();
                        }
                        JSONObject _jsonObject = new JSONObject();
                        _jsonObject.put("path",installfolder+"/"+mainfile);
                        compilerslist.put(namepanel_input.getText(),_jsonObject);
                        refresh_compiler_list(listpanel,dialog1);
                        try {
                            save_compilers_list();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        downloading_content_dialog.setVisible(false);
                    });
                    thread.start();
                    downloading_content_dialog.setSize(new Dimension(800,downloading_content_dialog.getHeight()));
                    downloading_content_dialog.setVisible(true);
                });
                cancelbutton.addActionListener(e1 -> {
                    dialog1.setVisible(false);
                });
                buttonspanel.add(installbutton);
                buttonspanel.add(cancelbutton);
                dialog1.add(buttonspanel, BorderLayout.PAGE_END);

                dialog1.setVisible(true);
                */
            });
            panel.add(download_compiler_button,BorderLayout.PAGE_END);

            listpanel.setLayout(new BoxLayout(listpanel,BoxLayout.PAGE_AXIS));
            refresh_compiler_list(listpanel,dialog);
            listpanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem new_compiler_config = new JMenuItem("New compiler configuration");
                        new_compiler_config.addActionListener(e1 -> {
                            JDialog dialog1 = new JDialog(Main.idewindow.windowframe,"New compiler configuration",true);
                            dialog1.setSize(700,500);
                            dialog1.setLayout(new BorderLayout());

                            JPanel middlepanel = new JPanel();
                            JPanel namepanel = new JPanel();
                            JLabel namepanel_label = new JLabel("Name");
                            namepanel_label.setPreferredSize(new Dimension(200, Main.font.getSize() + 10));
                            namepanel.add(namepanel_label);
                            JTextField namepanel_input = new JTextField();
                            namepanel_input.setPreferredSize(new Dimension(dialog1.getWidth() - namepanel_label.getPreferredSize().width - 40, Main.font.getSize() + 10));
                            namepanel.add(namepanel_input);
                            middlepanel.add(namepanel);

                            JPanel pathpanel = new JPanel();
                            JLabel pathpanel_label = new JLabel("Compiler path");
                            pathpanel_label.setPreferredSize(new Dimension(200, Main.font.getSize() + 10));
                            pathpanel.add(pathpanel_label);
                            JTextField pathpanel_input = new JTextField();
                            pathpanel_input.setPreferredSize(new Dimension(dialog1.getWidth() - pathpanel_label.getPreferredSize().width - 40, Main.font.getSize() + 10));
                            pathpanel.add(pathpanel_input);
                            middlepanel.add(pathpanel);

                            JButton choose_compiler_path = new JButton("Choose compiler path");
                            choose_compiler_path.setPreferredSize(new Dimension(dialog1.getWidth() - 20, Main.font.getSize() + 15));
                            choose_compiler_path.addActionListener(e2 -> {
                                choose_compiler_path(dialog1,JFileChooser.FILES_ONLY);
                            });
                            middlepanel.add(choose_compiler_path);

                            JButton create_button = new JButton("Create configuration");
                            create_button.addActionListener(e2 -> {
                                if (compilerslist.containsKey(namepanel_input.getText())) {
                                    JOptionPane.showMessageDialog(dialog1,"That configuration already exists!","Error",JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("path",pathpanel_input.getText());
                                compilerslist.put(namepanel_input.getText(),jsonObject);
                                dialog1.setVisible(false);
                                refresh_compiler_list(listpanel,dialog);
                                try {
                                    save_compilers_list();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                            dialog1.add(create_button, BorderLayout.PAGE_END);

                            dialog1.add(middlepanel);
                            dialog1.setResizable(false);

                            dialog1.setVisible(true);
                        });
                        popupMenu.add(new_compiler_config);
                        popupMenu.show(listpanel,listpanel.getMousePosition().x,listpanel.getMousePosition().y);
                    }
                }
            });
            final int[] count = {0};
            class setlabelpos {
                public setlabelpos() {
                    count[0] =0;
                    for (Component i : listpanel.getComponents()) {
                        i.setBounds(5,(count[0] *(Main.font.getSize()+10)),dialog.getWidth()-30,Main.font.getSize()+10);
                        count[0]++;
                    }
                }
            }
            new setlabelpos();
            dialog.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    new setlabelpos();
                }
            });
            panel.add(listpanel);

            panels.put(compilers_text,panel);
            panels_string_path.add(compilers_text);
        }
        is_init=true;
    }
    private static void list_memory_files(String filepath, FileSystem fileSystem, HashMap<DefaultMutableTreeNode, String> treeNodeHashMap) throws IOException {
        Files.list(fileSystem.getPath(filepath)).forEachOrdered(path -> {
            DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(path.getFileName());
            treeNodeHashMap.put(defaultMutableTreeNode,path.getParent().toString());
            if (Files.isDirectory(path)) {
                try {
                    list_memory_files(path.toString(),fileSystem, treeNodeHashMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private static DefaultMutableTreeNode get_parent(String parent_name,HashMap<DefaultMutableTreeNode,String> treeNodeHashMap) {
        for (Map.Entry<DefaultMutableTreeNode,String> i : treeNodeHashMap.entrySet()) {
            if (i.getKey().toString().equalsIgnoreCase(parent_name)) {
                return i.getKey();
            }
        }
        return null;
    }
    private static void get_tree(JTree tree,JPanel right_panel) throws IOException {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode();
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Icon getLeafIcon() {
                return null;
            }

            @Override
            public Icon getClosedIcon() {
                return null;
            }

            @Override
            public Icon getOpenIcon() {
                return null;
            }
        });
        FileSystem jimfs_filesys = Jimfs.newFileSystem(Configuration.unix().toBuilder().setWorkingDirectory("/").build());
        for (String i : panels_string_path) {
            Path path = jimfs_filesys.getPath(i);
            Files.createDirectories(path);
        }
        HashMap<DefaultMutableTreeNode,String> treeNodeHashMap = new HashMap<>();
        Files.list(jimfs_filesys.getPath("")).forEachOrdered(filepath -> {
            try {
                DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(filepath.getFileName());
                treeNodeHashMap.put(defaultMutableTreeNode,"");
                list_memory_files(filepath.toString(),jimfs_filesys,treeNodeHashMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        for (Map.Entry<DefaultMutableTreeNode,String> i : treeNodeHashMap.entrySet()) {
            DefaultMutableTreeNode defaultMutableTreeNode = get_parent(i.getValue(),treeNodeHashMap);
            Objects.requireNonNullElse(defaultMutableTreeNode, treeNode).add(i.getKey());
        }
        /*
        // Appearance and behavior
        DefaultMutableTreeNode appearanceAndBehaviornode = new DefaultMutableTreeNode(appearanceandbehavior_text);
        treeNode.add(appearanceAndBehaviornode);

        // network
        DefaultMutableTreeNode networknode = new DefaultMutableTreeNode(network_text);
        appearanceAndBehaviornode.add(networknode);

        // appearance
        DefaultMutableTreeNode appearancenode = new DefaultMutableTreeNode(appearance_text);
        appearanceAndBehaviornode.add(appearancenode);

        // behavior
        DefaultMutableTreeNode behaviornode = new DefaultMutableTreeNode(behavior_text);
        appearanceAndBehaviornode.add(behaviornode);

        // compilers
        DefaultMutableTreeNode compilersnode = new DefaultMutableTreeNode(compilers_text);
        treeNode.add(compilersnode);
         */

        DefaultTreeModel defaultTreeModel = new DefaultTreeModel(treeNode);
        tree.addTreeSelectionListener(e -> {
            set_panel(right_panel,panels.get(util.to_string_path(e.getPath().getPath())));
        });
        tree.setModel(defaultTreeModel);
    }
    private static void apply_stuff(JPanel settingsdialog) throws UnsupportedLookAndFeelException, IOException {
        theme_loader.apply_theme();
        Main.font = new Font(new JLabel().getFont().getFontName(),Font.BOLD,Integer.parseInt(Main.configjson.get("fontsize").toString()));
        setfont.setfont(Main.font,Main.idewindow.windowframe);
        setfont.setfont(Main.font,settingsdialog.getAccessibleContext());
        for (Map.Entry<String,JPanel> entry : panels.entrySet()) {
            setfont.setfont(Main.font,entry.getValue());
            SwingUtilities.updateComponentTreeUI(entry.getValue());
        }
        SwingUtilities.updateComponentTreeUI(settingsdialog);
    }
    private static void refresh_components(JPanel settingsdialog, JDialog jDialog,TreePath treePath) throws IOException {
        settingsdialog.removeAll();
        settingsdialog.setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane();
        splitPane.setContinuousLayout(true);
        splitPane.setResizeWeight(0.2);
        menutree = new JTree();
        splitPane.add(new JScrollPane(menutree),JSplitPane.LEFT);
        JPanel rightpanel = new JPanel();
        rightpanel.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(rightpanel);
        splitPane.add(scrollPane,JSplitPane.RIGHT);
        JPanel buttonspanel = new JPanel();
        JButton savechangesbutton = new JButton("Save changes");
        JButton cancelbutton = new JButton("Cancel");
        JButton saveclosedialogbutton = new JButton("Save and close dialog");
        savechangesbutton.addActionListener(e -> {
            Main.configjson.put("fontsize",Integer.parseInt(fontsetting_panel_input.getText()));
            Main.configjson.put("open_project_on_startup",openproject_on_startup_panel_checkbox.isSelected());
            theme_loader.keep_theme();
            Main.configjson.put("theme_name",theme_loader.selected_theme.get_theme_name());
            if (no_proxy_radio_button.isSelected()) {
                Main.configjson.put("proxy_detection_mode",0);
            } else if (auto_detect_proxy_radio_button.isSelected()) {
                Main.configjson.put("proxy_detection_mode",1);
            }
            try {
                Main.save_config();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                apply_stuff(settingsdialog);
            } catch (UnsupportedLookAndFeelException | IOException ex) {
                ex.printStackTrace();
            }
        });
        cancelbutton.addActionListener(e -> {
            jDialog.setVisible(false);
        });
        saveclosedialogbutton.addActionListener(e -> {
            try {
                savechangesbutton.doClick();
                refresh_components(settingsdialog, jDialog,null);
                jDialog.setVisible(false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        buttonspanel.add(savechangesbutton);
        buttonspanel.add(cancelbutton);
        buttonspanel.add(saveclosedialogbutton);
        settingsdialog.add(buttonspanel,BorderLayout.PAGE_END);

        settingsdialog.add(splitPane);

        get_tree(menutree,rightpanel);
        if (!(treePath==null)) {
            menutree.setSelectionPath(treePath);
        }

        for (JComponent i : smallcomponents) {
            i.setFont(new Font(Main.font.getFontName(),Font.PLAIN,Main.font.getSize()-10));
        }
    }
    public static void settings() throws IOException, UnsupportedLookAndFeelException {
        JDialog settingsdialog = new JDialog(Main.idewindow.windowframe,"Settings",true);
        settingsdialog.setSize(new Dimension(1500,900));
        JPanel panel = new JPanel();
        init_panels(settingsdialog,panel);
        settingsdialog.setLayout(new BorderLayout());
        settingsdialog.add(panel);
        refresh_components(panel,settingsdialog,null);
        openproject_on_startup_panel_checkbox.setSelected((Boolean) Main.configjson.get("open_project_on_startup"));
        fontsetting_panel_input.setText(Main.configjson.get("fontsize").toString());
        themes_comboBox.setSelectedItem(Main.configjson.get("theme_name").toString());
        if (Integer.parseInt(Main.configjson.get("proxy_detection_mode").toString())==0) {
            no_proxy_radio_button.setSelected(true);
        } else if (Integer.parseInt(Main.configjson.get("proxy_detection_mode").toString())==1) {
            auto_detect_proxy_radio_button.setSelected(true);
        }

        settingsdialog.setVisible(true);
        apply_stuff(panel);
    }

    public static void settings_old() throws IOException {
        JPanel settingspanel = new JPanel();
        settingspanel.setPreferredSize(new Dimension(800,400));

        JPanel fontsetting_panel = new JPanel();
        JLabel fontsetting_panel_label = new JLabel("Font size");
        fontsetting_panel_label.setPreferredSize(new Dimension(190,Main.font.getSize()));
        JTextField fontsetting_panel_input = new JTextField(Main.configjson.get("fontsize").toString()) {
            /**
             * code below is copied from <a href="https://stackoverflow.com/a/11609982">https://stackoverflow.com/a/11609982</a>
             */
            public void processKeyEvent(KeyEvent ev) {
                char c = ev.getKeyChar();
                try {
                    // Ignore all non-printable characters. Just check the printable ones.
                    if (c > 31 && c < 127) {
                        Integer.parseInt(c + "");
                    }
                    super.processKeyEvent(ev);
                }
                catch (NumberFormatException nfe) {
                    // Do nothing. Character inputted is not a number, so ignore it.
                }
            }
        };
        fontsetting_panel_input.setPreferredSize(new Dimension(settingspanel.getPreferredSize().width-200,Main.font.getSize()+10));
        fontsetting_panel.add(fontsetting_panel_label);
        fontsetting_panel.add(fontsetting_panel_input);
        fontsetting_panel.setBounds(0,(Main.font.getSize()+10)*2,settingspanel.getWidth(),Main.font.getSize()+15);
        settingspanel.add(fontsetting_panel);

        JPanel openproject_on_startup_panel = new JPanel();
        JCheckBox openproject_on_startup_panel_checkbox = new JCheckBox("Open last opened project on startup");
        openproject_on_startup_panel_checkbox.setPreferredSize(new Dimension(settingspanel.getPreferredSize().width-10,Main.font.getSize()));
        openproject_on_startup_panel.setBounds(0,(Main.font.getSize()+10)*3,settingspanel.getPreferredSize().width,Main.font.getSize()+15);
        openproject_on_startup_panel.add(openproject_on_startup_panel_checkbox);
        settingspanel.add(openproject_on_startup_panel);

        JPanel buttonspanel = new JPanel();
        JButton open_compiler_list = new JButton("Open compiler list");
        open_compiler_list.addActionListener(e -> {
            try {
                compile.show_compiler_list();
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        });
        buttonspanel.add(open_compiler_list);
        settingspanel.add(buttonspanel);

        openproject_on_startup_panel_checkbox.setSelected((Boolean) Main.configjson.get("open_project_on_startup"));
        String[] options = new String[] {"Ok"};
        JOptionPane.showOptionDialog(Main.idewindow.windowframe,settingspanel,Main.idewindow.windowframe.getTitle(),JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
        Main.configjson.put("fontsize",Integer.parseInt(fontsetting_panel_input.getText()));
        Main.configjson.put("open_project_on_startup",openproject_on_startup_panel_checkbox.isSelected());
        Main.save_config();
        Main.font = new Font(new JLabel().getFont().getFontName(),Font.BOLD,Integer.parseInt(Main.configjson.get("fontsize").toString()));
        setfont.setfont(Main.font,Main.idewindow.windowframe);
    }
}
