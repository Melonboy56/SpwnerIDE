package me.melonsboy.spwn.ide;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class templates extends JPanel {
    private JLabel statuslabel;
    public file_editor fileEditor;
    private HashMap<String, Boolean> files_list;
    private HashMap<String,Boolean> opened_files = new HashMap<>();
    private HashMap<String,stringholder> file_content = new HashMap<>();
    private JPanel fileview_content;
    private JLabel fileview_path;
    private FileSystem memoryfilesystem;
    private boolean is_template_loaded = false;
    private JSONObject templatejson;
    private String current_path;
    private JSplitPane middlesplit_pane;
    private JSONObject compileconfigs;
    private String selected_compile_config;
    private JComboBox<String> compileconfigs_list;
    private void refresh_compileconfigs() {
        compileconfigs_list.removeAllItems();
        for (Object i : templates.this.compileconfigs.entrySet()) {
            compileconfigs_list.addItem(((Map.Entry)i).getKey().toString());
        }
    }
    public templates() throws FileNotFoundException {
        memoryfilesystem = Jimfs.newFileSystem(Configuration.unix().toBuilder().setWorkingDirectory("/").build());
        Main.idewindow.set_menubar(getmenubar());
        compileconfigs = new JSONObject();
        this.setLayout(new BorderLayout());
        statuslabel = new JLabel("Template open: None");
        this.add(statuslabel,BorderLayout.PAGE_END);
        middlesplit_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        middlesplit_pane.setContinuousLayout(true);
        middlesplit_pane.setResizeWeight(0.4);
        middlesplit_pane.setDividerLocation(Integer.parseInt(Main.configjson.get("middlesplit").toString()));
        JPanel fileview = new JPanel();
        fileview.setLayout(new BorderLayout());
        fileview_content = new JPanel();
        fileview_content.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    JMenuItem createfile = new JMenuItem("Create file");
                    JMenuItem createfolder = new JMenuItem("Create folder");
                    createfile.setEnabled(is_template_loaded);
                    createfolder.setEnabled(is_template_loaded);
                    createfile.addActionListener(e1 -> {
                        String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type new file name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
                        if (output==null) {return;}
                        if (output.replace(" ","").equalsIgnoreCase("")) {
                            JOptionPane.showMessageDialog(Main.idewindow.windowframe,"You can't have a empty name",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        try {
                            Files.createFile(memoryfilesystem.getPath(current_path+"/"+output));
                            file_content.put(current_path.replaceFirst("/","")+"/"+output,new stringholder());
                            list_files(current_path);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                    createfolder.addActionListener(e1 -> {
                        String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type new file name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
                        if (output==null) {return;}
                        if (output.replace(" ","").equalsIgnoreCase("")) {
                            JOptionPane.showMessageDialog(Main.idewindow.windowframe,"You can't have a empty name",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        try {
                            Files.createDirectory(memoryfilesystem.getPath(current_path+"/"+output));
                            list_files(current_path);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                    popupMenu.add(createfile);
                    popupMenu.add(createfolder);

                    popupMenu.show(Main.idewindow.windowframe,Main.idewindow.windowframe.getMousePosition().x,Main.idewindow.windowframe.getMousePosition().y);
                }
            }
        });
        fileview_content.setLayout(new BoxLayout(fileview_content,BoxLayout.PAGE_AXIS));
        fileview.add(new JScrollPane(fileview_content),BorderLayout.CENTER);
        fileview_path = new JLabel("");
        fileview.add(fileview_path,BorderLayout.PAGE_END);
        files_list = new HashMap<>();
        fileEditor = new file_editor(files_list);
        middlesplit_pane.add(fileview);
        middlesplit_pane.add(fileEditor);
        this.add(middlesplit_pane);

        JPanel toppanel = new JPanel();
        toppanel.setPreferredSize(new Dimension(this.getWidth(),Main.font.getSize()+20));
        this.add(toppanel,BorderLayout.PAGE_START);
        toppanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        compileconfigs_list = new JComboBox<>();
        compileconfigs_list.setPreferredSize(new Dimension(150,Main.font.getSize()+10));
        toppanel.add(compileconfigs_list);

        refresh_compileconfigs();
        if (!(selected_compile_config==null)) {
            compileconfigs_list.setSelectedItem(selected_compile_config);
        }
        compileconfigs_list.addItemListener(e -> {
            selected_compile_config = e.getItem().toString();
        });


        JLabel editcompileconfigslabel = new JLabel();
        editcompileconfigslabel.setIcon(util.scaleImage(new ImageIcon(Main.class.getResource("assets/settings.png")),32,32));
        editcompileconfigslabel.setOpaque(true);
        class refresh_combo_box {
            @Override
            public String toString() {
                refresh_compileconfigs();
                return super.toString();
            }
        }
        editcompileconfigslabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!is_template_loaded) {return;}
                util.show_compiler_config(templates.this.compileconfigs,new refresh_combo_box());
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
        toppanel.add(editcompileconfigslabel);
    }

    @Override
    public void setVisible(boolean aFlag) {
        if (!aFlag) {
            try {
                Main.configjson.put("mainwindow_width",Main.idewindow.windowframe.getWidth());
                Main.configjson.put("mainwindow_height",Main.idewindow.windowframe.getHeight());
                save_template();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        super.setVisible(aFlag);
    }

    private void save_template() throws IOException {
        Main.configjson.put("middlesplit",middlesplit_pane.getDividerLocation());
        if (!is_template_loaded) {return;}
        HashMap<String,Boolean> files = new HashMap<>();
        list_files("/",files);
        JSONObject template_json = new JSONObject();
        int index = 0;
        for (Map.Entry<String,Boolean> i : files.entrySet()) {
            // "0":{"path":"src","type":"folder"}
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("path",i.getKey());
            if (i.getValue()) {
                jsonObject.put("type","folder");
            } else {
                jsonObject.put("type","file");
            }
            template_json.put(""+index,jsonObject);
            index++;
        }
        for (Map.Entry<String,stringholder> i : file_content.entrySet()) {
            // "2":{"path":"src\/main.spwn","text":"\/\/ write your code here\n","type":"filewrite"}
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("path",i.getKey());
            jsonObject.put("type","filewrite");
            jsonObject.put("text",i.getValue().data);
            template_json.put(""+index,jsonObject);
            index++;
        }
        for (Map.Entry<String,Boolean> i : opened_files.entrySet()) {
            // "3":{"path":"src\/main.spwn","type":"open"}
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("path",i.getKey());
            jsonObject.put("type","open");
            if (i.getValue()) {
                template_json.put(""+index,jsonObject);
            }
            index++;
        }
        for (Object i : compileconfigs.entrySet()) {
            // {"name":"Main","settings":{},"compilername":"Spwn Compiler","type":"compileconfig"}
            Map.Entry entry = (Map.Entry) i;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type","compileconfig");
            jsonObject.put("name",entry.getKey());
            jsonObject.put("compilername",((JSONObject)entry.getValue()).get("compilername"));
            jsonObject.put("settings",((JSONObject)entry.getValue()).get("settings"));
            template_json.put(""+index,jsonObject);
            index++;
        }
        JSONObject temp_jsonObject = new JSONObject();
        temp_jsonObject.put("type","selectcompileconfig");
        temp_jsonObject.put("name",selected_compile_config);
        template_json.put(""+index,temp_jsonObject);
        index++;
        templatejson.put("template",template_json);
        //((JSONArray)Main.configjson.get("templates")).remove(templatejson);
        Main.save_config();
    }
    private void list_files(String path, HashMap<String,Boolean> files) throws IOException {
        Files.list(memoryfilesystem.getPath(path)).forEach(filepath -> {
            if (Files.isDirectory(filepath)) {
                try {
                    files.put(filepath.toString().replaceFirst("/",""),true);
                    list_files(filepath.toString(),files);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                files.put(filepath.toString().replaceFirst("/",""),false);
            }
        });
    }
    private void load_template(JSONObject template_json) throws IOException {
        statuslabel.setText("Template open: "+template_json.get("name"));
        files_list.clear();
        file_content.clear();
        opened_files.clear();
        memoryfilesystem.close();
        fileEditor.close_all();
        memoryfilesystem = Jimfs.newFileSystem(Configuration.unix().toBuilder().setWorkingDirectory("/").build());
        JSONObject template_numbers = (JSONObject) template_json.get("template");
        for (int i = 0; i < template_numbers.size(); i++) {
            JSONObject indexjsonobject = (JSONObject) template_numbers.get(""+i);
            if (((String)indexjsonobject.get("type")).equalsIgnoreCase("folder")) {
                Files.createDirectory(memoryfilesystem.getPath((String) indexjsonobject.get("path")));
            } else if (((String)indexjsonobject.get("type")).equalsIgnoreCase("file")) {
                Files.createFile(memoryfilesystem.getPath((String) indexjsonobject.get("path")));
            } else if (((String)indexjsonobject.get("type")).equalsIgnoreCase("filewrite")) {
                file_content.putIfAbsent(indexjsonobject.get("path").toString().replace('\\', '/'), new stringholder());
                file_content.put(indexjsonobject.get("path").toString().replace('\\','/'),new stringholder(file_content.get(indexjsonobject.get("path").toString().replace('\\','/'))+indexjsonobject.get("text").toString()));
            } else if (((String)indexjsonobject.get("type")).equalsIgnoreCase("open")) {
                opened_files.put(((String) indexjsonobject.get("path")).replace('\\','/'),true);
            } else if (((String)indexjsonobject.get("type")).equalsIgnoreCase("compileconfig")) {
                JSONObject jsonObject = new JSONObject();
                // {"name":"Main","settings":{},"compilername":"Spwn Compiler","type":"compileconfig"}
                jsonObject.put("compilername",(indexjsonobject.get("compilername")));
                jsonObject.put("name",(indexjsonobject.get("name")));
                jsonObject.put("settings",(indexjsonobject.get("settings")));
                compileconfigs.put((indexjsonobject.get("name")),jsonObject);
            } else if (((String)indexjsonobject.get("type")).equalsIgnoreCase("selectcompileconfig")) {
                selected_compile_config=(String) indexjsonobject.get("name");
            }
        }
        templatejson = template_json;
        refresh_compileconfigs();
        compileconfigs_list.setSelectedItem(selected_compile_config);
        list_files("/");
        is_template_loaded=true;
    }
    private void list_files(String path) throws IOException {
        fileview_path.setText(path);
        fileview_content.removeAll();

        if (!(path.equals("/"))) {
            JLabel label = new JLabel("..");
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!SwingUtilities.isRightMouseButton(e)) {
                        try {
                            String output = util.get_file_parent(path);
                            if (output.equals("")) {
                                output = "/";
                            }
                            list_files(output);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
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
            fileview_content.add(label);
        }
        Files.list(memoryfilesystem.getPath(path)).forEach(file -> {
            try {
                JLabel label = new JLabel(file.getFileName().toString());
                label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            JPopupMenu popupMenu = new JPopupMenu();
                            JMenuItem deletefile = new JMenuItem("Delete file");
                            deletefile.addActionListener(e1 -> {
                                try {
                                    int output = JOptionPane.showConfirmDialog(Main.idewindow.windowframe,"Are you sure you want to delete that file?",Main.idewindow.windowframe.getTitle(),JOptionPane.YES_NO_OPTION);
                                    if (output==JOptionPane.YES_OPTION) {
                                        util.delete_filepath(file,fileEditor);
                                        file_content.remove(file.toString().replaceFirst("/",""));
                                        opened_files.remove(file.toString().replaceFirst("/",""));
                                        files_list.remove(file.toString().replace("/",""));
                                        list_files(path);
                                    }
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                            popupMenu.add(deletefile);
                            JMenuItem renamefile = new JMenuItem("Rename file");
                            renamefile.addActionListener(e1 -> {
                                String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type new file name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
                                if (output==null) {return;}
                                if (output.replace(" ","").equalsIgnoreCase("")) {
                                    JOptionPane.showMessageDialog(Main.idewindow.windowframe,"You can't have a empty name",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                try {
                                    boolean is_open = fileEditor.is_open(file.toString());
                                    fileEditor.file_deleted(file.toString());
                                    stringholder text = file_content.get(file.toString().replaceFirst("/",""));
                                    file_content.put(file.resolveSibling(output).toString().replaceFirst("/",""),text);
                                    Files.move(file,file.resolveSibling(output));
                                    if (is_open) {fileEditor.add_tab(file.resolveSibling(output).toString(),text);}
                                    save_template();
                                    list_files(path);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });
                            popupMenu.add(renamefile);
                            if (!Files.isDirectory(file)) {
                                opened_files.putIfAbsent(file.toString().replaceFirst("/",""),false);
                                JCheckBoxMenuItem toggleopenedcheckbox = new JCheckBoxMenuItem("Set opened");
                                toggleopenedcheckbox.setState(opened_files.get(file.toString().replaceFirst("/","")));
                                toggleopenedcheckbox.addActionListener(e1 -> {
                                    opened_files.put(file.toString().replaceFirst("/",""),!opened_files.get(file.toString().replaceFirst("/","")));
                                });
                                popupMenu.add(toggleopenedcheckbox);
                            }
                            popupMenu.show(Main.idewindow.windowframe,Main.idewindow.windowframe.getMousePosition().x,Main.idewindow.windowframe.getMousePosition().y);
                        } else {
                            if (Files.isDirectory(file)) {
                                try {
                                    if (path.equals("")) {
                                        list_files((label.getText()).replace("//","/"));
                                    } else {
                                        list_files((path+"/"+label.getText()).replace("//","/"));
                                    }

                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            } else {
                                try {
                                    stringholder stringHolder = file_content.get(path.replaceFirst("/","")+"/"+label.getText());
                                    fileEditor.add_tab((path+"/"+label.getText()).replace("//","/"),stringHolder);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
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
                fileview_content.add(label);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        current_path=path;
        fileview_content.updateUI();
    }
    private JMenuBar getmenubar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu filemenu = new JMenu("File");
        menuBar.add(filemenu);

        JMenuItem filemenu_close_templates = new JMenuItem("Close templates");
        filemenu.add(filemenu_close_templates);
        filemenu_close_templates.addActionListener(e -> {
            try {
                save_template();
                Main.save_config();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Main.idewindow.set_screen(new startscreen());
        });
        JMenu filemenu_open = new JMenu("Open template");
        filemenu.add(filemenu_open);

        JMenuItem copytemplate = new JMenuItem("Create copy");
        JMenuItem newtemplate = new JMenuItem("New template");
        JMenuItem renametemplate = new JMenuItem("Rename template");
        JMenuItem deletetemplate = new JMenuItem("Delete template");
        filemenu.add(copytemplate);
        filemenu.add(newtemplate);
        filemenu.add(renametemplate);
        filemenu.add(deletetemplate);
        copytemplate.setEnabled(is_template_loaded);
        renametemplate.setEnabled(is_template_loaded);
        deletetemplate.setEnabled(is_template_loaded);
        copytemplate.addActionListener(e -> {
            try {
                save_template();
                String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type new template name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
                if (output==null) {return;}
                if (output.replace(" ","").equalsIgnoreCase("")) {
                    JOptionPane.showMessageDialog(Main.idewindow.windowframe,"You can't have a empty name",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                    return;
                }
                for (Object i : (JSONArray)Main.configjson.get("templates")) {
                    if (output.equalsIgnoreCase((String) ((JSONObject)i).get("name"))) {
                        JOptionPane.showMessageDialog(Main.idewindow.windowframe,"\""+(((JSONObject)i).get("name"))+"\" template already exists",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(templatejson.toJSONString());
                jsonObject.put("name",output);
                ((JSONArray)Main.configjson.get("templates")).add(jsonObject);
                Main.save_config();
            } catch (IOException | ParseException ex) {
                ex.printStackTrace();
            }
            Main.idewindow.set_menubar(getmenubar());
        });
        renametemplate.addActionListener(e -> {
            try {
                save_template();
                String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type new template name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
                if (output==null) {return;}
                if (output.replace(" ","").equalsIgnoreCase("")) {
                    JOptionPane.showMessageDialog(Main.idewindow.windowframe,"You can't have a empty name",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                    return;
                }
                for (Object i : (JSONArray)Main.configjson.get("templates")) {
                    if (output.equalsIgnoreCase((String) ((JSONObject)i).get("name"))) {
                        JOptionPane.showMessageDialog(Main.idewindow.windowframe,"\""+(((JSONObject)i).get("name"))+"\" template already exists",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                templatejson.put("name",output);
                statuslabel.setText("Template open: "+templatejson.get("name"));
                Main.save_config();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Main.idewindow.set_menubar(getmenubar());
        });
        newtemplate.addActionListener(e -> {
            String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type template name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
            if (output==null) {return;}
            if (output.replace(" ","").equalsIgnoreCase("")) {
                JOptionPane.showMessageDialog(Main.idewindow.windowframe,"You can't have a empty name",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (Object i : (JSONArray)Main.configjson.get("templates")) {
                if (output.equalsIgnoreCase((String) ((JSONObject)i).get("name"))) {
                    JOptionPane.showMessageDialog(Main.idewindow.windowframe,"\""+(((JSONObject)i).get("name"))+"\" template already exists",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            JSONObject templatejson = new JSONObject();
            templatejson.put("name",output);
            templatejson.put("template",new JSONObject());
            ((JSONArray)Main.configjson.get("templates")).add(templatejson);
            try {
                load_template(templatejson);
                copytemplate.setEnabled(is_template_loaded);
                renametemplate.setEnabled(is_template_loaded);
                deletetemplate.setEnabled(is_template_loaded);
                Main.save_config();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            Main.idewindow.set_menubar(getmenubar());
        });
        deletetemplate.addActionListener(e -> {
            int output = JOptionPane.showConfirmDialog(Main.idewindow.windowframe,"Are you sure you want to delete \""+templatejson.get("name")+"\" template",Main.idewindow.windowframe.getTitle(),JOptionPane.YES_NO_OPTION);
            if (output==JOptionPane.YES_OPTION) {
                ((JSONArray)Main.configjson.get("templates")).remove(templatejson);
                try {
                    Main.idewindow.set_screen(new templates());
                    Main.save_config();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        for (Object i : (JSONArray)Main.configjson.get("templates")) {
            JMenuItem menuItem = new JMenuItem((String) ((JSONObject)i).get("name"));
            menuItem.addActionListener(e -> {
                try {
                    load_template(((JSONObject)i));
                    copytemplate.setEnabled(is_template_loaded);
                    renametemplate.setEnabled(is_template_loaded);
                    deletetemplate.setEnabled(is_template_loaded);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
            filemenu_open.add(menuItem);
        }
        return menuBar;
    }
}
