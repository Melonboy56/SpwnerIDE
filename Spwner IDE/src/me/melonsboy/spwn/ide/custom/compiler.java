package me.melonsboy.spwn.ide.custom;

import org.json.simple.JSONObject;

import javax.swing.*;

public class compiler extends JPanel {
    public String compiler_name = "A compiler settings example";
    public compiler() {
        super();
    }
    public compiler(String name) {
        this();
        compiler_name=name;
    }
    public void compile(JTextArea compileconsoletextarea,JSONObject settings) {

    }
    public JSONObject get_default_settings() {
        JSONObject jsonObject = new JSONObject();
        return jsonObject;
    }
    public void save_settings(JSONObject settingsjson) {

    }
    public void load_settings(JSONObject settingsjson) {

    }
}
