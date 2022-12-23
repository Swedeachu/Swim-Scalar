package swim.scale;

import javax.swing.*;
import java.awt.*;
import java.awt.Window.Type;
import java.awt.event.WindowEvent;

public class ExportWindow extends SwingWorker<Void, Void> {

    private static JFrame exportFrame;
    public static JTextField packNameField;
    private static boolean optimized;
    private static int resToScale;
    public static String packName;
    public static Object scaleAlgo = RenderingHints.VALUE_INTERPOLATION_BILINEAR;

    /**
     * @wbp.parser.entryPoint
     */
    public static void showExportWindow(int scaleRes, boolean optiVal) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        exportFrame = new JFrame();
        exportFrame.setType(Type.POPUP);
        exportFrame.setResizable(false);
        exportFrame.setTitle("Export Rescaled Pack");
        exportFrame.setBounds(100, 100, 479, 244);
        exportFrame.getContentPane().setBackground(Color.DARK_GRAY);
        exportFrame.getContentPane().setLayout(null);

        packNameField = new JTextField();
        packNameField.setBounds(10, 41, 448, 59);
        exportFrame.getContentPane().add(packNameField);
        packNameField.setColumns(10);

        JLabel packNameLabel = new JLabel("Pack Name:");
        packNameLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
        packNameLabel.setForeground(Color.LIGHT_GRAY);
        packNameLabel.setBackground(Color.LIGHT_GRAY);
        packNameLabel.setBounds(10, 11, 177, 19);
        exportFrame.getContentPane().add(packNameLabel);

        JButton exportButton = new JButton("Export");
        exportButton.setFont(new Font("Tahoma", Font.PLAIN, 53));
        exportButton.setBounds(10, 111, 448, 88);
        exportFrame.getContentPane().add(exportButton);

        exportButton.addActionListener(event -> {
            String text = packNameField.getText();
            if (FileManager.legalFileName(text) == true) {
                String selected = Window.getSelectedButtonText(Window.group);
                System.out.println(selected);
                if (selected.equalsIgnoreCase("nearest neighbor")) {
                    scaleAlgo = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                } else if (selected.equalsIgnoreCase("bilinear")) {
                    scaleAlgo = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
                } else if (selected.equalsIgnoreCase("bicubic")) {
                    scaleAlgo = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                }
                resToScale = scaleRes;
                optimized = optiVal;
                packName = text;
                Window.textArea.append("\nRescaling pack at Resolution: " + scaleRes + "x | Pack Optimization set to " + optimized);
                ExportWindow task = new ExportWindow();
                task.execute();
            } else {
                JOptionPane.showMessageDialog(null, "Not a legal File Name: " + text, "Error!", JOptionPane.WARNING_MESSAGE);
            }
        });

        exportFrame.setVisible(true);
    }

    public static void closeExportWindow() {
        exportFrame.dispatchEvent(new WindowEvent(exportFrame, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    protected Void doInBackground() throws Exception {
        PackBuilder.rescalePack(resToScale, optimized);
        return null;
    }
}
