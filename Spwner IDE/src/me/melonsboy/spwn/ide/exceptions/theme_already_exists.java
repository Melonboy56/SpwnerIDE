package me.melonsboy.spwn.ide.exceptions;

public class theme_already_exists extends Exception {
    String name;
    public theme_already_exists(String themename) {
        name=themename;
    }

    @Override
    public String getMessage() {
        return "\""+name+"\" already exists in the theme list";
    }
}
