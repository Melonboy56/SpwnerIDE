package me.melonsboy.spwn.ide.exceptions;

public class extension_already_exists extends Exception {
    String name;
    public extension_already_exists(String extension) {
        name=extension;
    }

    @Override
    public String getMessage() {
        return "\""+name+"\" already exists in the extension list";
    }
}
