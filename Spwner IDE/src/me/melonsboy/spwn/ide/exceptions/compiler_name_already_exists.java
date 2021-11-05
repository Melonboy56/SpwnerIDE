package me.melonsboy.spwn.ide.exceptions;

public class compiler_name_already_exists extends Exception {
    String name;
    public compiler_name_already_exists(String compiler_name) {
        name=compiler_name;
    }

    @Override
    public String getMessage() {
        return "\""+name+"\" already exists in the compilers list";
    }
}
