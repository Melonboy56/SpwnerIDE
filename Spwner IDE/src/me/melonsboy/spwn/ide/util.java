package me.melonsboy.spwn.ide;

import com.sun.javafx.PlatformUtil;
import me.melonsboy.spwn.ide.custom.compiler;
import me.melonsboy.spwn.ide.custom.iconButton;
import me.melonsboy.spwn.ide.custom.menuitem;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class util {
    public static void show_compiler_config(JSONObject compilerconfigs,Object refresh_combobox_sel) {
        compiler_config.show_compiler_config(compilerconfigs,refresh_combobox_sel);
    }
    /**
     *
     * @return the folder path of the jar file
     * @throws URISyntaxException <- idk what that exception is meant for xd
     */
    public static String get_jar_folder() throws URISyntaxException {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
    }

    public static String format_string(String string, pluginloader pluginloader) {
        string = string.replace("{pluginname}",pluginloader.name);


        return string;
    }

    public static void disable_plugin(pluginloader pluginloader) {
        for (Map.Entry<menuitem,Plugin> entry : Main.pluginsmenuhashmap.entrySet()) {
            if (entry.getValue()==pluginloader.plugin) {
                Main.pluginsmenuhashmap.remove(entry.getKey());
            }
        }
        for (Map.Entry<iconButton,Plugin> entry : Main.iconButtonPluginHashMap.entrySet()) {
            if (entry.getValue()==pluginloader.plugin) {
                Main.iconButtonPluginHashMap.remove(entry.getKey());
            }
        }
        for (Map.Entry<compiler,Plugin> i : Main.compilerPluginHashMap.entrySet()) {
            if (i.getValue()==pluginloader.plugin) {
                Main.compilerArrayList.remove(i.getKey());
                Main.compilerPluginHashMap.remove(i.getKey());
            }
        }
        for (Map.Entry<String, pluginloader> i : Main.pluginHashMap.entrySet()) {
            if (i.getValue()==pluginloader) {
                Main.pluginHashMap.remove(i.getKey());
                break;
            }
        }

    }

    /**
     *
     * @return True if GeometryDash.exe is running on the system (it just checks if that file name is in the processes list)
     */
    public static boolean isGD_open() {

        return false;
    }

    /**
     * Returns the file parent
     * @param path the file path
     * @return the file parent
     */
    public static String get_file_parent(String path) {
        if (path.split("/").length==1) {
            return path;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            String[] strings = path.split("/");
            strings[strings.length-1]=null;
            for (String i : strings) {
                if (!(i==null)) {
                    stringBuilder.append(i).append("/");
                }
            }
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
            return stringBuilder.toString();
        }
    }
    /**
     * Returns the file extension for the file name/path
     * @param filename the file name (or path)
     * @return the file extension
     */
    public static String get_extension(String filename) {
        String[] split_name = filename.split("\\.");
        return split_name[split_name.length-1];
    }
    /**
     *
     */
    public static boolean can_run() {
        if (PlatformUtil.isWindows()) {
            return true;
        } else if (PlatformUtil.isLinux()) {
            return true;
        } else return PlatformUtil.isMac();
    }

    /**
     * checks if there a proxy present
     * @param test_urltext the URL to test
     * @return true if proxy exists, false if proxy doesn't exist.
     * @throws URISyntaxException if the url can't be parsed
     */
    public static void detect_and_set_proxy(String test_urltext) throws URISyntaxException {
        java.util.List<Proxy> l = ProxySelector.getDefault().select(new URI(test_urltext));
        for (Proxy proxy : l) {
            InetSocketAddress addr = (InetSocketAddress) proxy.address();

            if (addr == null) {
                System.out.println("No Proxy is detected");
            } else {
                {
                    System.out.println("Proxy type: " + proxy.type());
                    System.out.println("Proxy IP : " + addr.getHostName());
                    System.out.println("Proxy port : " + addr.getPort());
                    JPanel panel = new JPanel();
                    panel.setPreferredSize(new Dimension(480,(Main.font.getSize()*2)+20));
                    panel.setLayout(new GridLayout(2,2));
                    JLabel username_label = new JLabel("Username");
                    JLabel password_label = new JLabel("Password");
                    JTextField username_field = new JTextField();
                    JPasswordField password_field = new JPasswordField();
                    panel.add(username_label);
                    panel.add(username_field);
                    panel.add(password_label);
                    panel.add(password_field);
                    String[] strings = new String[] {"Ok","Cancel"};

                    int output = JOptionPane.showOptionDialog(Main.idewindow.windowframe,panel,"Proxy Auth",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,strings,strings[0]);
                    if (output==0) {
                        Main.proxy_ip=addr.getHostName();
                        Main.proxy_port=addr.getPort();
                        Main.proxy_credentials = new UsernamePasswordCredentials(username_field.getText(),new String(password_field.getPassword()));
                        Main.proxy_authScope = new AuthScope(Main.proxy_ip,Main.proxy_port);
                    }
                }
            }
        }
    }

    /**
     * Checks if the file can be downloaded.
     * @param jsonObject the compiler json object
     * @return
     */

    public static boolean can_download(JSONObject jsonObject) {
        if (PlatformUtil.isWindows()) {
            return jsonObject.containsKey("windows");
        } else if (PlatformUtil.isLinux()) {
            return jsonObject.containsKey("linux");
        } else if (PlatformUtil.isMac()) {
            return jsonObject.containsKey("mac");
        } else {
            return false;
        }
    }

    /**
     * code below is from https://stackoverflow.com/a/34189578
     */
    public static ImageIcon scaleImage(ImageIcon icon, int w, int h) {
        if (icon==null) {return null;}
        Image img = icon.getImage();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics();
        g.drawImage(img, 0, 0, w, h, null);
        return new ImageIcon(bi);
    }
    public static void delete_filepath(Path filepath, file_editor fileEditor) throws IOException {
        if (Files.isDirectory(filepath)) {
            Files.list(filepath).forEach(file -> {
                try {
                    delete_filepath(file, fileEditor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        fileEditor.file_deleted(filepath.toString());
        Files.delete(filepath);
    }
    public static boolean delete_file(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    delete_file(f);
                }
            }
        }
        return file.delete();
    }
    public static String to_string_path(Object[] thepath) {
        StringBuilder pathstring = new StringBuilder();
        thepath[0] = null;
        for (Object i : thepath) {
            if (i==null) {continue;}
            pathstring.append(i).append("/");
        }
        if (pathstring.length()==0) {
            return "";
        }
        pathstring.deleteCharAt(pathstring.length()-1);
        return pathstring.toString();
    }

    public static int get_longest_string_width(JComboBox<String> comboBox) {
        int longest_width = 0;
        for (int i = 0; i < comboBox.getModel().getSize(); i++) {
            int width = Main.idewindow.windowframe.getGraphics().getFontMetrics().stringWidth(comboBox.getModel().getElementAt(i));
            if (width > longest_width) {
                longest_width=width;
            }
        }
        return longest_width;
    }

    public static JTextArea get_label_area(String text) {
        JTextArea textArea = new JTextArea();
        textArea.setText(text);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBackground(UIManager.getColor("Label.background"));
        textArea.setFont(UIManager.getFont("Label.font"));
        textArea.setBorder(UIManager.getBorder("Label.border"));
        return textArea;
    }

    public static void update_ComponentTreeUI(Component component) {
        SwingUtilities.updateComponentTreeUI(component);
        if (component instanceof JComponent) {
            for (Component i : ((JComponent)component).getComponents()) {
                update_ComponentTreeUI(i);
            }
        } else if (component instanceof JDialog) {
            for (Component i : ((JDialog)component).getComponents()) {
                update_ComponentTreeUI(i);
            }
        }
    }

    public static void remove_dupes(DefaultMutableTreeNode defaultMutableTreeNode) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < defaultMutableTreeNode.getChildCount(); i++) {
            if (list.contains(defaultMutableTreeNode.getChildAt(i).toString())) {
                defaultMutableTreeNode.remove(i);
            } else {
                list.add(defaultMutableTreeNode.getChildAt(i).toString());
            }
        }
    }

    public static boolean contains_name(DefaultMutableTreeNode defaultMutableTreeNode, String name) {
        for (int i = 0; i < defaultMutableTreeNode.getChildCount(); i++) {
            if (defaultMutableTreeNode.getChildAt(i).equals(name)) {
                return true;
            }
        }
        return false;
    }
    public static pluginloader load_plugin(String plugin_path) throws IOException {
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[] {new File(plugin_path).toURI().toURL()});
            InputStream inputStream = urlClassLoader.getResourceAsStream("plugin.json");
            JSONObject pluginconfig = (JSONObject) new JSONParser().parse(new String(inputStream.readAllBytes()));
            Class pluginclass = Class.forName((String)pluginconfig.get("mainclass"),true,urlClassLoader);
            Plugin plugin = (Plugin) pluginclass.getDeclaredConstructor().newInstance();
            return new pluginloader(plugin,pluginconfig);
        } catch (ParseException | ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
