package me.melonsboy.spwn.ide;

import com.google.common.util.concurrent.AtomicDouble;
import me.melonsboy.spwn.ide.custom.compiler;
import me.melonsboy.spwn.ide.custom.compilers_source;
import me.melonsboy.spwn.ide.custom.iconButton;
import me.melonsboy.spwn.ide.custom.menuitem;
import me.melonsboy.spwn.ide.exceptions.compiler_name_already_exists;
import me.melonsboy.spwn.ide.exceptions.extension_already_exists;
import me.melonsboy.spwn.ide.exceptions.theme_already_exists;
import me.melonsboy.spwn.ide.themes.Theme;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The API for plugins
 */

public class API {
    // int types for button placement
    public static final int TOPPANEL_PLACEMENT = 0;

    /**
     * Adds a theme to a list
     * @param theme the theme class
     * @throws theme_already_exists If the theme name already exists in the list
     */
    public static void add_theme(Theme theme) throws theme_already_exists {
        theme_loader.add_theme(theme);
    }

    /**
     * Gets the main folder path
     * @return returns the main folder path
     */
    public static String get_mainfolder_path() {
        return Main.mainfolder_path;
    }

    /**
     * Runs an HTTP Method (it sets the proxy settings if needed)
     * @param httpMethod the http method object
     * @param atomicInteger the object for download progress (get methods only)
     * @param fileWriter the file writer to write downloaded content to the disk
     */
    public static byte[] run_http_method(HttpMethod httpMethod, AtomicInteger atomicInteger, FileWriter fileWriter) {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setSoTimeout(5000);

        if (Main.proxy_ip != null) {
            HostConfiguration hostConfiguration = httpClient.getHostConfiguration();
            hostConfiguration.setProxy(Main.proxy_ip, Main.proxy_port);
            httpClient.getState().setProxyCredentials(Main.proxy_authScope, Main.proxy_credentials);
        }
        byte[] respone = null;
        try {
            httpClient.executeMethod(httpMethod);
            if (httpMethod.getStatusCode()== HttpStatus.SC_OK) {
                if (httpMethod instanceof GetMethod && fileWriter != null) {
                    GetMethod getMethod = (GetMethod) httpMethod;
                    long total_bytes = getMethod.getResponseContentLength();
                    var ref = new Object() {
                        long current_byte = 0;
                    };
                    InputStream inputStream = getMethod.getResponseBodyAsStream();
                    new Thread(() -> {
                        if (total_bytes == -1) {return;}
                        DecimalFormat decimalFormat = new DecimalFormat("#.##");
                        while (ref.current_byte != total_bytes) {
                            atomicInteger.set((int) (Double.parseDouble(decimalFormat.format(((double) ref.current_byte)/((double)total_bytes)))*100));
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    for (int i = inputStream.read(); i != -1 ; i=inputStream.read()) {
                        fileWriter.write(i);
                        ref.current_byte++;
                    }
                    fileWriter.close();
                    atomicInteger.set(-1);
                } else {
                    respone = httpMethod.getResponseBody();
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            httpMethod.releaseConnection();
        }
        return respone;
    }

    /**
     * Adds a file extension into the file extensions list
     * @param extension the file extension string
     * @param file the file object
     * @throws extension_already_exists if the file extension already exists in the list
     */
    public static void add_file_extension(String extension, file file) throws extension_already_exists {
        filemgr.add_file_ext_instance(extension, file);
    }

    /**
     * adds a button icon to the editor window
     * @param iconButton the icon button object
     * @param plugin the plugin class
     */
    public static void add_button_icon(iconButton iconButton,Plugin plugin) {
        Main.customiconbuttons.add(iconButton);
        Main.iconButtonPluginHashMap.put(iconButton, plugin);
    }

    /**
     * Gets the compile config name
     * @param configname the compile config name
     * @return returns the compile config json
     */
    public static JSONObject get_compile_config_json(String configname) {
        if (Main.idewindow.thepanel instanceof editor) {
            return (JSONObject) ((JSONObject)((editor) Main.idewindow.thepanel).projectjson.get("compileconfigs")).get(configname);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Gets selected compile config name
     * @return returns selected compile config name
     */
    public static String get_selected_compile_config_name() {
        if (Main.idewindow.thepanel instanceof editor) {
            return ((editor) Main.idewindow.thepanel).compileconfigs.getItemAt(((editor) Main.idewindow.thepanel).compileconfigs.getSelectedIndex());
        } else {
            throw new UnsupportedOperationException();
        }
    }
    /**
     * Saves all text files (editor screen only)
     */
    public static void save_all_files() throws IOException {
        if (Main.idewindow.thepanel instanceof editor) {
            ((editor)Main.idewindow.thepanel).textEditor.save_all();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Gets the project folder
     * @return Returns the project folder, returns null if project is not open
     */
    public static String get_project_folder() {
        if (Main.idewindow.thepanel instanceof editor) {
            return ((editor)Main.idewindow.thepanel).folder_path;
        } else {
            return null;
        }
    }

    /**
     * Gets the compiler version
     * @param compiler_version the name of the compiler version
     * @return returns the compiler version json object, or null if it doesn't exist
     */
    public static JSONObject get_compiler_json(String compiler_version) {
        return (JSONObject) compile.compilerslist.get(compiler_version);
    }

    /**
     * Adds a compiler source into the program
     * @param compilersSource the compiler source class
     * @param plugin the plugin class
     */
    public static void add_compilers_source(compilers_source compilersSource,Plugin plugin) {
        Main.compilersSourcePluginHashMap.put(compilersSource, plugin);
    }

    /**
     * Adds a compiler into the compiler settings
     * @param compiler the compiler object
     * @param plugin the plugin object
     */
    public static void add_compiler(compiler compiler, Plugin plugin) throws compiler_name_already_exists {
        for (compiler i : Main.compilerArrayList) {
            if (i.compiler_name.equalsIgnoreCase(compiler.compiler_name)) {
                throw new compiler_name_already_exists(compiler.compiler_name);
            }
        }
        Main.compilerArrayList.add(compiler);
        Main.compilerPluginHashMap.put(compiler,plugin);
    }

    /**
     * Adds a tab to the bottom of the editor screen
     * @param panel the content panel
     * @param tabID the tab ID
     * @param plugin the plugin class
     * @param always_open is the tab should be always open
     */
    public static void add_bottom_tab(JPanel panel, String tabID, Plugin plugin, String tabname, ImageIcon imageIcon,boolean always_open) {
        if (Main.bottompanel_plugin_hashmap.containsKey(tabID)) {
            if (!(Main.idewindow==null)) {
                if (!(Main.idewindow.thepanel==null)) {
                    if (Main.idewindow.thepanel instanceof editor) {
                        ((editor)Main.idewindow.thepanel).bottomtabbedpane.setSelectedComponent(Main.bottompanel_tabid_panels.get(tabID));
                        ((editor)Main.idewindow.thepanel).bottomtabbedpane.setTitleAt(((editor)Main.idewindow.thepanel).bottomtabbedpane.getSelectedIndex(),tabname);
                    }
                }
            }
            return;
        }
        SwingUtilities.updateComponentTreeUI(panel);
        Main.bottompanel_plugin_hashmap.put(tabID,plugin);
        Main.bottompanel_tabid_icons.put(tabID,util.scaleImage(imageIcon,32,32));
        Main.bottompanel_tabid_panels.put(tabID,panel);
        Main.bottompanel_tabid_names.put(tabID,tabname);
        if (always_open) {
            Main.always_open_tabs.add(panel);
        }
        if (!(Main.idewindow==null)) {
            if (!(Main.idewindow.thepanel == null)) {
                if (Main.idewindow.thepanel instanceof editor) {
                    ((editor) Main.idewindow.thepanel).bottomtabbedpane.addTab(tabname, util.scaleImage(imageIcon, 32, 32), panel);
                    ((editor) Main.idewindow.thepanel).bottomtabbedpane.setSelectedComponent(panel);
                }
            }
        }
    }

    /**
     * Removes a bottom tab
     * @param tabID the tab id
     */
    public static void remove_bottom_tab(String tabID) {
        if (Main.idewindow.thepanel instanceof editor) {
            JTabbedPane tabbedPane = ((editor)Main.idewindow.thepanel).bottomtabbedpane;
            int oldindex = tabbedPane.getSelectedIndex();
            tabbedPane.setSelectedComponent(Main.bottompanel_tabid_panels.get(tabID));
            tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
            tabbedPane.setSelectedIndex(oldindex);
        }
        Main.bottompanel_plugin_hashmap.remove(tabID);
        Main.bottompanel_tabid_icons.remove(tabID);
        Main.bottompanel_tabid_names.remove(tabID);
        Main.bottompanel_tabid_panels.remove(tabID);
    }

    /**
     * Checks if the file extension exists in the list
     * @param extension the file extension string
     * @return true if it exists in the list, false if it doesn't exist in the list
     */
    public static boolean is_file_extension_exist(String extension) {
        return filemgr.is_ext_in_system(extension);
    }

    /**
     * Adds a menu item to the menu bar
     * @param menuitem the menu item object
     * path the path for the menu item, example: "File/new" would add a menu item in the new menu in the file menu
     */
    public static void add_menuitem(menuitem menuitem,Plugin plugin) {
        Main.custommenuitems.add(menuitem);
        Main.pluginsmenuhashmap.put(menuitem, plugin);
    }
}
