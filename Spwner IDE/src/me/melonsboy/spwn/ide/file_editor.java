package me.melonsboy.spwn.ide;

import me.melonsboy.spwn.ide.custom.save_button;
import org.json.simple.JSONArray;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class file_editor extends JPanel {
    public JTabbedPane filetabs;
    private HashMap<String, JPanel> panelHashMap = new HashMap<>();
    private List<String> opened_files = new ArrayList<>();
    private String folder_path;
    private editor editor;
    private boolean is_template;
    private HashMap<String, Boolean> files;
    private HashMap<String,Boolean> memorytabs;

    public file_editor(HashMap<String,Boolean> files) {
        this(null,null,true);
        this.files = files;
    }

    public file_editor(String folderpath, editor editor,boolean is_template) {
        super();
        this.is_template = is_template;
        folder_path = folderpath;
        this.editor = editor;
        memorytabs = new HashMap<>();
        this.setLayout(new BorderLayout());
        filetabs = new JTabbedPane();
        filetabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        //filetabs.setUI(new customtabbedUI(Color.DARK_GRAY));
        filetabs.addChangeListener(e -> editor.update_title());
        this.add(filetabs,BorderLayout.CENTER);
    }
    public boolean is_open(String filepath) {
        return panelHashMap.containsKey(filepath);
    }
    public void check_files() {
        HashMap<String, JPanel> temphashmap = new HashMap<>(panelHashMap);

        for (Map.Entry<String, JPanel> i : temphashmap.entrySet()) {
            if (is_template) {
                if (!files.containsKey(i.getKey())) {
                    file_deleted(i.getKey());
                }
            } else {
                if (!new File(i.getKey()).exists()) {
                    if (memorytabs.containsKey(i.getKey())) {
                        if (!memorytabs.get(i.getKey())) {
                            file_deleted(i.getKey());
                        }
                    } else {
                        file_deleted(i.getKey());
                    }
                }
            }
        }
    }
    public void file_deleted(String filepath) {
        if (!(panelHashMap.get(filepath)==null)) {
            filetabs.remove(panelHashMap.get(filepath));
            panelHashMap.remove(filepath);
            opened_files.remove(filepath);
        }
    }
    public void close_all() throws IOException {
        save_all();
        filetabs.removeAll();
        panelHashMap.clear();
        opened_files.clear();
    }
    public void save_file(String filepath) {
        if (!(panelHashMap.get(filepath)==null)) {
            JPanel panel = panelHashMap.get(filepath);
            for (Component j : panel.getComponents()) {
                if (j instanceof JButton) {
                    ((JButton) j).doClick();
                }
            }
        }
    }
    public void save_all() throws IOException {
        for (Map.Entry<String, JPanel> i : panelHashMap.entrySet()) {
            JPanel panel = i.getValue();
            for (Component j : panel.getComponents()) {
                if (j instanceof save_button) {
                    ((save_button)j).doClick();
                }
            }
        }
        if (!is_template) {
            ((JSONArray)editor.projectjson.get("opened_files")).clear();
            for (String i : opened_files) {
                String replaced_i = i.replaceFirst(folder_path.replace('\\','/')+"/","");
                ((JSONArray)editor.projectjson.get("opened_files")).add(replaced_i);
            }
            FileWriter fileWriter = new FileWriter(editor.folder_path+"/"+Main.project_json_name);
            fileWriter.write(editor.projectjson.toJSONString());
            fileWriter.close();
            Main.configjson.put("middlesplit",editor.middlepanel.getDividerLocation());
            Main.configjson.put("bottomsplit",editor.bottompane.getDividerLocation());
            Main.save_config();
        }
    }
    public String get_selected_filepath() {
        filetabs.getSelectedIndex();
        return opened_files.get(filetabs.getSelectedIndex());
    }
    public void set_tab_title(String tabname,String title) {
        if (!(panelHashMap.get(tabname)==null)) {
            int selected_index = filetabs.getSelectedIndex();
            filetabs.setSelectedComponent(panelHashMap.get(tabname));
            filetabs.setTitleAt(filetabs.getSelectedIndex(),title);
            filetabs.setSelectedIndex(selected_index);
            editor.update_title();
        }
    }
    public void toggle_save_state(String filepath, String title,boolean is_saved) {
        if (!(panelHashMap.get(filepath)==null)) {
            int selected_index = filetabs.getSelectedIndex();
            filetabs.setSelectedComponent(panelHashMap.get(filepath));
            if (is_saved) {
                filetabs.setTitleAt(filetabs.getSelectedIndex(),title);
            } else {
                filetabs.setTitleAt(filetabs.getSelectedIndex(),"*"+title);
            }
            filetabs.setSelectedIndex(selected_index);
        }
    }
    public void add_tab(String filepath, JPanel panel,Icon icon) {
        filetabs.addTab(new File(filepath).getName(),icon,panel);
        editor.update_title();
    }
    public void add_memory_tab(String id,String tabname, JPanel panel) {
        if (panelHashMap.containsKey(id)) {
            filetabs.setSelectedComponent(panelHashMap.get(id));
            return;
        }
        filetabs.addTab(tabname,panel);
        panelHashMap.put(id,panel);
        memorytabs.put(id,true);
        editor.update_title();
    }
    public void add_tab(String filepath) throws IOException {
        add_tab(filepath,new stringholder());
        editor.update_title();
    }
    public void add_tab(String filepath,stringholder text) throws IOException {
        filepath = filepath.replace('\\','/');
        if (opened_files.contains(filepath)) {
            filetabs.setSelectedComponent(panelHashMap.get(filepath));
            return;
        }
        JPanel panel = filemgr.load_file(filepath,is_template,text);
        add_tab(filepath, panel, filemgr.get_icon(filepath));
        opened_files.add(filepath);
        panelHashMap.put(filepath,panel);
        filetabs.setSelectedComponent(panelHashMap.get(filepath));
        editor.update_title();
    }
}
