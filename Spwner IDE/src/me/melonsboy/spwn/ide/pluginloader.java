package me.melonsboy.spwn.ide;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class pluginloader {
    public Plugin plugin;
    public String name;
    public String version;
    public String id;
    public String[] owners;
    public pluginloader(Plugin plugin, JSONObject jsonObject) {
        this.plugin=plugin;
        name = (String) jsonObject.get("name");
        version = (String) jsonObject.get("version");
        id=(String)jsonObject.get("id");
        owners = new String[((JSONArray)jsonObject.get("owner")).size()];
        for (int i = 0; i < ((JSONArray)jsonObject.get("owner")).size(); i++) {
            owners[i] = (String) ((JSONArray)jsonObject.get("owner")).get(i);
        }
    }
}
