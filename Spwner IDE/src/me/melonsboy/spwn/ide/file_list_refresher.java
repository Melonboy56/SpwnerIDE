package me.melonsboy.spwn.ide;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;

public class file_list_refresher extends Thread {
    private boolean running=true;
    private File[] files;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private int file_count = -1;
    public file_list_refresher(DefaultMutableTreeNode treeNode, File[] files, DefaultTreeModel treeModel) {
        root=treeNode;
        this.files = files;
        this.treeModel = treeModel;
    }
    public void stop_thread() {
        running=false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                for (int i = 0; i < root.getChildCount(); i++) {
                    if (root.getChildAt(i).getChildCount()==0) {
                        root.remove(i);
                    }
                }
                for (File file : files) {
                    if (!file.isDirectory()) {
                        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file.getName());
                        root.add(childNode);
                    }
                }
                treeModel.reload();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
