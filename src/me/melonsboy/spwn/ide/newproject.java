package me.melonsboy.spwn.ide;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class newproject {
    public static void newproject() throws IOException {
        JPanel newprojectpanel = new JPanel();
        newprojectpanel.setLayout(null);
        newprojectpanel.setPreferredSize(new Dimension(500,400));
        String projectname = "untitled";
        JPanel projectnamepanel = new JPanel();
        JLabel projectnamepanel_label = new JLabel("Project name");
        projectnamepanel_label.setPreferredSize(new Dimension(190,Main.font.getSize()));
        projectnamepanel_label.setForeground(Color.WHITE);
        projectnamepanel_label.setFont(Main.font);
        JTextField projectnamepanel_input = new JTextField(projectname);
        projectnamepanel_input.setPreferredSize(new Dimension(300,Main.font.getSize()+10));
        projectnamepanel_input.setFont(Main.font);
        projectnamepanel.add(projectnamepanel_label);
        projectnamepanel.add(projectnamepanel_input);
        projectnamepanel.setBounds(0,(Main.font.getSize()+10),500,Main.font.getSize()+15);
        newprojectpanel.add(projectnamepanel);

        JPanel projectlocationpanel = new JPanel();
        JLabel projectlocationpanel_label = new JLabel("Project location");
        projectlocationpanel_label.setPreferredSize(new Dimension(190,Main.font.getSize()));
        projectlocationpanel_label.setForeground(Color.WHITE);
        projectlocationpanel_label.setFont(Main.font);
        if (Main.configjson.get("last_projects_dir")==null) {
            Main.configjson.put("last_projects_dir", (System.getProperty("user.home")+"\\SpwnerProjects").replaceAll("\\\\","/"));
            Main.save_config();
        }
        JTextField projectlocationpanel_input = new JTextField(Main.configjson.get("last_projects_dir").toString().replace('\\','/')+"/"+projectname);
        projectlocationpanel_input.setPreferredSize(new Dimension(300,Main.font.getSize()+10));
        projectlocationpanel_input.setFont(Main.font);
        projectlocationpanel.add(projectlocationpanel_label);
        projectlocationpanel.add(projectlocationpanel_input);
        projectlocationpanel.setBounds(0,(Main.font.getSize()+10)*2,500,Main.font.getSize()+15);
        newprojectpanel.add(projectlocationpanel);

        JButton browse_project_location = new JButton("Browse Project location");
        browse_project_location.setFont(Main.font);
        browse_project_location.setForeground(Color.WHITE);
        browse_project_location.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser((String) Main.configjson.get("last_projects_dir"));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setMultiSelectionEnabled(false);
            int output = fileChooser.showSaveDialog(Main.idewindow.windowframe);
            if (output==JFileChooser.APPROVE_OPTION) {
                projectlocationpanel_input.setText(fileChooser.getSelectedFile().getPath());
            }
        });
        browse_project_location.setBounds(0,(Main.font.getSize()+10)*3,500,Main.font.getSize()+15);
        newprojectpanel.add(browse_project_location);

        JLabel templates_label = new JLabel("Templates");
        templates_label.setFont(Main.font);
        templates_label.setForeground(Color.WHITE);
        templates_label.setBounds(0,(Main.font.getSize()+10)*4,200,Main.font.getSize()+15);
        newprojectpanel.add(templates_label);

        HashMap<Integer,JSONObject> templates_hashmap = new HashMap<>();
        JComboBox<String> templates_combobox = new JComboBox<>();
        JSONObject nonejsonobject = new JSONObject();
        nonejsonobject.put("name","None");
        nonejsonobject.put("template",new JSONObject());
        templates_hashmap.put(-1,nonejsonobject);
        templates_combobox.addItem((String) nonejsonobject.get("name"));
        for (int i = 0; i < ((JSONArray)Main.configjson.get("templates")).size(); i++) {
            templates_combobox.addItem((String) ((JSONObject)((JSONArray)Main.configjson.get("templates")).get(i)).get("name"));
            templates_hashmap.put(i, (JSONObject) ((JSONArray) Main.configjson.get("templates")).get(i));
        }
        templates_combobox.setFont(Main.font);
        templates_combobox.setForeground(Color.WHITE);
        templates_combobox.setBounds(200,(Main.font.getSize()+11)*4,300,Main.font.getSize()+15);
        newprojectpanel.add(templates_combobox);

        String[] options = new String[] {"Create Project", "Cancel"};
        int output = JOptionPane.showOptionDialog(Main.idewindow.windowframe,newprojectpanel,Main.idewindow.windowframe.getTitle(),JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
        if (output==0) {
            Files.createDirectories(Paths.get(new File(projectlocationpanel_input.getText()).getPath()));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("projectname",projectnamepanel_input.getText());
            jsonObject.put("opened_files",new JSONArray());
            apply_template(new File(projectlocationpanel_input.getText()).getPath(),templates_hashmap.get(templates_combobox.getSelectedIndex()-1),jsonObject);
            FileWriter fileWriter = new FileWriter(new File(projectlocationpanel_input.getText()).getPath()+"/project.json");
            fileWriter.write(jsonObject.toJSONString());
            fileWriter.close();
            Main.configjson.put("last_projects_dir",new File(projectlocationpanel_input.getText()).getParent());
            Main.configjson.put("last_project_location",new File(projectlocationpanel_input.getText()).getPath());
            ((JSONArray)Main.configjson.get("project_paths")).add(new File(projectlocationpanel_input.getText()).getPath());
            Main.save_config();
            Main.open_project(new File(projectlocationpanel_input.getText()).getPath());
        }
    }
    private static void apply_template(String folderpath, JSONObject template_json,JSONObject projectjson) throws IOException {
        JSONObject template_numbers = (JSONObject) template_json.get("template");
        for (int i = 0; i < template_numbers.size(); i++) {
            JSONObject indexjsonobject = (JSONObject) template_numbers.get(""+i);
            if (((String)indexjsonobject.get("type")).equalsIgnoreCase("folder")) {
                Files.createDirectories(Paths.get(folderpath+"/"+ indexjsonobject.get("path")));
            } else if (((String)indexjsonobject.get("type")).equalsIgnoreCase("file")) {
                boolean output = new File(folderpath+"/"+indexjsonobject.get("path")).createNewFile();
            } else if (((String)indexjsonobject.get("type")).equalsIgnoreCase("filewrite")) {
                FileWriter fileWriter = new FileWriter(folderpath+"/"+indexjsonobject.get("path"),true);
                fileWriter.write((String) indexjsonobject.get("text"));
                fileWriter.close();
            } else if (((String)indexjsonobject.get("type")).equalsIgnoreCase("open")) {
                ((JSONArray)projectjson.get("opened_files")).add(((String)indexjsonobject.get("path")).replace('\\','/'));
            }
        }
    }
}
