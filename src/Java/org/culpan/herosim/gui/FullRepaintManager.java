package org.culpan.herosim.gui;

import javax.swing.RepaintManager;
import javax.swing.JComponent;
import java.awt.Container;

public class FullRepaintManager extends RepaintManager {
    public void addDirtyRegion(JComponent comp, int x, int y, int w, int h) {
        super.addDirtyRegion(comp,x,y,w,h);
        JComponent root = getRootJComponent(comp);
        // to avoid a recursive infinite loop
        if(comp != root) {
            super.addDirtyRegion(root,0,0,root.getWidth(),root.getHeight());
        }
    }
    public JComponent getRootJComponent(JComponent comp) {
        Container parent = comp.getParent();
        if(parent instanceof JComponent) {
            return getRootJComponent((JComponent)parent);
        }
        return comp;
    }

}
