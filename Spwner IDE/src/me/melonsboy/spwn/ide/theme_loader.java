package me.melonsboy.spwn.ide;

import me.melonsboy.spwn.ide.exceptions.theme_already_exists;
import me.melonsboy.spwn.ide.themes.Theme;
import me.melonsboy.spwn.ide.themes.flatIntelliJlaf;
import me.melonsboy.spwn.ide.themes.flatdarculalaf;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class theme_loader {
    public static Theme selected_theme;
    private static Theme showing_theme;
    static ArrayList<Theme> arrayList = new ArrayList<>();
    static ArrayList<Theme> list_themes() {
        return arrayList;
    }

    static void load_themes() throws theme_already_exists {
        add_theme(new flatdarculalaf());
        add_theme(new flatIntelliJlaf());

        selected_theme=arrayList.get(0);
        for (Theme i : arrayList) {
            if (i.get_theme_name().equalsIgnoreCase(Main.configjson.get("theme_name").toString())) {
                selected_theme=i;
                break;
            }
        }
    }
    static void keep_theme() {
        if (!(showing_theme==null)) {
            selected_theme = showing_theme;
        }
    }

    static void apply_theme() throws UnsupportedLookAndFeelException, IOException {
        UIManager.setLookAndFeel(selected_theme.get_look_and_feel());
        SwingUtilities.updateComponentTreeUI(Main.idewindow.windowframe);
        apply_theme_to_syntaxtextarea(Main.idewindow.windowframe.getComponents());
    }
    static void show_theme(Theme theme) throws UnsupportedLookAndFeelException, IOException {
        Theme oldtheme = selected_theme;
        selected_theme=theme;
        showing_theme=theme;
        apply_theme();
        selected_theme=oldtheme;
    }
    private static void apply_theme_to_syntaxtextarea(Component[] components) throws IOException {
        for (Component i : components) {
            if (i instanceof JComponent) {
                if (i instanceof RSyntaxTextArea) {
                    try {
                        org.fife.ui.rsyntaxtextarea.Theme.load(new ByteArrayInputStream(theme_loader.selected_theme.get_rsyntaxtextarea_theme().getBytes(StandardCharsets.UTF_8))).apply((RSyntaxTextArea) i);
                        i.setFont(Main.font);
                    } catch (NullPointerException exception) {
                        exception.printStackTrace();
                    }
                }
                apply_theme_to_syntaxtextarea(((JComponent)i).getComponents());
            }
        }
    }

    public static void add_theme(Theme theme) throws theme_already_exists {
        for (Theme i : arrayList) {
            if (i.get_theme_name().equalsIgnoreCase(theme.get_theme_name())) {
                throw new theme_already_exists(theme.get_theme_name());
            }
        }
        arrayList.add(theme);
    }
}
