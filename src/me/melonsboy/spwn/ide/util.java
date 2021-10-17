package me.melonsboy.spwn.ide;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class util {
    /**
     *
     * @return the folder path of the jar file
     * @throws URISyntaxException <- idk what that exception is meant for xd
     */
    public static String get_jar_folder() throws URISyntaxException {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
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
            System.out.println("contains: "+name);
            if (defaultMutableTreeNode.getChildAt(i).equals(name)) {
                return true;
            }
        }
        return false;
    }
}
