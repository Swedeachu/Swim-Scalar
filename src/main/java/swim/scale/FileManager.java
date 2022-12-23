package swim.scale;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Scanner;

public class FileManager {

    public static File exportPath; // where packs get exported to
    public static File configPath;

    private static void createConfigsDir(File dirPath) {
        File configDir = new File(dirPath.getAbsolutePath() + "\\configs");
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        configPath = configDir;
        loadExportPath(configDir);
    }

    public static void clearTempDir(File tempDir) {
        for (File file : tempDir.listFiles()) {
            try {
                if (!file.getName().equals("configs")) {
                    FileUtils.forceDelete(file);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void createTempDir(File tempDir) {
        if (!tempDir.exists()) { // first check if temp dir exists
            tempDir.mkdirs();
        } else {
            clearTempDir(tempDir); // if it does exist make sure to clear all files inside it, if any
        }
        createConfigsDir(tempDir);
    }

    private static void loadExportPath(File configDir) {
        File exportConfig = new File(configDir + "\\export.config");
        if (exportConfig.exists()) {
            readExportPath(exportConfig);
        } else {
            createExportPath(exportConfig);
        }
    }

    private static void createExportPath(File exportConfig) {
        try {
            FileWriter myWriter = new FileWriter(exportConfig);
            String home = System.getProperty("user.home") + "\\Downloads";
            myWriter.write(home);
            myWriter.close();
            exportPath = new File(System.getProperty("user.home") + "\\Downloads");
            Window.exportPathButton.setText("Export Folder: " + exportPath.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readExportPath(File exportConfig) {
        try {
            Scanner reader = new Scanner(exportConfig);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                if (new File(data).exists()) {
                    exportPath = new File(data);
                    Window.exportPathButton.setText("Export Folder: " + data);
                    reader.close();
                    return;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void updateExportConfig(String newExportPath) throws IOException {
        try {
            File config = new File(System.getenv("APPDATA") + "\\swim_scale\\configs\\export.config");
            if (config.exists()) {
                FileWriter fw = new FileWriter(config, false);
                PrintWriter pw = new PrintWriter(fw, false);
                pw.flush();
                fw.write(newExportPath);
                pw.close();
                fw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean legalFileName(String filename) {
        final String REGEX_PATTERN = "^[\\w\\-. ]+$";
        if (filename == null) {
            return false;
        }
        return filename.matches(REGEX_PATTERN);
    }

}
