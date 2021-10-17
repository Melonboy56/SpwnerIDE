package me.melonsboy.spwn.ide;

import javax.accessibility.AccessibleContext;
import java.awt.*;

public class setbackground {
    public static void setbackground(Color color, AccessibleContext accessibleContext) {
        accessibleContext.getAccessibleComponent().setBackground(color);
        for (int i = 0; i < accessibleContext.getAccessibleChildrenCount(); i++) {
            setbackground(color,accessibleContext.getAccessibleChild(i).getAccessibleContext());
        }
    }
}
