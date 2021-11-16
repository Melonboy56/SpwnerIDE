package me.melonsboy.plugins.spwner_ide.core_plugin;

import com.sun.javafx.PlatformUtil;
import me.melonsboy.plugins.spwner_ide.core_plugin.custom.console;
import me.melonsboy.spwn.ide.*;
import me.melonsboy.spwn.ide.custom.compiler;
import me.melonsboy.spwn.ide.custom.compilers_source;
import me.melonsboy.spwn.ide.custom.iconButton;
import me.melonsboy.spwn.ide.exceptions.extension_already_exists;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static me.melonsboy.spwn.ide.compile.*;


public class Plugin extends me.melonsboy.spwn.ide.Plugin {
    private JPanel compilepanel;
    private JTextArea compileconsoletextarea;
    static ArrayList<console> consoleArrayList = new ArrayList<>();
    private static ArrayList<Process> native_terminal_process_arraylist = new ArrayList<>();

    @Override
    public void onDisable() {
        for (Process i : native_terminal_process_arraylist) {
            i.destroyForcibly();
            while (i.isAlive()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static final String github_repo_url = "https://raw.githubusercontent.com/Melonboy56/files_for_scripts/main/";

    @Override
    public void onEnable() {
        {
            compilers_source compilersSource = new compilers_source() {
                private ArrayList<String> downloaded_compilers_list;
                private JSONObject compilersjson;
                @Override
                public ArrayList<String> get_list() {
                    if (downloaded_compilers_list == null) {downloaded_compilers_list=new ArrayList<>();}
                    downloaded_compilers_list.clear();
                    try {
                        compilersjson = download_compilers_list();
                        System.out.println(compilersjson);
                    } catch (IOException | ParseException | URISyntaxException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (compilersjson==null) {return null;}
                    for (Object i : compilersjson.keySet()) {
                        JSONObject jsonObject1 = (JSONObject) compilersjson.get(i);
                        if (util.can_download((JSONObject) jsonObject1.get("path"))) {
                            downloaded_compilers_list.add(i.toString());
                        }
                    }
                    Collections.sort(downloaded_compilers_list);
                    return downloaded_compilers_list;
                }
                private JSONObject download_compilers_list() throws IOException, ParseException, URISyntaxException, InterruptedException {
                    GetMethod getMethod = new GetMethod(github_repo_url+"files.json");
                    byte[] outputbytes = me.melonsboy.spwn.ide.API.run_http_method(getMethod,null,null);
                    JSONObject onlinecompiler_list = (JSONObject) ((JSONObject) new JSONParser().parse(new String(outputbytes))).get("spwn");
                    return onlinecompiler_list;
                }

                @Override
                public void download_compiler(int i) {
                    System.out.println(downloaded_compilers_list.get(i));
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
                    JTextField compilername_textfield = new JTextField();
                    JTextField compilerlocation_textfield = new JTextField();
                    {
                        JPanel panel1 = new JPanel();
                        panel1.setLayout(new BorderLayout());
                        JLabel label = new JLabel("Name");
                        label.setPreferredSize(new Dimension(150,Main.font.getSize()+10));
                        panel1.add(label,BorderLayout.WEST);
                        compilername_textfield.setText(downloaded_compilers_list.get(i));
                        panel1.add(compilername_textfield,BorderLayout.CENTER);
                        panel.add(panel1);
                    }
                    {
                        final boolean[] is_edited = {false};
                        JPanel panel1 = new JPanel();
                        panel1.setLayout(new BorderLayout());
                        JLabel label = new JLabel("Path");
                        label.setPreferredSize(new Dimension(150,Main.font.getSize()+10));
                        panel1.add(label,BorderLayout.WEST);
                        compilerlocation_textfield.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyTyped(KeyEvent e) {
                                is_edited[0] =true;
                            }
                        });
                        compilername_textfield.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyReleased(KeyEvent e) {
                                if (!is_edited[0]) {
                                    compilerlocation_textfield.setText((me.melonsboy.spwn.ide.API.get_mainfolder_path() + "/compilers/"+compilername_textfield.getText()).replace('\\','/'));
                                }
                            }
                        });
                        panel1.add(compilerlocation_textfield,BorderLayout.CENTER);
                        compilerlocation_textfield.setText((me.melonsboy.spwn.ide.API.get_mainfolder_path() + "/compilers/"+compilername_textfield.getText()).replace('\\','/'));
                        JButton choosepathbutton = new JButton("...");
                        choosepathbutton.addActionListener(e -> {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                            int output = fileChooser.showSaveDialog(Main.idewindow.windowframe);
                            if (output==JFileChooser.APPROVE_OPTION) {
                                compilerlocation_textfield.setText(fileChooser.getSelectedFile().getPath());
                            }
                        });
                        panel1.add(choosepathbutton,BorderLayout.EAST);
                        panel.add(panel1);
                    }
                    panel.setPreferredSize(new Dimension(700,Main.font.getSize()*4));
                    String[] strings = new String[] {"Download","Cancel"};
                    int output = JOptionPane.showOptionDialog(Main.idewindow.windowframe,panel,"Download a compiler",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,strings,strings[0]);
                    if (output==0) {
                        try {
                            new File(me.melonsboy.spwn.ide.API.get_mainfolder_path()+"/downloaded_compilers/spwn").mkdirs();
                            GetMethod getMethod = null;
                            String mainfile = null;
                            AtomicInteger atomicInteger = new AtomicInteger(0);
                            if (PlatformUtil.isWindows()) {
                                getMethod = new GetMethod(github_repo_url+((JSONObject)((JSONObject)compilersjson.get(downloaded_compilers_list.get(i))).get("path")).get("windows").toString());
                                mainfile = ((JSONObject)((JSONObject)compilersjson.get(downloaded_compilers_list.get(i))).get("mainfile")).get("windows").toString();
                            } else if (PlatformUtil.isMac()) {
                                getMethod = new GetMethod(github_repo_url+((JSONObject)((JSONObject)compilersjson.get(downloaded_compilers_list.get(i))).get("path")).get("mac").toString());
                                mainfile = ((JSONObject)((JSONObject)compilersjson.get(downloaded_compilers_list.get(i))).get("mainfile")).get("mac").toString();
                            } else if (PlatformUtil.isLinux()) {
                                getMethod = new GetMethod(github_repo_url+((JSONObject)((JSONObject)compilersjson.get(downloaded_compilers_list.get(i))).get("path")).get("linux").toString());
                                mainfile = ((JSONObject)((JSONObject)compilersjson.get(downloaded_compilers_list.get(i))).get("mainfile")).get("linux").toString();
                            }
                            JDialog downloading_dialog = new JDialog(Main.idewindow.windowframe,"Downloading Compiler",true);
                            downloading_dialog.setLayout(new BorderLayout());
                            //downloading_dialog.setResizable(false);
                            downloading_dialog.setSize(400,Main.font.getSize()*4);
                            JLabel label = new JLabel("0% Complete",JLabel.CENTER);
                            downloading_dialog.add(label);
                            String finalMainfile = mainfile;
                            new Thread() {
                                @Override
                                public void run() {
                                    while (atomicInteger.get() != -1) {
                                        try {
                                            label.setText(atomicInteger.get()+"% Complete");
                                            label.updateUI();
                                            Thread.sleep(10);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    label.setText("Installing Compiler..");
                                    label.updateUI();
                                    new File(me.melonsboy.spwn.ide.API.get_mainfolder_path()+"/downloaded_compilers/spwn/"+((JSONObject)compilersjson.get(downloaded_compilers_list.get(i))).get("foldername")).mkdirs();
                                    ZipFile zipFile = new ZipFile(me.melonsboy.spwn.ide.API.get_mainfolder_path()+"/downloaded_compilers/spwn/"+((JSONObject)compilersjson.get(downloaded_compilers_list.get(i))).get("foldername"));
                                    try {
                                        zipFile.extractAll(compilerlocation_textfield.getText());
                                    } catch (ZipException e) {
                                        e.printStackTrace();
                                    }
                                    JSONObject _jsonObject = new JSONObject();
                                    _jsonObject.put("path",compilerlocation_textfield.getText()+"/"+ finalMainfile);
                                    compilerslist.put(compilername_textfield.getText().replace('\\','/'),_jsonObject);
                                    downloading_dialog.setVisible(false);
                                }
                            }.start();
                            assert getMethod != null;
                            GetMethod finalGetMethod = getMethod;
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        if (new File(me.melonsboy.spwn.ide.API.get_mainfolder_path()+"/downloaded_compilers/spwn/"+((JSONObject)compilersjson.get(downloaded_compilers_list.get(i))).get("foldername")).exists()) {
                                            atomicInteger.set(-1);
                                        } else {
                                            FileWriter fileWriter = new FileWriter(me.melonsboy.spwn.ide.API.get_mainfolder_path()+"/downloaded_compilers/spwn/"+((JSONObject)compilersjson.get(downloaded_compilers_list.get(i))).get("foldername"),StandardCharsets.ISO_8859_1);
                                            me.melonsboy.spwn.ide.API.run_http_method(finalGetMethod,atomicInteger,fileWriter);
                                        }
                                        if (downloading_dialog.isVisible()) {
                                            downloading_dialog.setVisible(true);
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }.start();
                            downloading_dialog.setVisible(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public String get_name() {
                    return "Spwn Compilers";
                }
            };
            add_compilers_source(compilersSource);
        }
        {
            compilepanel = new JPanel();
            compilepanel.setLayout(new BorderLayout());
            compileconsoletextarea = new JTextArea();
            compileconsoletextarea.setEditable(false);
            compilepanel.add(new JScrollPane(compileconsoletextarea),BorderLayout.CENTER);
        }
        {
            JComboBox<String> compiler_combobox = new JComboBox<>();
            JTextField script_path_field = new JTextField();
            JTextField compiler_args_field = new JTextField();
            compiler compiler = new compiler("Spwn Compiler") {
                @Override
                public void save_settings(JSONObject settingsjson) {
                    settingsjson.put("path",script_path_field.getText());
                    settingsjson.put("compilerversion",compiler_combobox.getSelectedItem());
                    settingsjson.put("compile_args",compiler_args_field.getText());
                }

                @Override
                public void load_settings(JSONObject settingsjson) {
                    settingsjson.putIfAbsent("compile_args","build {filepath}");
                    // loads the compilers
                    compiler_combobox.removeAllItems();
                    for (Object i : compile.compilerslist.entrySet()) {
                        Map.Entry entry = (Map.Entry) i;
                        System.out.println(entry);
                        compiler_combobox.addItem((String) entry.getKey());
                    }
                    compiler_combobox.setSelectedItem(settingsjson.get("compilerversion"));
                    // sets the script path
                    script_path_field.setText((String) settingsjson.get("path"));
                    // sets the compiler arguments
                    compiler_args_field.setText((String) settingsjson.get("compile_args"));

                }

                @Override
                public void compile(JTextArea compileconsoletextarea,JSONObject settingsjson) {
                    try {
                        compileconsoletextarea.setText("");
                        compileconsoletextarea.repaint();
                        add_bottom_tab(compilepanel,"core-compile","Build",null,false);
                        save_all_files();
                        JSONObject jsonObject = get_compile_config_json(get_selected_compile_config_name());
                        if (jsonObject==null) {
                            JOptionPane.showMessageDialog(Main.idewindow.windowframe,"Looks like there is no compile configurations",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        // compile spwn code
                        {
                            try {
                                String compiler_args = (String) settingsjson.get("compile_args");
                                String compiler_file_path = ((JSONObject)compile.compilerslist.get(settingsjson.get("compilerversion"))).get("path").toString();
                                String file_path = get_project_folder().replace('\\','/')+"/"+settingsjson.get("path");
                                compiler_args = compiler_args.replace("{filepath}",file_path);
                                String commandargs = " "+compiler_args;
                                File compiler_file = new File(compiler_file_path);
                                if (compiler_file.exists() && compiler_file.isFile()) {
                                    ProcessBuilder builder = new ProcessBuilder(compiler_file.getPath());
                                    for (String i : commandargs.split(" ")) {
                                        if (i.equalsIgnoreCase("")) {continue;}
                                        builder.command().add(i);
                                    }
                                    System.out.println(Arrays.toString(builder.command().toArray()));
                                    builder.redirectErrorStream(true);
                                    Process process = builder.start();
                                    InputStream is = process.getInputStream();
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                                    String line;
                                    while ((line=reader.readLine()) != null) {
                                        String replaced_line = line.replaceAll("\u001B\\[[;\\d]*m", "");
                                        try {
                                            //textArea.setLayout(new BoxLayout(textArea,BoxLayout.PAGE_AXIS));
                                            compileconsoletextarea.append(replaced_line+"\n");
                                            compileconsoletextarea.repaint();
                                        } catch (NullPointerException exception) {
                                            exception.printStackTrace();
                                        }
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(Main.idewindow.windowframe,"The compiler didn't exist or it wasn't a file","Error",JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        }
                        //new compile(compile_log_panel,folderpath,get_compile_config_json(get_selected_compile_config_name())).start();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            };
            compiler.setLayout(new BoxLayout(compiler,BoxLayout.PAGE_AXIS));
            {
                JPanel panel = new JPanel() {
                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(compiler.getSize().width,Main.font.getSize()+20);
                    }
                };
                panel.setLayout(new GridLayout());
                JLabel label = new JLabel("Compiler");
                panel.add(label);
                panel.add(compiler_combobox);
                compiler.add(panel);
            }
            {
                JPanel panel = new JPanel() {
                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(compiler.getSize().width,Main.font.getSize()+20);
                    }
                };
                panel.setLayout(new GridLayout());
                JLabel label = new JLabel("Script path");
                panel.add(label);
                panel.add(script_path_field);
                compiler.add(panel);
            }
            {
                JPanel panel = new JPanel() {
                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(compiler.getSize().width,Main.font.getSize()+20);
                    }
                };
                panel.setLayout(new GridLayout());
                JLabel label = new JLabel("Compiler arguments");
                panel.add(label);
                panel.add(compiler_args_field);
                compiler.add(panel);
            }
            try {
                add_compiler(compiler);
            } catch (me.melonsboy.spwn.ide.exceptions.compiler_name_already_exists compiler_name_already_exists) {
                compiler_name_already_exists.printStackTrace();
            }
        }
        {
            iconButton iconButton = new iconButton() {
                @Override
                public String get_tooltip() {
                    return "Compile Code";
                }

                private boolean is_compiling = false;
                @Override
                public void button_clicked(boolean is_right_click) {
                    if (!is_right_click) {
                        if (is_compiling) {return;}
                        for (compiler i : Main.compilerArrayList) {
                            JSONObject compilerconfigs = ((JSONObject)((editor)Main.idewindow.thepanel).projectjson.get("compileconfigs"));
                            JSONObject selected_compiler_config = (JSONObject) compilerconfigs.get(((editor)Main.idewindow.thepanel).compileconfigs.getSelectedItem().toString());
                            if (i.compiler_name.equalsIgnoreCase((String) selected_compiler_config.get("compilername"))) {
                                try {
                                    compileconsoletextarea.setText("");
                                    compileconsoletextarea.repaint();
                                    add_bottom_tab(compilepanel,"core-compile","Build",null,false);
                                    save_all_files();
                                    Thread thread = new Thread() {
                                        @Override
                                        public void run() {
                                            is_compiling=true;
                                            update_icon();
                                            i.compile(compileconsoletextarea, (JSONObject) selected_compiler_config.get("settings"));
                                            is_compiling=false;
                                            update_icon();
                                        }
                                    };
                                    thread.start();
                                    //i.load_settings((JSONObject) ((JSONObject) (compilerconfigs.get(((editor)Main.idewindow.thepanel).compileconfigs.getSelectedItem().toString()))).get("settings"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                }

                @Override
                public ImageIcon get_icon() {
                    return is_compiling ? new ImageIcon(Plugin.this.getClass().getResource("assets/compile_gray.png")) : new ImageIcon(Plugin.this.getClass().getResource("assets/compile.png"));
                }
            };
            iconButton.show_in_templates=false;
            add_button_icon(iconButton);
        }
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JTabbedPaneCloseButton tabbedPane = new JTabbedPaneCloseButton();
            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            JPanel bottompanel = new JPanel();
            bottompanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JButton bottompanel_left_new_session_button = new JButton("New Console Session");
            bottompanel_left_new_session_button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    for (console i : consoleArrayList) {
                        JMenuItem menuItem = new JMenuItem(i.get_name());
                        menuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                JPanel panel = i.get_instance();
                                tabbedPane.addTab(i.get_name(),panel);
                            }
                        });
                        popupMenu.add(menuItem);
                    }
                    popupMenu.show(bottompanel_left_new_session_button,bottompanel_left_new_session_button.getMousePosition().x,bottompanel_left_new_session_button.getMousePosition().y);
                }
            });
            bottompanel.add(bottompanel_left_new_session_button);
            panel.add(tabbedPane,BorderLayout.CENTER);
            panel.add(bottompanel,BorderLayout.PAGE_START);
            //panel.add(toppanel,BorderLayout.PAGE_START);

            add_bottom_tab(panel,"core-terminal","Terminal",new ImageIcon(Plugin.this.getClass().getResource("assets/spwn.png")),true);
        }
        {
            {
                console console = new console() {
                    @Override
                    public JPanel get_instance() {
                        ProcessBuilder processBuilder = new ProcessBuilder();
                        if (PlatformUtil.isWindows()) {processBuilder.command().add("cmd.exe");}
                        if (PlatformUtil.isLinux()) {processBuilder.command().add("xterm");}
                        if (PlatformUtil.isMac()) {processBuilder.command().add("terminal.app");}
                        processBuilder.redirectErrorStream(true);
                        JPanel panel = new JPanel(new BorderLayout());
                        final int[] pressed_keys_count = {0};
                        JTextArea console_text_area = new JTextArea();
                        console_text_area.setEditable(false);
                        console_text_area.setText("");
                        panel.add(new JScrollPane(console_text_area),BorderLayout.CENTER);
                        try {
                            Process process = processBuilder.start();
                            native_terminal_process_arraylist.add(process);
                            OutputStream outputStream = process.getOutputStream();
                            InputStream inputStream = process.getInputStream();
                            StringBuilder outputstring = new StringBuilder();
                            console_text_area.addKeyListener(new KeyAdapter() {
                                @Override
                                public void keyPressed(KeyEvent e) {
                                    try {
                                        //outputStream.flush();
                                        if (!process.isAlive()) {return;}
                                        if (e.getKeyChar()=='\b') {
                                            if (pressed_keys_count[0] != 0) {
                                                StringBuilder stringBuilder = new StringBuilder(console_text_area.getText());
                                                stringBuilder.deleteCharAt(stringBuilder.length()-1);
                                                outputstring.deleteCharAt(outputstring.length()-1);
                                                console_text_area.setText(stringBuilder.toString());
                                                pressed_keys_count[0]--;
                                            }
                                        } else if (e.getKeyChar()=='\n') {
                                            StringBuilder stringBuilder = new StringBuilder(console_text_area.getText());
                                            for (int i = 0; i < pressed_keys_count[0]; i++) {
                                                stringBuilder.deleteCharAt(stringBuilder.length()-1);
                                            }
                                            console_text_area.setText(stringBuilder.toString());
                                            pressed_keys_count[0]=0;
                                            outputStream.write(outputstring.toString().getBytes(StandardCharsets.UTF_8));
                                            outputStream.write('\n');
                                            while (outputstring.length() != 0) {
                                                outputstring.deleteCharAt(outputstring.length()-1);
                                            }
                                            outputStream.flush();
                                        } else {
                                            outputstring.append(e.getKeyChar());
                                            pressed_keys_count[0]++;
                                            if (e.getKeyChar() > 31 && e.getKeyChar() < 127) {
                                                console_text_area.append(String.valueOf(e.getKeyChar()));
                                            }
                                        }
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                            SwingUtilities.invokeLater(() -> {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        int character;
                                        while (panel.getParent() == null) {
                                            try {
                                                Thread.sleep(500);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                while (panel.getParent() != null) {
                                                    try {
                                                        Thread.sleep(500);
                                                        if (!process.isAlive()){break;}
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                process.destroyForcibly();
                                            }
                                        }.start();
                                        while (true) {
                                            try {
                                                if ((character = inputStream.read()) == -1) break;
                                                console_text_area.append(String.valueOf((char) character));
                                                console_text_area.repaint();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        native_terminal_process_arraylist.remove(process);
                                        console_text_area.append("\nProcess exited with exit code "+process.exitValue());
                                        console_text_area.repaint();
                                    }
                                }.start();
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return panel;
                    }

                    @Override
                    public String get_name() {
                        return "Native Terminal";
                    }
                };
                API.add_console(console);
            }
        }
        try {
            me.melonsboy.spwn.ide.API.add_file_extension("txt", new file() {
                @Override
                public JPanel get_instance(String filepath, boolean is_template, stringholder stringholder) {
                    try {
                        return new txt_editor(filepath, is_template, stringholder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public ImageIcon get_icon() {
                    return null;
                }
            });
            me.melonsboy.spwn.ide.API.add_file_extension("spwn", new file() {
                @Override
                public JPanel get_instance(String filepath, boolean is_template, stringholder stringholder) {
                    try {
                        return new spwn_editor(filepath, is_template, stringholder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public ImageIcon get_icon() {
                    return new ImageIcon(Plugin.this.getClass().getResource("assets/spwn.png"));
                }
            });
            me.melonsboy.spwn.ide.API.add_file_extension("json", new file() {
                @Override
                public JPanel get_instance(String filepath, boolean is_template, stringholder stringholder) {
                    try {
                        return new json_editor(filepath, is_template, stringholder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public ImageIcon get_icon() {
                    return null;
                }
            });
        } catch (extension_already_exists e) {
            e.printStackTrace();
        }
    }
}
