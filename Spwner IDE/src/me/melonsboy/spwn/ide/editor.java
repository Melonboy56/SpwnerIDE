package me.melonsboy.spwn.ide;

import me.melonsboy.spwn.ide.custom.compiler;
import me.melonsboy.spwn.ide.custom.menuitem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class editor extends JPanel {
    private projectfileview fileview;
    public file_editor textEditor;
    private boolean is_init;
    public String folder_path;
    public JSplitPane middlepanel;
    public JSplitPane bottompane;
    public JSONObject projectjson;
    public JTabbedPane bottomtabbedpane;
    public JComboBox<String> compileconfigs;
    private HashMap<String,JMenu> menuHashMap = new HashMap<>();

    public void update_title() {
        try {
            Main.idewindow.windowframe.setTitle(projectjson.get("projectname")+" ["+folder_path+"]"+" - "+new File(textEditor.filetabs.getTitleAt(textEditor.filetabs.getSelectedIndex())).getName());
        } catch (IndexOutOfBoundsException exception) {
            exception.printStackTrace();
        }
    }

    private void show_changelog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("assets/changelog.txt")));
        String text = bufferedReader.lines().collect(Collectors.joining("\n"));
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        panel.add(new JScrollPane(textArea));
        JPanel settingspanel = new JPanel();
        panel.add(settingspanel,BorderLayout.PAGE_END);
        JCheckBox dontshownexttimecheckbox = new JCheckBox("Don't open this tab");
        dontshownexttimecheckbox.setSelected(!(Boolean) Main.configjson.get("open_changelog_message"));
        settingspanel.add(dontshownexttimecheckbox);
        JButton closechangelogbutton = new JButton("Close tab");
        settingspanel.add(closechangelogbutton);
        closechangelogbutton.addActionListener(e -> {
            try {
                Main.configjson.put("open_changelog_message",!dontshownexttimecheckbox.isSelected());
                textEditor.file_deleted("<changelog>");
                Main.save_config();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        textEditor.add_memory_tab("<changelog>","Spwner IDE alpha v1.1 changelog",panel);
    }

    @Override
    public void setVisible(boolean aFlag) {
        if (!aFlag) {
            try {
                textEditor.save_all();
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

    public editor(JSONObject projectjson, String folderpath) throws IOException, ParseException {
        super();
        this.projectjson = projectjson;
        this.setLayout(new BorderLayout());
        bottompane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottompane.setResizeWeight(0.8);
        bottompane.setContinuousLayout(true);
        middlepanel = new JSplitPane();
        middlepanel.setContinuousLayout(true);
        folder_path = folderpath;
        textEditor = new file_editor(folderpath,this,false);
        for (int i = 0; i < ((JSONArray)projectjson.get("opened_files")).size(); i++) {
            String text = ((JSONArray)projectjson.get("opened_files")).get(i).toString();
            if (new File(folder_path+"/"+text).exists()) {
                textEditor.add_tab(folder_path + "/" + text);
            } else {
                ((JSONArray)projectjson.get("opened_files")).remove(i);
            }
        }
        if ((Boolean)Main.configjson.get("open_changelog_message")) {
            show_changelog();
        }
        fileview = new projectfileview(folder_path,textEditor);
        middlepanel.add(fileview,JSplitPane.LEFT);
        middlepanel.add(textEditor,JSplitPane.RIGHT);
        bottompane.add(middlepanel,JSplitPane.TOP);

        bottomtabbedpane = new JTabbedPane();
        bottomtabbedpane.setTabPlacement(JTabbedPane.BOTTOM);
        bottomtabbedpane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        bottompane.add(bottomtabbedpane,JSplitPane.BOTTOM);
        for (Map.Entry<String,JPanel> entry : Main.bottompanel_tabid_panels.entrySet()) {
            if (!Main.always_open_tabs.contains(entry.getValue())) {
                Main.bottompanel_tabid_panels.remove(entry.getKey());
            }
        }
        for (Map.Entry<String, JPanel> i : Main.bottompanel_tabid_panels.entrySet()) {
            bottomtabbedpane.addTab(Main.bottompanel_tabid_names.get(i.getKey()),Main.bottompanel_tabid_icons.get(i.getKey()),i.getValue());
        }

        JPanel compilepanel = new JPanel();
        compilepanel.setLayout(new BorderLayout());
        JPanel compile_log_panel = new JPanel();
        JScrollPane compile_scrollpane = new JScrollPane(compile_log_panel);
        compilepanel.add(compile_scrollpane,BorderLayout.CENTER);
        compilepanel.setPreferredSize(new Dimension(this.getWidth(),150));
        //bottompane.add(compilepanel,JSplitPane.BOTTOM);
        middlepanel.setDividerLocation(Integer.parseInt(Main.configjson.get("middlesplit").toString()));
        bottompane.setDividerLocation(Integer.parseInt(Main.configjson.get("bottomsplit").toString()));
        JPanel toppanel = new JPanel();
        JPanel toppanel_right = new JPanel();
        JPanel toppanel_left = new JPanel();
        toppanel.setPreferredSize(new Dimension(this.getWidth(),Main.font.getSize()+20));
        this.add(toppanel,BorderLayout.PAGE_START);
        toppanel.setLayout(new BorderLayout());
        toppanel.add(toppanel_right,BorderLayout.EAST);
        toppanel.add(toppanel_left,BorderLayout.WEST);
        toppanel_right.setLayout(new FlowLayout(FlowLayout.RIGHT));
        //((FlowLayout)toppanel.getLayout()).setVgap(0);

        compileconfigs = new JComboBox() {

            private void fix_combobox() {
                this.setPreferredSize(new Dimension(util.get_longest_string_width(this)+60,Main.font.getSize()+10));
            }

            @Override
            public void updateUI() {
                fix_combobox();
                super.updateUI();
            }

            @Override
            public void paint(Graphics g) {
                fix_combobox();
                super.paint(g);
            }
        };
        compileconfigs.setToolTipText("Select Compile Configuration");

        compileconfigs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!(compileconfigs.getPreferredSize().height==Main.font.getSize()+10)) {
                    compileconfigs.setPreferredSize(new Dimension(util.get_longest_string_width(compileconfigs)+60,Main.font.getSize()+10));
                    compileconfigs.updateUI();
                }
            }
        });
        //compileconfigs.setPreferredSize(new Dimension(150,Main.font.getSize()+10));
        toppanel_right.add(compileconfigs);

        class refresh_combobox_sel {
            @Override
            public String toString() {
                function1();
                return super.toString();
            }

            public void function1() {
                compileconfigs.removeAllItems();
                if (!projectjson.containsKey("compileconfigs")) {
                    projectjson.put("compileconfigs",new JSONObject());
                }
                for (Object i : ((JSONObject)projectjson.get("compileconfigs")).entrySet()) {
                    compileconfigs.addItem(((Map.Entry)i).getKey().toString());
                }
                compileconfigs.updateUI();
            }
        }
        new refresh_combobox_sel().function1();
        if (!(projectjson.get("selected_compile_config")==null)) {
            compileconfigs.setSelectedItem(projectjson.get("selected_compile_config").toString());
        }
        compileconfigs.addItemListener(e -> {
            projectjson.put("selected_compile_config",e.getItem());
        });
        JLabel editcompileconfigslabel = new JLabel();
        editcompileconfigslabel.setToolTipText("Edit Compile configurations");
        editcompileconfigslabel.setIcon(util.scaleImage(new ImageIcon(Main.class.getResource("assets/settings.png")),32,32));
        editcompileconfigslabel.setOpaque(true);
        editcompileconfigslabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                util.show_compiler_config(((JSONObject)projectjson.get("compileconfigs")),new refresh_combobox_sel());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                e.getComponent().setBackground(e.getComponent().getBackground().darker());
                e.getComponent().repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setBackground(null);
                e.getComponent().repaint();
            }
        });
        toppanel_right.add(editcompileconfigslabel);
        JSeparator jSeparator = new JSeparator(JSeparator.VERTICAL);
        jSeparator.setPreferredSize(new Dimension(jSeparator.getPreferredSize().width,toppanel.getPreferredSize().height));
        toppanel_right.add(jSeparator);
        for (me.melonsboy.spwn.ide.custom.iconButton i : Main.customiconbuttons) {
            JLabel label = new JLabel();
            i.put_label(label);
            label.setIcon(util.scaleImage(i.get_icon(),32,32));
            label.setOpaque(true);
            label.setToolTipText(i.get_tooltip());
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    i.button_clicked(SwingUtilities.isRightMouseButton(e));
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    e.getComponent().setBackground(e.getComponent().getBackground().darker());
                    e.getComponent().repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    e.getComponent().setBackground(null);
                    e.getComponent().repaint();
                }
            });
            if (i.placement==API.TOPPANEL_PLACEMENT) {
                toppanel_right.add(label);
            }
        }
        this.add(bottompane,BorderLayout.CENTER);
        Main.idewindow.set_menubar(getmenubar());
        update_title();
        SwingUtilities.updateComponentTreeUI(this);
        setfont.setfont(Main.font,this.getAccessibleContext());
        editor.this.updateUI();
        is_init=true;
    }
    private JMenuBar getmenubar() throws IOException, ParseException {
        JMenuBar menuBar = new JMenuBar();
        //menuBar.setOpaque(false);
        JMenu filemenu = new JMenu("File");
        JMenu filemenu_new = new JMenu("New");
        JMenu filemenu_open_recent = new JMenu("Open recent");
        JMenuItem filemenu_close = new JMenuItem("Close project");
        JMenuItem filemenu_open = new JMenuItem("Open project");
        JMenuItem filemenu_settings = new JMenuItem("Settings");
        JMenuItem filemenu_rename = new JMenuItem("Rename project");
        JMenuItem filemenu_refresh_proxy = new JMenuItem("Refresh Proxy settings");

        JMenuItem filemenu_new_file = new JMenuItem("File");
        JMenuItem filemenu_new_folder = new JMenuItem("Folder");
        JMenuItem filemenu_new_project = new JMenuItem("Project");
        filemenu_new.add(filemenu_new_file);
        filemenu_new.add(filemenu_new_folder);
        filemenu_new.add(filemenu_new_project);
        filemenu_new_file.addActionListener(e -> {
            String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type file name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
            if (output==null) {
                return;
            }
            File newfile = new File(fileview.current_folder+"/"+output);
            try {
                newfile.createNewFile();
                fileview.update_list();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        filemenu_new_folder.addActionListener(e -> {
            String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type folder name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
            if (output==null) {
                return;
            }
            try {
                Files.createDirectories(Paths.get(fileview.current_folder+"/"+output));
                fileview.update_list();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        filemenu_new_project.addActionListener(e -> {
            try {
                newproject.newproject();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        for (Object i : (JSONArray)Main.configjson.get("project_paths")) {
            if (!new File(i+"/"+Main.project_json_name).exists()) {
                continue;
            }
            FileReader fileReader = new FileReader(i +"/"+Main.project_json_name);
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(fileReader);
            fileReader.close();
            JMenuItem menuItem = new JMenuItem((String) jsonObject.get("projectname"));
            menuItem.addActionListener(e -> {
                JSONArray projectarray = ((JSONArray)Main.configjson.get("project_paths"));
                if (!Main.open_project((String) i)) {
                    JLabel label = new JLabel("That project doesn't exist anymore, remove it from the list?");
                    int output = JOptionPane.showConfirmDialog(Main.idewindow.windowframe,label,Main.idewindow.windowframe.getTitle(),JOptionPane.YES_NO_OPTION);
                    if (output==JOptionPane.YES_OPTION) {
                        projectarray.remove(i);
                        //Main.idewindow.set_screen(new startscreen());
                    }
                }
            });
            filemenu_open_recent.add(menuItem);
        }

        filemenu_close.addActionListener(e -> {
            // unloads the project
            try {
                textEditor.save_all();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Main.idewindow.set_screen(new startscreen());
        });
        filemenu_open.addActionListener(e -> {
            try {
                openproject.openproject();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        filemenu_settings.addActionListener(e -> {
            try {
                settings.settings();
            } catch (IOException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        });
        filemenu_rename.addActionListener(e -> {
            String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type new project name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
            if (output==null) {
                return;
            }
            for (Component i : filemenu_open_recent.getMenuComponents()) {
                if (i instanceof JMenuItem) {
                    if (((JMenuItem)i).getText().equalsIgnoreCase((String) projectjson.get("projectname"))) {
                        ((JMenuItem)i).setText(output);
                    }
                }
            }
            projectjson.put("projectname",output);
        });
        filemenu_refresh_proxy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    util.detect_and_set_proxy("https://www.google.com");
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        filemenu.add(filemenu_new);
        filemenu.add(filemenu_open_recent);
        filemenu.add(filemenu_close);
        filemenu.add(filemenu_open);
        filemenu.add(filemenu_settings);
        filemenu.add(filemenu_rename);
        filemenu.add(filemenu_refresh_proxy);

        menuBar.add(filemenu);

        JMenu helpmenu = new JMenu("Help");
        menuBar.add(helpmenu);
        JMenuItem helpmenu_changelog = new JMenuItem("Change log");
        helpmenu_changelog.addActionListener(e -> {
            show_changelog();
        });
        helpmenu.add(helpmenu_changelog);
        JMenuItem helpmenu_about = new JMenuItem("About");
        helpmenu_about.addActionListener(e -> {
            JOptionPane.showMessageDialog(Main.idewindow.windowframe,"Spwner IDE alpha v1.1 (build 2)","About "+Main.program_name,JOptionPane.INFORMATION_MESSAGE);
        });
        helpmenu.add(helpmenu_about);
        add_menu_keys(menuBar);
        for (menuitem i : Main.custommenuitems) {
            if (i.path.equalsIgnoreCase("")) {
                JMenu menu = new JMenu(i.menuname);
                menuBar.add(menu);
                menuHashMap.put(i.menuname,menu);
            } else {
                JMenuItem menu = new JMenuItem(i.menuname);
                menu.addActionListener(e -> i.menu_clicked());
                for (Map.Entry<String, pluginloader> entry : Main.pluginHashMap.entrySet()) {
                    if (entry.getValue().plugin==Main.pluginsmenuhashmap.get(i)) {
                        create_menu_path(util.format_string(i.path,entry.getValue()),menuBar);
                        menuHashMap.get(util.format_string(i.path,entry.getValue())).add(menu);
                        break;
                    }
                }
            }
        }

        return menuBar;
    }
    private void create_menu_path(String path,JMenuBar menuBar) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] splitpath = path.split("/");
        for (int i = 0; i < splitpath.length; i++) {
            stringBuilder.append(splitpath[i]).append("/");
            String finalstring = new StringBuilder(stringBuilder.toString()).deleteCharAt(stringBuilder.length()-1).toString();
            if (!(menuHashMap.get(finalstring)==null)) {
                if (!(i==splitpath.length-1)) {
                    JMenu menu = new JMenu(splitpath[i+1]);
                    if (menuHashMap.get(finalstring+"/"+menu.getText())==null) {
                        menuHashMap.get(finalstring).add(menu);
                        menuHashMap.put(finalstring+"/"+splitpath[i+1],menu);
                    }
                }
            }
        }
    }
    private void add_menu_keys(JMenuBar menuBar) {
        menuHashMap.clear();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            add_menu_keys(menuBar.getMenu(i),""+menuBar.getMenu(i).getText());
        }
    }
    private void add_menu_keys(JMenu menu,String path) {
        menuHashMap.put(path,menu);
        for (Component i : menu.getMenuComponents()) {
            if (i instanceof JMenu) {
                menuHashMap.put(path+"/"+((JMenu)i).getText(),(JMenu) i);
                add_menu_keys((JMenu) i,path+"/"+((JMenu)i).getText());
            }
        }
    }
    private void update_fileview() throws IOException {
        if (!(fileview==null)) {
            this.remove(fileview);
        }
        fileview = new projectfileview(folder_path, textEditor);
        this.add(fileview);
    }

    @Override
    public void updateUI() {
        if (!is_init) {return;}
        //update_fileview();
        fileview.setPreferredSize(new Dimension(280,middlepanel.getHeight()));
        textEditor.setPreferredSize(new Dimension(middlepanel.getWidth()-280,middlepanel.getHeight()));
        fileview.updateUI();
        textEditor.updateUI();
        super.updateUI();
    }
}
