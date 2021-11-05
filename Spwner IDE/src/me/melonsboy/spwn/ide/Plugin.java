package me.melonsboy.spwn.ide;

import me.melonsboy.spwn.ide.custom.compiler;
import me.melonsboy.spwn.ide.custom.compilers_source;
import me.melonsboy.spwn.ide.custom.iconButton;
import me.melonsboy.spwn.ide.custom.menuitem;
import me.melonsboy.spwn.ide.exceptions.compiler_name_already_exists;
import me.melonsboy.spwn.ide.exceptions.extension_already_exists;
import me.melonsboy.spwn.ide.exceptions.theme_already_exists;
import me.melonsboy.spwn.ide.themes.Theme;
import org.apache.commons.httpclient.HttpMethod;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class Plugin {
    /**
     * This function is called when the plugin is being enabled
     */
    public void onEnable() {

    }

    /**
     * This function is called when the plugin is being disabled
     */
    public void onDisable() {

    }

    protected final String get_project_folder() {
        return API.get_project_folder();
    }
    /**
     * Gets the compile config name
     * @param configname the compile config name
     * @return returns the compile config json
     */
    protected final JSONObject get_compile_config_json(String configname) {
        return API.get_compile_config_json(configname);
    }

    /**
     * Gets the compiler version
     * @param compiler_version the name of the compiler version
     * @return returns the compiler version json object, or null if it doesn't exist
     */
    protected final JSONObject get_compiler_json(String compiler_version) {
        return API.get_compiler_json(compiler_version);
    }

    /**
     * Adds a compiler source into the program
     * @param compilersSource the compiler source class
     */
    protected final void add_compilers_source(compilers_source compilersSource) {
        API.add_compilers_source(compilersSource,this);
    }


    /**
     * Gets selected compile config name
     * @return returns selected compile config name
     */
    protected final String get_selected_compile_config_name() {
        return API.get_selected_compile_config_name();
    }

    /**
     * Saves all text files (editor screen only)
     */
    protected final void save_all_files() throws IOException {
        API.save_all_files();
    }

    /**
     * Adds a tab to the bottom of the editor screen
     * @param panel the content panel
     * @param tabID the tab ID
     */
    protected final void add_bottom_tab(JPanel panel, String tabID, String tabname, ImageIcon imageIcon,boolean always_open) {
        API.add_bottom_tab(panel,tabID,this,tabname,imageIcon,always_open);
    }

    /**
     * Runs an HTTP Method (it sets the proxy settings if needed)
     * @param httpMethod the http method object
     * @param atomicInteger the object for download progress (get methods only)
     * @param fileWriter the file writer to write downloaded content to the disk
     */
    protected final byte[] run_http_method(HttpMethod httpMethod, AtomicInteger atomicInteger, FileWriter fileWriter) {
        return API.run_http_method(httpMethod, atomicInteger, fileWriter);
    }

    /**
     * Gets the main folder path
     * @return returns the main folder path
     */
    protected final String get_mainfolder_path() {
        return API.get_mainfolder_path();
    }

    /**
     * Removes a bottom tab
     * @param tabID the tab id
     */
    protected final void remove_bottom_tab(String tabID) {
        API.remove_bottom_tab(tabID);
    }

    /**
     * Adds a theme to a list
     * @param theme
     * @throws theme_already_exists If the theme name already exists in the list
     */
    protected final void add_theme(Theme theme) throws theme_already_exists {
        API.add_theme(theme);
    }

    /**
     * Adds a file extension into the file extensions list
     * @param extension the file extension string
     * @param file the file object
     * @throws extension_already_exists if the file extension already exists in the list
     */
    protected final void add_file_extension(String extension, file file) throws extension_already_exists {
        API.add_file_extension(extension,file);
    }
    protected final void add_button_icon(iconButton iconButton) {
        API.add_button_icon(iconButton,this);
    }

    /**
     * Checks if the file extension exists in the list
     * @param extension the file extension string
     * @return true if it exists in the list, false if it doesn't exist in the list
     */
    protected final boolean is_file_extension_exist(String extension) {
        return API.is_file_extension_exist(extension);
    }

    /**
     * Adds a menu item to the menu bar
     * @param menuitem the menu item object
     * path the path for the menu item, example: "File/new" would add a menu item in the new menu in the file menu
     */
    protected final void add_menuitem(menuitem menuitem) {
        API.add_menuitem(menuitem,this);
    }
    /**
     * Adds a compiler into the compiler settings
     * @param compiler the compiler object
     */
    protected final void add_compiler(compiler compiler) throws compiler_name_already_exists {
        API.add_compiler(compiler,this);
    }
}
