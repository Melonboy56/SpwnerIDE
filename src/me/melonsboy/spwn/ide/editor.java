package me.melonsboy.spwn.ide;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private void show_changelog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("assets/changelog.txt")));
        String text = bufferedReader.lines().collect(Collectors.joining("\n"));
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setFont(Main.font);
        textArea.setForeground(Color.WHITE);
        panel.add(new JScrollPane(textArea));
        JPanel settingspanel = new JPanel();
        settingspanel.setBackground(Color.DARK_GRAY);
        panel.add(settingspanel,BorderLayout.PAGE_END);
        JCheckBox dontshownexttimecheckbox = new JCheckBox("Don't open this tab");
        dontshownexttimecheckbox.setSelected(!(Boolean) Main.configjson.get("open_changelog_message"));
        dontshownexttimecheckbox.setFont(Main.font);
        dontshownexttimecheckbox.setForeground(Color.WHITE);
        settingspanel.add(dontshownexttimecheckbox);
        JButton closechangelogbutton = new JButton("Close tab");
        closechangelogbutton.setFont(Main.font);
        closechangelogbutton.setForeground(Color.WHITE);
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
        textEditor.add_memory_tab("<changelog>","Spwner IDE alpha v1.0 changelog",panel);
    }

    @Override
    public void setVisible(boolean aFlag) {
        if (!aFlag) {
            try {
                textEditor.save_all();
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
        Main.idewindow.set_menubar(getmenubar());
        this.setBackground(Color.DARK_GRAY);
        this.projectjson = projectjson;
        this.setLayout(new BorderLayout());
        bottompane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottompane.setResizeWeight(0.8);
        bottompane.setContinuousLayout(true);
        this.add(bottompane,BorderLayout.CENTER);
        middlepanel = new JSplitPane();
        middlepanel.setContinuousLayout(true);
        middlepanel.setBackground(Color.DARK_GRAY);
        folder_path = folderpath;
        textEditor = new file_editor(folderpath,this,false);
        textEditor.setFont(Main.font);
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
        fileview.setFont(Main.font);
        middlepanel.add(fileview,JSplitPane.LEFT);
        middlepanel.add(textEditor,JSplitPane.RIGHT);
        bottompane.add(middlepanel,JSplitPane.TOP);
        JPanel compilepanel = new JPanel();
        compilepanel.setBackground(Color.DARK_GRAY);
        compilepanel.setLayout(new BorderLayout());
        JPanel compile_log_panel = new JPanel();
        compile_log_panel.setBackground(Color.DARK_GRAY);
        JScrollPane compile_scrollpane = new JScrollPane(compile_log_panel);
        compilepanel.add(compile_scrollpane,BorderLayout.CENTER);
        JLabel compilebutton = new JLabel("Compile selected file",JLabel.CENTER);
        compilebutton.setFont(Main.font);
        compilebutton.setForeground(Color.WHITE);
        compilebutton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (textEditor.filetabs.getSelectedIndex()==-1) {
                    JOptionPane.showMessageDialog(Main.idewindow.windowframe,"Please open a file to compile it",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    textEditor.save_all();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new compile(compile_log_panel,textEditor.get_selected_filepath()).start();
                compile_log_panel.updateUI();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                compilebutton.setForeground(Color.LIGHT_GRAY);
                compilebutton.updateUI();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                compilebutton.setForeground(Color.WHITE);
                compilebutton.updateUI();
            }
        });
        compilepanel.add(compilebutton,BorderLayout.PAGE_END);
        compilepanel.setBackground(Color.DARK_GRAY);
        compile_scrollpane.setBackground(Color.DARK_GRAY);
        compilepanel.setPreferredSize(new Dimension(this.getWidth(),150));
        bottompane.add(compilepanel,JSplitPane.BOTTOM);
        middlepanel.setDividerLocation(Integer.parseInt(Main.configjson.get("middlesplit").toString()));
        bottompane.setDividerLocation(Integer.parseInt(Main.configjson.get("bottomsplit").toString()));
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            SwingUtilities.invokeLater(editor.this::updateUI);
            return null;
        }, 500, TimeUnit.MILLISECONDS);
        is_init=true;
    }
    private JMenuBar getmenubar() throws IOException, ParseException {
        backgroundmenubar menuBar = new backgroundmenubar();
        menuBar.setColor(Color.DARK_GRAY);
        menuBar.setOpaque(false);
        JMenu filemenu = new JMenu("File");
        JMenu filemenu_new = new JMenu("New");
        JMenu filemenu_open_recent = new JMenu("Open recent");
        JMenuItem filemenu_close = new JMenuItem("Close project");
        JMenuItem filemenu_open = new JMenuItem("Open project");
        JMenuItem filemenu_settings = new JMenuItem("Settings");
        JMenuItem filemenu_rename = new JMenuItem("Rename project");
        filemenu.setFont(Main.font);
        filemenu_new.setFont(Main.font);
        filemenu_open_recent.setFont(Main.font);
        filemenu_close.setFont(Main.font);
        filemenu_settings.setFont(Main.font);
        filemenu_rename.setFont(Main.font);
        filemenu_open.setFont(Main.font);
        filemenu.setForeground(Color.WHITE);
        filemenu_new.setForeground(Color.WHITE);
        filemenu_open_recent.setForeground(Color.WHITE);
        filemenu_close.setForeground(Color.WHITE);
        filemenu_open.setForeground(Color.WHITE);
        filemenu_settings.setForeground(Color.WHITE);
        filemenu_rename.setForeground(Color.WHITE);

        JMenuItem filemenu_new_file = new JMenuItem("File");
        JMenuItem filemenu_new_folder = new JMenuItem("Folder");
        JMenuItem filemenu_new_project = new JMenuItem("Project");
        filemenu_new_file.setFont(Main.font);
        filemenu_new_folder.setFont(Main.font);
        filemenu_new_project.setFont(Main.font);
        filemenu_new_file.setForeground(Color.WHITE);
        filemenu_new_folder.setForeground(Color.WHITE);
        filemenu_new_project.setForeground(Color.WHITE);
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
            FileReader fileReader = new FileReader(i +"/project.json");
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(fileReader);
            fileReader.close();
            JMenuItem menuItem = new JMenuItem((String) jsonObject.get("projectname"));
            //String name = (String) jsonObject.get("projectname");
            menuItem.setFont(Main.font);
            menuItem.setForeground(Color.WHITE);
            menuItem.addActionListener(e -> {
                JSONArray projectarray = ((JSONArray)Main.configjson.get("project_paths"));
                if (!Main.open_project((String) i)) {
                    JLabel label = new JLabel("That project doesn't exist anymore, remove it from the list?");
                    label.setFont(Main.font);
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
            } catch (IOException ex) {
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
        filemenu.add(filemenu_new);
        filemenu.add(filemenu_open_recent);
        filemenu.add(filemenu_close);
        filemenu.add(filemenu_open);
        filemenu.add(filemenu_settings);
        filemenu.add(filemenu_rename);

        menuBar.add(filemenu);

        JMenu helpmenu = new JMenu("Help");
        helpmenu.setForeground(Color.WHITE);
        helpmenu.setFont(Main.font);
        menuBar.add(helpmenu);
        JMenuItem helpmenu_changelog = new JMenuItem("Change log");
        helpmenu_changelog.addActionListener(e -> {
            show_changelog();
        });
        helpmenu_changelog.setFont(Main.font);
        helpmenu_changelog.setForeground(Color.WHITE);
        helpmenu.add(helpmenu_changelog);
        JMenuItem helpmenu_about = new JMenuItem("About");
        helpmenu_about.addActionListener(e -> {
            JOptionPane.showMessageDialog(Main.idewindow.windowframe,"Spwner IDE alpha v1.0 (build 1)","About "+Main.idewindow.windowframe.getTitle(),JOptionPane.INFORMATION_MESSAGE);
        });
        helpmenu_about.setFont(Main.font);
        helpmenu_about.setForeground(Color.WHITE);
        helpmenu.add(helpmenu_about);

        return menuBar;
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
        //fileview.setBounds(0,0,300,Main.idewindow.windowframe.getHeight()-80);
        //textEditor.setOpaque(true);
        //textEditor.setBounds(300,0,Main.idewindow.windowframe.getWidth()-315,Main.idewindow.windowframe.getHeight()-80);
        super.updateUI();
    }
}
