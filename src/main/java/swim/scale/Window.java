package swim.scale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

public class Window {

    private static JFrame frmSwimAutoScaler;
    private static JLabel currentPackLabel;
    public static JButton exportPathButton;
    public static JTextArea textArea;
    public static ButtonGroup group;
    public static File publicPack;

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        frmSwimAutoScaler = new JFrame();
        frmSwimAutoScaler.setTitle("Swim Auto Scaler");
        frmSwimAutoScaler.setResizable(false);
        frmSwimAutoScaler.getContentPane().setBackground(Color.DARK_GRAY);
        frmSwimAutoScaler.getContentPane().setLayout(null);
        frmSwimAutoScaler.setBounds(100, 100, 960, 440);
        frmSwimAutoScaler.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton packUploadButton = new JButton("Drag and Drop Pack");
        packUploadButton.setFont(new Font("Tahoma", Font.PLAIN, 20));
        packUploadButton.setBounds(10, 150, 213, 107);
        frmSwimAutoScaler.getContentPane().add(packUploadButton);

        JCheckBox packOptimizerCheckBox = new JCheckBox("Optimized for Packs");
        packOptimizerCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 20));
        packOptimizerCheckBox.setToolTipText("This means only images in important folders of a texture pack such as \"items\" or \"blocks\" are rescaled. This is enabled by default.");
        packOptimizerCheckBox.setSelected(true);
        packOptimizerCheckBox.setBounds(10, 14, 213, 59);
        frmSwimAutoScaler.getContentPane().add(packOptimizerCheckBox);

        JButton rescalePackButton = new JButton("Rescale Pack");
        rescalePackButton.setForeground(new Color(0, 0, 0));
        rescalePackButton.setFont(new Font("Tahoma", Font.PLAIN, 31));
        rescalePackButton.setBounds(10, 268, 213, 117);
        frmSwimAutoScaler.getContentPane().add(rescalePackButton);

        JPanel pathPanel = new JPanel();
        pathPanel.setBounds(233, 268, 541, 53);
        frmSwimAutoScaler.getContentPane().add(pathPanel);
        pathPanel.setLayout(null);

        currentPackLabel = new JLabel("Current Pack:");
        currentPackLabel.setFont(new Font("Tahoma", Font.PLAIN, 22));
        currentPackLabel.setBounds(10, 11, 521, 31);
        pathPanel.add(currentPackLabel);

        String s = "Scale to: ";
        String sizes[] = {s + "8x", s + "16x", s + "32x", s + "64x", s + "128x", s + "256x", s + "512x", s + "1024x", s + "2048x"};
        JComboBox comboBox = new JComboBox(sizes);
        comboBox.setFont(new Font("Tahoma", Font.PLAIN, 26));
        comboBox.setBounds(10, 80, 213, 59);
        frmSwimAutoScaler.getContentPane().add(comboBox);

        exportPathButton = new JButton("Export Path:");
        exportPathButton.setBounds(233, 332, 541, 53);
        frmSwimAutoScaler.getContentPane().add(exportPathButton);
        exportPathButton.setFont(new Font("Tahoma", Font.PLAIN, 22));
        exportPathButton.setHorizontalAlignment(SwingConstants.LEFT);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(233, 14, 541, 243);
        frmSwimAutoScaler.getContentPane().add(scrollPane);

        textArea = new JTextArea();
        textArea.setForeground(Color.GREEN);
        textArea.setBackground(Color.BLACK);
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        // start

        group = new ButtonGroup();

        JRadioButton nearestNeighborButton = new JRadioButton("Nearest Neighbor");
        nearestNeighborButton.setToolTipText("Fast but can produce pixelated or blocky results.");
        nearestNeighborButton.setBounds(780, 14, 148, 23);
        frmSwimAutoScaler.getContentPane().add(nearestNeighborButton);
        group.add(nearestNeighborButton);

        JRadioButton bilinearButton = new JRadioButton("Bilinear");
        bilinearButton.setToolTipText("Provides a good balance between speed and quality.");
        bilinearButton.setBounds(780, 50, 148, 23);
        frmSwimAutoScaler.getContentPane().add(bilinearButton);
        group.add(bilinearButton);
        bilinearButton.setSelected(true);

        JRadioButton bicubicButton = new JRadioButton("Bicubic");
        bicubicButton.setToolTipText("Provides the highest quality results but is slower than the other algorithms.");
        bicubicButton.setBounds(780, 86, 148, 23);
        frmSwimAutoScaler.getContentPane().add(bicubicButton);
        group.add(bicubicButton);

        JScrollPane algoScrollPane = new JScrollPane();
        algoScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        algoScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        algoScrollPane.setBounds(780, 116, 151, 269);
        frmSwimAutoScaler.getContentPane().add(algoScrollPane);

        JTextArea algoText = new JTextArea();
        algoText.setFont(new Font("Tahoma", Font.PLAIN, 13));
        algoText.setText("The Nearest neighbor\r\nalgorithm is fast,\r\nbut can produce\r\npixelated or blocky\r\nresults.\r\n\r\nThe Bilinear algorithm\r\nprovides a good balance\r\nbetween speed and\r\nquality.\r\n\r\nThe bicubic algorithm\r\nprovides the highest\r\nquality results, but is\r\nslower than the\r\nother algorithms.");
        algoText.setEditable(false);
        algoScrollPane.setViewportView(algoText);

        // end

        frmSwimAutoScaler.setVisible(true);

        packUploadButton.addActionListener(event -> {
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fileChooser.setAcceptAllFileFilterUsed(false);
                int response = fileChooser.showOpenDialog(null);
                if (response == JFileChooser.APPROVE_OPTION) {
                    String chosenFile = fileChooser.getSelectedFile().getAbsolutePath();
                    String extension = FilenameUtils.getExtension(chosenFile.toLowerCase());
                    File pack = new File(chosenFile);
                    if (FileUtils.isDirectory(pack) || extension.equals("zip") || extension.equals("mcpack")) {
                        publicPack = pack;
                        textArea.append("\nCurrent Pack: " + FilenameUtils.getBaseName(pack.getAbsolutePath()));
                        currentPackLabel.setText("<html><body>Current Pack: " + FilenameUtils.getBaseName(pack.getAbsolutePath()) + "<body><html>");
                    } else {
                        JOptionPane.showMessageDialog(null, "Must be a Texture Pack (zip, mcpack, folder)", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        packUploadButton.setDropTarget(new DropTarget() {
            private static final long serialVersionUID = 1L;
            File pack;

            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    int i = 0;
                    for (File currentFile : droppedFiles) {
                        pack = currentFile;
                        i++;
                    }
                    if (i <= 1) {
                        String extension = FilenameUtils.getExtension(pack.getAbsolutePath().toLowerCase());
                        if (FileUtils.isDirectory(pack) || extension.equals("zip") || extension.equals("mcpack")) {
                            publicPack = pack;
                            textArea.append("\nCurrent Pack: " + FilenameUtils.getBaseName(pack.getAbsolutePath()));
                            currentPackLabel.setText("<html><body>Current Pack: " + FilenameUtils.getBaseName(pack.getAbsolutePath()) + "<body><html>");
                        } else {
                            JOptionPane.showMessageDialog(null, "Must be a Texture Pack (zip, mcpack, folder)", "Error!", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "To many Files Dragged! (" + i + ")", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        rescalePackButton.addActionListener(event -> {
            if (publicPack == null) {
                JOptionPane.showMessageDialog(null, "You need to Select a Pack!", "Error!", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (publicPack.exists()) {
                try {
                    int resolution; // this will be calculated by the selection of the drop down box
                    int selection = comboBox.getSelectedIndex();
                    switch (selection) {
                        case 1:
                            resolution = 16;
                            break;
                        case 2:
                            resolution = 32;
                            break;
                        case 3:
                            resolution = 64;
                            break;
                        case 4:
                            resolution = 128;
                            break;
                        case 5:
                            resolution = 256;
                            break;
                        case 6:
                            resolution = 512;
                            break;
                        case 7:
                            resolution = 1024;
                            break;
                        case 8:
                            resolution = 2048;
                            break;
                        default:
                            resolution = 8;
                            break;
                    }
                    boolean optimized = packOptimizerCheckBox.isSelected();
                    ExportWindow.showExportWindow(resolution, optimized);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (UnsupportedLookAndFeelException e) {
                    throw new RuntimeException(e);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Pack Not Found! : " + publicPack.getAbsolutePath(), "Error!", JOptionPane.ERROR_MESSAGE);
            }
        });

        exportPathButton.addActionListener(event -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(frmSwimAutoScaler);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                FileManager.exportPath = file;
                exportPathButton.setText("Export Folder: " + FileManager.exportPath);
                JOptionPane.showMessageDialog(null, "Changed Export Folder to: " + file.getAbsolutePath(), "Update!", JOptionPane.INFORMATION_MESSAGE);
                try {
                    FileManager.updateExportConfig(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // create temp app data dirs after setting up UI
        File tempDir = new File(System.getenv("APPDATA") + "\\swim_scale");
        FileManager.createTempDir(tempDir);
        PackBuilder.createLegalDirs();
        PackBuilder.createFileBlackList();
        textArea.append("Pack Rescale Tool by Swim Services | discord.gg/swim");
    }

    public static String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }

}
