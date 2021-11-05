package me.melonsboy.spwn.ide;

import com.google.common.io.Files;
import me.melonsboy.spwn.ide.custom.compiler;
import me.melonsboy.spwn.ide.custom.compilers_source;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Main {
    static String proxy_ip = null;
    static int proxy_port = 0;
    static UsernamePasswordCredentials proxy_credentials;
    static AuthScope proxy_authScope;

    static String mainfolder_path;
    static JSONObject configjson;
    public static window idewindow;
    public static Font font;
    static final String program_name = "Spwner IDE (v1.1 Alpha)";
    static final String project_json_name = "SpwnProject.spwner";
    static ArrayList<String> fontnames = new ArrayList<>();
    static HashMap<String,pluginloader> pluginHashMap = new HashMap<>();
    static ArrayList<me.melonsboy.spwn.ide.custom.menuitem> custommenuitems = new ArrayList<>();
    static HashMap<me.melonsboy.spwn.ide.custom.menuitem, Plugin> pluginsmenuhashmap = new HashMap<>();
    static ArrayList<me.melonsboy.spwn.ide.custom.iconButton> customiconbuttons = new ArrayList<>();
    static HashMap<me.melonsboy.spwn.ide.custom.iconButton, Plugin> iconButtonPluginHashMap = new HashMap<>();
    static HashMap<String, Plugin> bottompanel_plugin_hashmap = new HashMap<>();
    static HashMap<String,ImageIcon> bottompanel_tabid_icons = new HashMap<>();
    static HashMap<String,JPanel> bottompanel_tabid_panels = new HashMap<>();
    static HashMap<String,String> bottompanel_tabid_names = new HashMap<>();
    static HashMap<compiler,Plugin> compilerPluginHashMap = new HashMap<>();
    static ArrayList<JPanel> always_open_tabs = new ArrayList<>();
    public static ArrayList<compiler> compilerArrayList = new ArrayList<>();
    static HashMap<compilers_source,Plugin> compilersSourcePluginHashMap = new HashMap<>();

    static void disable_plugins() {
        for (Map.Entry<String,pluginloader> i : pluginHashMap.entrySet()) {
            i.getValue().plugin.onDisable();
        }
    }

    static void setup_program() throws IOException, ParseException {
        // creates the files before loading the rest the program (if the files don't exist)
        File file = new File(mainfolder_path+"/config.json");
        if (!file.exists()) {
            file.createNewFile();
            JSONObject configjson = new JSONObject();
            configjson.put("project_paths",new JSONArray());
            configjson.put("fontsize",25);
            configjson.put("last_project_location",null);
            configjson.put("last_projects_dir", (System.getProperty("user.home")+"\\SpwnerProjects").replaceAll("\\\\","/"));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("assets/templates.json")));
            configjson.put("templates",new JSONParser().parse(bufferedReader));
            configjson.put("bottomsplit",500);
            configjson.put("middlesplit",400);
            configjson.put("open_project_on_startup",true);
            configjson.put("open_changelog_message",true);
            configjson.put("mainwindow_width",900);
            configjson.put("mainwindow_height",600);
            configjson.put("mainwindow_is_maxed",false);
            configjson.put("theme_name","");
            configjson.put("proxy_detection_mode",0);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(configjson.toJSONString());
            fileWriter.close();
        } else {
            if (file.isDirectory()) {
                JOptionPane.showMessageDialog(null,"Error: config.json is a folder, not a file","Startup error",JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
    }
    static void save_config() throws IOException {
        File file = new File(mainfolder_path+"/config.json");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(configjson.toJSONString());
        fileWriter.close();
    }
    private static void load_plugins(String pluginsfolder) throws IOException {
        if (!new File(pluginsfolder).isDirectory()) {
            new File(pluginsfolder).mkdirs();
        }
        for (File i : new File(pluginsfolder).listFiles()) {
            pluginloader pluginloader = util.load_plugin(i.getPath());
            if (!(pluginloader==null)) {
                pluginHashMap.put(pluginloader.id,pluginloader);
                try {
                    pluginloader.plugin.onEnable();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
    private static void load_plugins() throws Exception {
        load_plugins(mainfolder_path+"/plugins");
    }
    public static void start_and_open_project(String projectpath) throws Exception {
        create_window();
        if (!open_project(projectpath)) {
            idewindow.set_screen(new startscreen());
        }
        check_proxy();
    }
    private static void open_project() {
        if ((boolean)configjson.get("open_project_on_startup")) {
            if (!open_project((String) configjson.get("last_project_location"))) {
                idewindow.set_screen(new startscreen());
            }
        } else {
            idewindow.set_screen(new startscreen());
        }
    }
    private static void check_proxy() throws URISyntaxException {
        if (Integer.parseInt(configjson.get("proxy_detection_mode").toString()) != 0) {
            util.detect_and_set_proxy("https://www.google.com");
        }
    }

    /**
     * Spwner IDE Alpha version 1.1
     * By Melonsboy
     */
    public static void start_program() throws Exception {
        create_window();
        open_project();
    }

    public static void create_window() throws Exception {
        if (!util.can_run()) {
            JOptionPane.showMessageDialog(null,"Your operating system is not supported.","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("java.net.useSystemProxies","true");
        Collections.addAll(fontnames, GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        mainfolder_path = Paths.get(System.getProperty("user.home"),".SpwnerIDE").toString();
        File mainfolder_file = new File(mainfolder_path);
        if (!mainfolder_file.exists()) {
            if (!mainfolder_file.mkdirs()) {
                throw new IOException("Could not create folder");
            }
        }
        if (new File(util.get_jar_folder()+"/config.json").exists()) {
            if (new File(mainfolder_path+"/config.json").exists()) {
                if (!new File(mainfolder_path+"/config.json").delete()) {
                    throw new IOException("Could not delete file");
                }
            }
            Files.move(new File(util.get_jar_folder()+"/config.json"),new File(mainfolder_path+"/config.json"));
        }
        setup_program();
        configjson = (JSONObject) new JSONParser().parse(new FileReader(mainfolder_path+"/config.json"));
        load_plugins();
        theme_loader.load_themes();
        font = new Font(new JLabel().getFont().getFontName(),Font.BOLD,Integer.parseInt(configjson.get("fontsize").toString()));
        compile.load_compilers_list();
        idewindow = new window(program_name);
        idewindow.set_size(900,600);
        if ((configjson.get("mainwindow_width")!=null && configjson.get("mainwindow_height")!=null)) {
            idewindow.set_size(Integer.parseInt(configjson.get("mainwindow_width").toString()),Integer.parseInt(configjson.get("mainwindow_height").toString()));
        }
        if ((boolean)configjson.get("mainwindow_is_maxed")) {
            idewindow.windowframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        idewindow.windowframe.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!(idewindow.thepanel==null)) {
                    idewindow.thepanel.updateUI();
                }
            }
        });
    }
    static void add_recent_project_path(String path) {

    }
    static void remove_recent_project_path(String path) {

    }

    public static void main(String[] args) throws Exception {
        start_program();
    }
    static boolean open_project(String folderpath) {
        File file = new File(folderpath+"/"+project_json_name);
        if (!file.exists()) {
            return false;
        }
        try {
            FileReader fileReader = new FileReader(folderpath+"/"+project_json_name);
            JSONObject projectjson = (JSONObject) new JSONParser().parse(fileReader);
            fileReader.close();
            String projectname = (String) projectjson.get("projectname");
            JSONArray opened_files = (JSONArray) projectjson.get("opened_files");
            if (projectname==null || opened_files==null) {throw new NullPointerException();}
            idewindow.set_screen(new editor(projectjson,folderpath));
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(idewindow.windowframe,e.getMessage(),idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return true;
        }
    }
}
