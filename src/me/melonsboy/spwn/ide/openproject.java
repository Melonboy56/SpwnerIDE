package me.melonsboy.spwn.ide;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class openproject {
    public static void openproject() throws IOException {
        JFileChooser fileChooser = new JFileChooser((String) Main.configjson.get("last_projects_dir"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int output = fileChooser.showOpenDialog(Main.idewindow.windowframe);
        if (output==JFileChooser.APPROVE_OPTION) {
            Main.configjson.put("last_project_location",fileChooser.getSelectedFile().getPath());
            if (!((JSONArray)Main.configjson.get("project_paths")).contains(fileChooser.getSelectedFile().getPath())) {
                ((JSONArray)Main.configjson.get("project_paths")).add(fileChooser.getSelectedFile().getPath());
            }
            Main.save_config();
            Main.open_project(fileChooser.getSelectedFile().getPath());
        }
    }
}
