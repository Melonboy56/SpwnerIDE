package me.melonsboy.spwn.ide;

import com.sun.javafx.PlatformUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

public class compile{
    public static JSONObject compilerslist;
    static String github_repo_url = "https://raw.githubusercontent.com/Melonboy56/files_for_scripts/main/";
    public static void download_file(String download_url, String download_location, AtomicInteger atomicInteger) throws IOException {
        URL url = new URL(download_url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.connect();
        long length = con.getContentLengthLong();
        InputStream inputStream = con.getInputStream();
        if (!new File(download_location).getParentFile().mkdirs()) {
            System.err.println("Warning: File.mkdirs returned false");
        }
        FileWriter fileWriter = new FileWriter(download_location, StandardCharsets.ISO_8859_1);
        long count = 0;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        boolean do_continue = true;
        for (int i = 0; i != -1; i = inputStream.read()) {
            if (do_continue) {
                do_continue=false;
                continue;
            }
            count++;
            atomicInteger.set((int) (Double.parseDouble(decimalFormat.format(((double) count)/((double)length)))*100));
            //System.out.println("len: "+length+", count: "+count+", byte: "+i+", percent: "+ (Double.parseDouble(decimalFormat.format(((double) count)/((double)length)))*100));
            fileWriter.write(i);
        }
        fileWriter.close();
        atomicInteger.set(101);
        //System.out.println(Arrays.toString(bytes));
    }
    public static JSONObject download_compilers_list() throws IOException, ParseException {
        URL url = new URL(github_repo_url+"files.json");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.connect();
        String output = new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining("\n"));
        JSONObject onlinecompiler_list = (JSONObject) ((JSONObject) new JSONParser().parse(output)).get("spwn");
        return onlinecompiler_list;
    }
    private static void create_compilers_list() throws IOException {
        if (!new File(Main.mainfolder_path+"/compiler_list.json").exists()) {
            FileWriter fileWriter = new FileWriter(Main.mainfolder_path+"/compiler_list.json");
            fileWriter.write("{}");
            fileWriter.close();
        }
    }
    static void save_compilers_list() throws IOException {
        FileWriter fileWriter = new FileWriter(Main.mainfolder_path+"/compiler_list.json");
        fileWriter.write(compilerslist.toJSONString());
        fileWriter.close();
    }
    static String choose_compiler_path(JDialog dialog,int selectionmode) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(selectionmode);
        int output = fileChooser.showSaveDialog(dialog);
        if (!(output==JFileChooser.APPROVE_OPTION)) {
            return null;
        }
        return fileChooser.getSelectedFile().getPath();
    }
    static void load_compilers_list() throws IOException, ParseException {
        create_compilers_list(); // creates one if it doesn't exist
        //download_compilers_list();
        FileReader fileReader = new FileReader(Main.mainfolder_path+"/compiler_list.json");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String compiler_list_json_string = bufferedReader.lines().collect(Collectors.joining("\n"));
        compilerslist = (JSONObject) new JSONParser().parse(compiler_list_json_string);
    }
    static void refresh_compiler_list(JPanel listpanel,JDialog dialog) {
        int count = 0;
        listpanel.removeAll();
        for (Object i : compilerslist.entrySet()) {
            AtomicBoolean is_config_removed = new AtomicBoolean(false);
            Map.Entry entry = (Map.Entry) i;
            final String[] keytext = {entry.getKey().toString()};
            JLabel label = new JLabel(keytext[0],JLabel.CENTER);
            //label.setBounds(5,(count[0] *(Main.font.getSize()+10)),dialog.getWidth()-30,Main.font.getSize()+10);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        JMenuItem removeconfig = new JMenuItem("Remove compiler configuration");
                        removeconfig.addActionListener(e1 -> {
                            int output = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to remove the compiler configuration?", "Confirm remove", JOptionPane.YES_NO_OPTION);
                            if (output == JOptionPane.YES_OPTION) {
                                compilerslist.remove(entry.getKey());
                                listpanel.remove(label);
                                is_config_removed.set(true);
                                listpanel.updateUI();
                                try {
                                    save_compilers_list();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                        popupMenu.add(removeconfig);
                        popupMenu.show(dialog,dialog.getMousePosition().x,dialog.getMousePosition().y);
                    } else {
                        JDialog dialog1 = new JDialog(dialog, "Compiler configuration", true);
                        dialog1.setSize(700, 500);
                        dialog1.setLayout(new BorderLayout());
                        JButton remove_compiler_config = new JButton("Remove compiler configuration");
                        remove_compiler_config.addActionListener(e1 -> {
                            int output = JOptionPane.showConfirmDialog(dialog1, "Are you sure you want to remove the compiler configuration?", "Confirm remove", JOptionPane.YES_NO_OPTION);
                            if (output == JOptionPane.YES_OPTION) {
                                compilerslist.remove(entry.getKey());
                                listpanel.remove(label);
                                is_config_removed.set(true);
                                dialog1.setVisible(false);
                                listpanel.updateUI();
                            }
                        });
                        dialog1.add(remove_compiler_config, BorderLayout.PAGE_END);
                        JPanel middlepanel = new JPanel();
                        JPanel namepanel = new JPanel();
                        JLabel namepanel_label = new JLabel("Name");
                        namepanel_label.setPreferredSize(new Dimension(200, Main.font.getSize() + 10));
                        namepanel.add(namepanel_label);
                        JTextField namepanel_input = new JTextField();
                        namepanel_input.setPreferredSize(new Dimension(dialog1.getWidth() - namepanel_label.getPreferredSize().width - 40, Main.font.getSize() + 10));
                        namepanel.add(namepanel_input);
                        middlepanel.add(namepanel);

                        JPanel pathpanel = new JPanel();
                        JLabel pathpanel_label = new JLabel("Compiler path");
                        pathpanel_label.setPreferredSize(new Dimension(200, Main.font.getSize() + 10));
                        pathpanel.add(pathpanel_label);
                        JTextField pathpanel_input = new JTextField();
                        pathpanel_input.setPreferredSize(new Dimension(dialog1.getWidth() - pathpanel_label.getPreferredSize().width - 40, Main.font.getSize() + 10));
                        pathpanel.add(pathpanel_input);
                        middlepanel.add(pathpanel);

                        JButton choose_compiler_path = new JButton("Choose compiler path");
                        choose_compiler_path.setPreferredSize(new Dimension(dialog1.getWidth() - 20, Main.font.getSize() + 15));
                        choose_compiler_path.addActionListener(e1 -> {
                            choose_compiler_path(dialog1,JFileChooser.FILES_ONLY);
                        });
                        middlepanel.add(choose_compiler_path);
                        namepanel_input.setText(keytext[0]);
                        pathpanel_input.setText(((JSONObject) entry.getValue()).get("path").toString());

                        dialog1.add(middlepanel);
                        dialog1.setResizable(false);
                        dialog1.setVisible(true);
                        try {
                            save_compilers_list();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if (is_config_removed.get()) {
                            return;
                        }
                        String replacetext = namepanel_input.getText();
                        if (compilerslist.containsKey(namepanel_input.getText())) {
                            if (!(entry.getKey().toString().equalsIgnoreCase(namepanel_input.getText()))) {
                                JOptionPane.showMessageDialog(dialog1, "That compiler configuration already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            replacetext = entry.getKey().toString();
                        } else {
                            label.setText(namepanel_input.getText());
                            keytext[0] = namepanel_input.getText();
                        }

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("path", pathpanel_input.getText());
                        compilerslist.remove(entry.getKey());
                        compilerslist.put(replacetext, jsonObject);
                        entry.setValue(jsonObject);

                        label.setForeground(null);
                        label.updateUI();
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    label.setForeground(Color.GREEN);
                    label.updateUI();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    label.setForeground(null);
                    label.updateUI();
                }
            });
            listpanel.add(label);
            count++;
        }
        listpanel.updateUI();
    }
    static void show_compiler_list() throws FileNotFoundException {
        JDialog dialog = new JDialog(Main.idewindow.windowframe,"Compiler list",true);
        dialog.setSize(700,500);
        dialog.setLayout(new BorderLayout());
        JLabel titlelabel = new JLabel("Compiler list",JLabel.CENTER);
        titlelabel.setOpaque(true);
        dialog.add(titlelabel,BorderLayout.PAGE_START);
        JButton install_compiler_button = new JButton("Download a compiler");
        install_compiler_button.addActionListener(e -> {
            JDialog downloading_content_dialog = new JDialog(dialog,"Downloading compilers list",true);
            downloading_content_dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            downloading_content_dialog.setResizable(false);
            downloading_content_dialog.setSize(400,Main.font.getSize()*5);
            JLabel downloading_content_dialog_label = new JLabel("Please wait",JLabel.CENTER);
            downloading_content_dialog.add(downloading_content_dialog_label);
            AtomicBoolean download_completed = new AtomicBoolean(false);
            DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
            AtomicReference<JSONObject> jsonObject = new AtomicReference<>();
            new Thread(() -> {
                try {
                    jsonObject.set(download_compilers_list());
                    ArrayList<String> arrayList = new ArrayList<>();
                    for (Object i : jsonObject.get().keySet()) {
                        JSONObject jsonObject1 = (JSONObject) jsonObject.get().get(i);
                        if (util.can_download((JSONObject) jsonObject1.get("path"))) {
                            arrayList.add(i.toString());
                        }
                    }
                    Collections.sort(arrayList);
                    for (String i : arrayList) {
                        comboBoxModel.addElement(i);
                    }
                    download_completed.set(true);
                } catch (IOException | ParseException ex) {
                    JOptionPane.showMessageDialog(downloading_content_dialog,"Could not connect to github","Error",JOptionPane.ERROR_MESSAGE);
                    downloading_content_dialog.setVisible(false);
                    ex.printStackTrace();
                }
                downloading_content_dialog.setVisible(false);
            }).start();
            downloading_content_dialog.setVisible(true);
            if (!download_completed.get()) {
                return;
            }
            JDialog dialog1 = new JDialog(dialog,"Download a compiler",true);
            dialog1.setSize(700,500);
            dialog1.setLayout(new BorderLayout());

            JPanel middlepanel = new JPanel();

            int label_width = 250;

            JPanel namepanel = new JPanel();
            namepanel.setPreferredSize(new Dimension(dialog1.getWidth(),Main.font.getSize()+15));
            JLabel namepanel_label = new JLabel("Name");
            JTextField namepanel_input = new JTextField();
            final boolean[] is_edited = {false};
            namepanel_input.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    is_edited[0]=true;
                }
            });
            namepanel_label.setPreferredSize(new Dimension(label_width,Main.font.getSize()+10));
            namepanel_input.setPreferredSize(new Dimension(dialog1.getWidth()-label_width-30,Main.font.getSize()+10));
            namepanel.add(namepanel_label);
            namepanel.add(namepanel_input);
            middlepanel.add(namepanel);

            JPanel locationpanel = new JPanel();
            locationpanel.setPreferredSize(new Dimension(dialog1.getWidth(),Main.font.getSize()+15));
            JLabel locationpanel_label = new JLabel("Folder path");
            JTextField locationpanel_input = new JTextField();
            locationpanel_input.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    is_edited[0] =true;
                }
            });
            locationpanel_label.setPreferredSize(new Dimension(label_width,Main.font.getSize()+10));
            locationpanel_input.setPreferredSize(new Dimension(dialog1.getWidth()-label_width-30,Main.font.getSize()+10));
            locationpanel.add(locationpanel_label);
            locationpanel.add(locationpanel_input);
            middlepanel.add(locationpanel);

            JPanel versionpanel = new JPanel();
            versionpanel.setPreferredSize(new Dimension(dialog1.getWidth(),Main.font.getSize()+15));
            JLabel versionpanel_label = new JLabel("Compiler version");
            JComboBox<String> versionpanel_input = new JComboBox<>();
            versionpanel_input.addItemListener(e12 -> {
                if (!is_edited[0]) {
                    locationpanel_input.setText(Main.mainfolder_path + "/compilers/" + e12.getItem().toString());
                    namepanel_input.setText(e12.getItem().toString());
                }
            });
            versionpanel_input.setModel(comboBoxModel);
            locationpanel_input.setText(Main.mainfolder_path + "/compilers/" + versionpanel_input.getItemAt(versionpanel_input.getSelectedIndex()));
            namepanel_input.setText(versionpanel_input.getItemAt(versionpanel_input.getSelectedIndex()));
            versionpanel_label.setPreferredSize(new Dimension(label_width,Main.font.getSize()+10));
            versionpanel_input.setPreferredSize(new Dimension(dialog1.getWidth()-label_width-30,Main.font.getSize()+10));
            versionpanel.add(versionpanel_label);
            versionpanel.add(versionpanel_input);
            middlepanel.add(versionpanel);

            JButton choosepathbutton = new JButton("Choose install folder");
            choosepathbutton.setPreferredSize(new Dimension(dialog1.getWidth()-20,Main.font.getSize()+15));
            choosepathbutton.addActionListener(e1 -> {
                String output = choose_compiler_path(dialog1,JFileChooser.DIRECTORIES_ONLY);
                if (output==null) {
                    return;
                }
                locationpanel_input.setText(output);
                is_edited[0]=true;
            });
            middlepanel.add(choosepathbutton);

            dialog1.add(middlepanel);

            JPanel buttonspanel = new JPanel();
            JButton installbutton = new JButton("Download");
            JButton cancelbutton = new JButton("Cancel");
            installbutton.addActionListener(e1 -> {
                dialog1.setVisible(false);
                downloading_content_dialog_label.setText("Please wait");
                downloading_content_dialog.setVisible(true);
                new Thread(() -> {
                    downloading_content_dialog_label.setText("Downloading "+versionpanel_input.getItemAt(versionpanel_input.getSelectedIndex())+" Compiler");
                    downloading_content_dialog_label.updateUI();
                    // sets the download link and the main file
                    String download_url = null;
                    String mainfile = null;
                    JSONObject downloadjsonobject = (JSONObject) jsonObject.get().get(versionpanel_input.getItemAt(versionpanel_input.getSelectedIndex()));
                    if (PlatformUtil.isWindows()) {

                    } else if (PlatformUtil.isLinux()) {

                    } else if (PlatformUtil.isMac()) {
                        
                    }
                }).start();
            });
            cancelbutton.addActionListener(e1 -> {
                dialog1.setVisible(false);
            });
            buttonspanel.add(installbutton);
            buttonspanel.add(cancelbutton);
            dialog1.add(buttonspanel, BorderLayout.PAGE_END);

            dialog1.setVisible(true);
        });
        dialog.add(install_compiler_button,BorderLayout.PAGE_END);

        JPanel listpanel = new JPanel();
        refresh_compiler_list(listpanel,dialog);
        listpanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popupMenu = new JPopupMenu();
                    JMenuItem new_compiler_config = new JMenuItem("New compiler configuration");
                    new_compiler_config.addActionListener(e1 -> {
                        JDialog dialog1 = new JDialog(dialog,"New compiler configuration",true);
                        dialog1.setSize(700,500);
                        dialog1.setLayout(new BorderLayout());

                        JPanel middlepanel = new JPanel();
                        JPanel namepanel = new JPanel();
                        JLabel namepanel_label = new JLabel("Name");
                        namepanel_label.setPreferredSize(new Dimension(200, Main.font.getSize() + 10));
                        namepanel.add(namepanel_label);
                        JTextField namepanel_input = new JTextField();
                        namepanel_input.setPreferredSize(new Dimension(dialog1.getWidth() - namepanel_label.getPreferredSize().width - 40, Main.font.getSize() + 10));
                        namepanel.add(namepanel_input);
                        middlepanel.add(namepanel);

                        JPanel pathpanel = new JPanel();
                        JLabel pathpanel_label = new JLabel("Compiler path");
                        pathpanel_label.setPreferredSize(new Dimension(200, Main.font.getSize() + 10));
                        pathpanel.add(pathpanel_label);
                        JTextField pathpanel_input = new JTextField();
                        pathpanel_input.setPreferredSize(new Dimension(dialog1.getWidth() - pathpanel_label.getPreferredSize().width - 40, Main.font.getSize() + 10));
                        pathpanel.add(pathpanel_input);
                        middlepanel.add(pathpanel);

                        JButton choose_compiler_path = new JButton("Choose compiler path");
                        choose_compiler_path.setPreferredSize(new Dimension(dialog1.getWidth() - 20, Main.font.getSize() + 15));
                        choose_compiler_path.addActionListener(e2 -> {
                            choose_compiler_path(dialog1,JFileChooser.FILES_ONLY);
                        });
                        middlepanel.add(choose_compiler_path);

                        JButton create_button = new JButton("Create configuration");
                        create_button.addActionListener(e2 -> {
                            if (compilerslist.containsKey(namepanel_input.getText())) {
                                JOptionPane.showMessageDialog(dialog1,"That configuration already exists!","Error",JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("path",pathpanel_input.getText());
                            compilerslist.put(namepanel_input.getText(),jsonObject);
                            dialog1.setVisible(false);
                            refresh_compiler_list(listpanel,dialog);
                            try {
                                save_compilers_list();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
                        dialog1.add(create_button, BorderLayout.PAGE_END);

                        dialog1.add(middlepanel);
                        dialog1.setResizable(false);

                        dialog1.setVisible(true);
                    });
                    popupMenu.add(new_compiler_config);
                    popupMenu.show(dialog,dialog.getMousePosition().x,dialog.getMousePosition().y);
                }
            }
        });
        listpanel.setLayout(new BoxLayout(listpanel,BoxLayout.PAGE_AXIS));

        final int[] count = {0};
        class setlabelpos {
            public setlabelpos() {
                count[0] =0;
                for (Component i : listpanel.getComponents()) {
                    i.setBounds(5,(count[0] *(Main.font.getSize()+10)),dialog.getWidth()-30,Main.font.getSize()+10);
                    count[0]++;
                }
            }
        }
        new setlabelpos();
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                new setlabelpos();
            }
        });
        dialog.add(listpanel,BorderLayout.CENTER);

        dialog.setVisible(true);
    }
    static void install_compiler(JSONObject compilerobject) {

    }
    static DefaultComboBoxModel<String> get_compilers_list() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (Object i : compilerslist.entrySet()) {
            Map.Entry entry = (Map.Entry)i;
            arrayList.add(entry.getKey().toString());
        }
        arrayList.sort(Comparator.naturalOrder());
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        for (String i : arrayList) {
            comboBoxModel.addElement(i);
        }
        return comboBoxModel;
    }
}
