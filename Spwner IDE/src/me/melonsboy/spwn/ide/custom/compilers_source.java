package me.melonsboy.spwn.ide.custom;

import java.util.ArrayList;
import java.util.List;

public abstract class compilers_source {
    /**
     * Gets the list of the compiler list
     * @return returns the compiler list
     */
    public abstract ArrayList<String> get_list();
    public abstract void download_compiler(int index);

    /**
     * Gets the name of the compilers source
     * @return returns the compilers source name
     */
    public abstract String get_name();
}
