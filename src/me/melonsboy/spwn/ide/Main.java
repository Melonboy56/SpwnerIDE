package me.melonsboy.spwn.ide;

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

public class Main {
    private static String jarfolder_path;
    public static JSONObject configjson;
    public static window idewindow;
    private static int screen_status = 0;
    public static Font font;

    private static void setup_program() throws IOException, ParseException {
        // creates the files before loading the rest the program (if the files don't exist)
        File file = new File(jarfolder_path+"\\config.json");
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
    public static void save_config() throws IOException {
        File file = new File(jarfolder_path+"\\config.json");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(configjson.toJSONString());
        fileWriter.close();
    }

    /**
     * I think this program was created around 8/21/2021 (MM/DD/YYYY)
     * And finished on 09/11/2021 (MM/DD/YYYY)
     * So
     */
    public static void main(String[] args) throws URISyntaxException, IOException, ParseException {
        jarfolder_path = util.get_jar_folder();
        setup_program();
        configjson = (JSONObject) new JSONParser().parse(new FileReader(jarfolder_path+"/config.json"));
        font = new Font(new JLabel().getFont().getFontName(),Font.BOLD,Integer.parseInt(configjson.get("fontsize").toString()));
        idewindow = new window();
        idewindow.set_size(900,600);
        idewindow.set_screen(new startscreen());
        idewindow.windowframe.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                idewindow.thepanel.updateUI();
            }
        });
        if ((boolean)configjson.get("open_project_on_startup")) {
            open_project((String) configjson.get("last_project_location"));
        }
    }

    public static String getJarfolder_path() {
        return jarfolder_path.replaceAll("\\\\","/");
    }
    public static boolean open_project(String folderpath) {
        File file = new File(folderpath+"\\project.json");
        if (!file.exists()) {
            return false;
        }
        try {
            FileReader fileReader = new FileReader(folderpath+"/project.json");
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
