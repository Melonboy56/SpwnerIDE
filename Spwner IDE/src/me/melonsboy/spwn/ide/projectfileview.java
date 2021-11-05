package me.melonsboy.spwn.ide;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class projectfileview extends JPanel {
    public String selected_folder_path;
    public file_editor file_editor;
    public HashMap<String,DefaultMutableTreeNode> nodehashmap = new HashMap<>();
    private JPanel contentpanel;
    private JLabel folderpath_label;
    public String current_folder = null;
    public projectfileview(String folder_path, file_editor textEditor) throws IOException {
        super();
        selected_folder_path=folder_path;
        file_editor=textEditor;
        contentpanel = new JPanel();
        folderpath_label=new JLabel("");
        //contentpanel.setLayout(new BoxLayout(contentpanel, BoxLayout.PAGE_AXIS));
        contentpanel.setLayout(new BoxLayout(contentpanel,BoxLayout.PAGE_AXIS));
        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(contentpanel));
        this.add(folderpath_label,BorderLayout.PAGE_END);
        contentpanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popupMenu = new JPopupMenu();

                    JMenuItem new_file = new JMenuItem("New File");
                    JMenuItem new_folder = new JMenuItem("New Folder");
                    new_file.addActionListener(e1 -> {
                        String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type file name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
                        if (output==null) {
                            return;
                        }
                        File newfile = new File(current_folder+"/"+output);
                        try {
                            newfile.createNewFile();
                            update_list();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                    new_folder.addActionListener(e1 -> {
                        String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type folder name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
                        if (output==null) {
                            return;
                        }
                        try {
                            Files.createDirectories(Paths.get(current_folder+"/"+output));
                            update_list();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                    popupMenu.add(new_file);
                    popupMenu.add(new_folder);

                    popupMenu.show(Main.idewindow.windowframe,Main.idewindow.windowframe.getMousePosition().x,Main.idewindow.windowframe.getMousePosition().y);
                }
            }
        });
        list_files(folder_path,false);
    }
    public void update_list() throws IOException {
        list_files(current_folder,false);
    }
    private void list_files(String folderpath,boolean is_rightclick) throws IOException {
        if (folderpath==null) {
            return;
        }
        if (is_rightclick) {
            JPopupMenu jPopupMenu = new JPopupMenu();

            JMenuItem renamemenuitem = new JMenuItem("Rename");
            JMenuItem deletemenuitem = new JMenuItem("Delete");

            String finalFolderpath2 = folderpath;
            renamemenuitem.addActionListener(e -> {
                String output = JOptionPane.showInputDialog(Main.idewindow.windowframe,"Type new file name",Main.idewindow.windowframe.getTitle(),JOptionPane.PLAIN_MESSAGE);
                if (output==null) {return;}
                if (output.replace(" ","").equalsIgnoreCase("")) {
                    JOptionPane.showMessageDialog(Main.idewindow.windowframe,"You can't have a empty name",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    boolean is_open = file_editor.is_open(finalFolderpath2.replace('\\','/'));
                    file_editor.save_file(finalFolderpath2.replace('\\','/'));
                    file_editor.file_deleted(finalFolderpath2);
                    String output_path = new File(finalFolderpath2).getParent()+"/"+output;
                    String output_parent = new File(output_path).getParent();
                    boolean filerenameoutput = new File(finalFolderpath2).renameTo(new File(output_path));
                    if (is_open) {file_editor.add_tab(output_path);}
                    list_files(output_parent,false);
                    if (!filerenameoutput) {
                        throw new IOException();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            deletemenuitem.addActionListener(e -> {
                int output = JOptionPane.showConfirmDialog(this,"Are you sure you want to delete that file?",Main.idewindow.windowframe.getTitle(),JOptionPane.YES_NO_OPTION);
                if (output==JOptionPane.YES_OPTION) {
                    boolean deleteoutput = util.delete_file(new File(finalFolderpath2));
                    file_editor.file_deleted(finalFolderpath2.replace('\\','/'));
                    if (!deleteoutput) {
                        JOptionPane.showMessageDialog(this,"Couldn't delete the file",Main.idewindow.windowframe.getTitle(),JOptionPane.ERROR_MESSAGE);
                    }
                    try {
                        list_files(new File(finalFolderpath2).getParent(),false);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            jPopupMenu.add(renamemenuitem);
            jPopupMenu.add(deletemenuitem);

            jPopupMenu.show(this,this.getMousePosition().x,this.getMousePosition().y);

            return;
        }
        file_editor.check_files();
        folderpath = folderpath.replace('\\','/');
        File file = new File(folderpath.replace('\\','/'));
        String folderpath_text = folderpath.replace(new File(selected_folder_path).getParent().replace('\\','/'),"");

        if (!file.isDirectory()) {
            file_editor.add_tab(folderpath);
            return;
        }

        folderpath_label.setText(folderpath_text);
        current_folder=folderpath;
        contentpanel.removeAll();
        File[] files = file.listFiles();
        assert files != null;
        if (!(folderpath.replaceAll("\\\\","/").equalsIgnoreCase(selected_folder_path.replaceAll("\\\\","/")))) {
            JLabel label = new JLabel("..");
            String finalFolderpath = folderpath;
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        list_files(new File(finalFolderpath +"/"+label.getText()).getCanonicalPath(),SwingUtilities.isRightMouseButton(e));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    e.getComponent().setForeground(Color.GREEN);
                    e.getComponent().repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    e.getComponent().setForeground(null);
                    e.getComponent().repaint();
                }
            });
            contentpanel.add(label);
        }
        for (File i : files) {
            JLabel label = new JLabel(i.getName());
            String finalFolderpath1 = folderpath;
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        File file1 = new File(finalFolderpath1 +"/"+label.getText());
                        if (!file1.exists()) {
                            contentpanel.remove(e.getComponent());
                            contentpanel.updateUI();
                            file_editor.file_deleted(finalFolderpath1+"/"+label.getText());
                            return;
                        }
                        list_files(file1.getCanonicalPath(),SwingUtilities.isRightMouseButton(e));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    e.getComponent().setForeground(Color.GREEN);
                    e.getComponent().repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    e.getComponent().setForeground(null);
                    e.getComponent().repaint();
                }
            });
            contentpanel.add(label);
        }
        contentpanel.updateUI();
    }

    @Override
    public void updateUI() {
        try {
            list_files(current_folder,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.updateUI();
    }
}
