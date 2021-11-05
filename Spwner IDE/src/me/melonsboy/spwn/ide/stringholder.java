package me.melonsboy.spwn.ide;

import javax.swing.*;

public class stringholder {
    public String data = "";
    public stringholder() {

    }
    public stringholder(String text) {
        data=text;
    }

    @Override
    public String toString() {
        return data;
    }
}
