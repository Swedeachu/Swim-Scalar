package swim.scale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.zeroturnaround.zip.ZipUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackBuilder extends SwingWorker<Void, Void> {
    private static int scaledResolution;
    private static boolean optimizedDirs = true;
    private static ArrayList<String> dirs = new ArrayList<>();
    private static ArrayList<String> fileBlackList = new ArrayList<>();
    private static boolean isMCPE;

    public static void rescalePack(int res, boolean optimized) {
        scaledResolution = res;
        optimizedDirs = optimized;
        PackBuilder task = new PackBuilder();
        task.execute();
    }

    @Override
    protected Void doInBackground() throws IOException, UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // this works as a pass over to a new thread
        PackRescale(scaledResolution, optimizedDirs, FileManager.exportPath);
        return null;
    }

    private static int count;

    private static void countPack(File[] files, boolean optimized) {
        for (File file : files) {
            if (file.isDirectory()) {
                countPack(file.listFiles(), optimized);
            } else {
                if (FilenameUtils.getExtension(file.getAbsolutePath()).equals("png")) {
                    String parentName = FilenameUtils.getName(file.getParentFile().getAbsolutePath());
                    if (optimized == false || (optimized && dirs.contains(parentName))) {
                        count++;
                    }
                }
            }
        }
    }

    private static void PackRescale(int scaledRes, boolean optimized, File export) throws IOException, UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        isMCPE = false;
        Window.textArea.append("\nReading Pack");
        File pack = Window.publicPack;
        // Loading status window
        ExportWindow.closeExportWindow();
        LoadingWindow.showLoadingWindow();
        LoadingWindow.setValue(0);
        LoadingWindow.loadingLabel.setVisible(true);
        count = 0;
        // copy over the pack to the temp dir
        Window.textArea.append("\nCopying Pack to Temp File Location for Editing..");
        File tempDir = new File(System.getenv("APPDATA") + "\\swim_scale");
        FileManager.clearTempDir(tempDir);
        File dest = new File(tempDir.getAbsolutePath());
        // if we are a directory then just do file actions and copy over with no zipping
        if (pack.isDirectory()) {
            FileUtils.copyDirectoryToDirectory(pack, dest);
            countPack(pack.listFiles(), optimized);
            LoadingWindow.setMaximum(count);
            LoadingWindow.loadingLabel.setVisible(false);
            Window.textArea.append("\nBeginning Rescale");
            recursiveRescale(dest.listFiles(), scaledRes, optimized);
            Window.textArea.append("\nUploading Finalized Rescale");
            FileUtils.copyDirectoryToDirectory(pack, export);
            File exported = new File(export.getAbsolutePath() + "\\" + FilenameUtils.getName(pack.getAbsolutePath()));
            File newName = new File(export.getAbsolutePath() + "\\" + ExportWindow.packName);
            exported.renameTo(newName);
            Window.textArea.append("\nFinished Rescaling pack to: " + newName.getAbsolutePath());
            JOptionPane.showMessageDialog(null, "Finished Rescaling Pack to: " + newName.getAbsolutePath(), "Success!", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // if we are an mcpack or a zip then do file actions then zip/turn back into fresh mcpack
            FileUtils.copyFileToDirectory(pack, dest);
            // now unzip it
            Window.textArea.append("\nExtracting Texture Pack");
            File zippedPack = new File(dest.getAbsolutePath() + "\\" + FilenameUtils.getName(pack.getAbsolutePath()));
            File unzippedPack = new File(dest.getAbsolutePath() + "\\" + FilenameUtils.getBaseName(pack.getAbsolutePath()));
            ZipUtil.unpack(zippedPack, unzippedPack);
            Window.textArea.append("\nIndexing Texture Pack Files");
            countPack(unzippedPack.listFiles(), optimized);
            LoadingWindow.setMaximum(count);
            LoadingWindow.loadingLabel.setVisible(false);
            Window.textArea.append("\nBeginning Rescale");
            recursiveRescale(unzippedPack.listFiles(), scaledRes, optimized);
            FileUtils.forceDelete(zippedPack);
            // if we are an MCPE pack then zip it to mcpack and rename it, then move the file to the export dir
            if (isMCPE) {
                Window.textArea.append("\nCompressing to MCPACK File Format");
                compressMCPACK(unzippedPack.getAbsolutePath());
                Window.textArea.append("\nUploading Finalized Rescale");
                FileUtils.copyFileToDirectory(zippedPack, export);
                File exportedFile = new File(export.getAbsolutePath() + "\\" + FilenameUtils.getName(zippedPack.getAbsolutePath()));
                File renamedFile = new File(export.getAbsolutePath() + "\\" + ExportWindow.packName + ".mcpack");
                exportedFile.renameTo(renamedFile);
                Window.textArea.append("\nCleared Temp Files");
                Window.textArea.append("\nFinished Rescaling pack to: " + renamedFile.getAbsolutePath());
                JOptionPane.showMessageDialog(null, "Finished Rescaling Pack to: " + renamedFile.getAbsolutePath(), "Success!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // if we are not an mcpack then zip it normally then rename it and move it to the export dir
                Window.textArea.append("\nUploading Finalized Rescale");
                FileUtils.copyDirectoryToDirectory(unzippedPack, export);
                File finished = new File(export.getAbsolutePath() + "\\" + ExportWindow.packName);
                File arrivedFile = new File(export.getAbsolutePath() + "\\" + FilenameUtils.getBaseName(pack.getAbsolutePath()));
                arrivedFile.renameTo(finished);
                Window.textArea.append("\nCleared Temp Files");
                Window.textArea.append("\nFinished Rescaling pack to: " + arrivedFile.getAbsolutePath());
                JOptionPane.showMessageDialog(null, "Finished Rescaling Pack to: " + arrivedFile.getAbsolutePath(), "Success!", JOptionPane.INFORMATION_MESSAGE);
                FileUtils.forceDelete(unzippedPack);
            }
        }
        LoadingWindow.closeLoadingWindow();
    }

    private static void recursiveRescale(File[] files, int scaleRes, boolean optimized) {
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    recursiveRescale(file.listFiles(), scaleRes, optimized);
                } else {
                    if (FilenameUtils.getExtension(file.getAbsolutePath()).equals("png")) {
                        String parentName = FilenameUtils.getName(file.getParentFile().getAbsolutePath());
                        // when optimized mode is enabled the file much be in a dir of a common name (blocks, items, armor, etc)
                        if (optimized == false || (optimized && dirs.contains(parentName))) {
                            // get file and image properties
                            LoadingWindow.updateProgress();
                            BufferedImage img = ImageIO.read(file);
                            BufferedImage scaled = null;
                            int width = img.getWidth();
                            int height = img.getHeight();
                            // handle special cases for things like armor, particles, gui, etc
                            String name = FilenameUtils.getBaseName(file.getAbsolutePath());
                            if (!fileBlackList.contains(name)) {
                                if (name.equalsIgnoreCase("icons") || name.equalsIgnoreCase("particles")) {
                                    if (width != scaleRes) {
                                        int scaledImageRes = scaleRes * 4;
                                        // I do this because anything equal or under 128 is literally too small for these type of sprite sheets
                                        if (scaledImageRes <= 128) {
                                            scaledImageRes = 256;
                                        }
                                        if (width < scaleRes) {
                                            Window.textArea.append("\nUpscaling " + name + " to " + scaledImageRes + " x " + scaledImageRes);
                                            if (ExportWindow.scaleAlgo.equals(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)) {
                                                scaled = Scalar.defaultNearestNeighborRescale(img, scaledImageRes, scaledImageRes, ExportWindow.scaleAlgo);
                                            } else {
                                                scaled = Scalar.biUpScalePixelArt(img, scaledImageRes, scaledImageRes, width, height);
                                            }
                                        } else if (width > scaleRes) {
                                            Window.textArea.append("\nDownscaling " + name + " to " + scaledImageRes + " x " + scaledImageRes);
                                            if (ExportWindow.scaleAlgo.equals(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)) {
                                                scaled = Scalar.defaultNearestNeighborRescale(img, scaledImageRes, scaledImageRes, ExportWindow.scaleAlgo);
                                            } else if (ExportWindow.scaleAlgo.equals(RenderingHints.VALUE_INTERPOLATION_BILINEAR)) {
                                                scaled = Scalar.bilinearDownScalePixelArt(img, scaledImageRes, scaledImageRes, width);
                                            } else {
                                                scaled = Scalar.bicubicScaleImage(img, scaledImageRes, scaledImageRes);
                                            }
                                        }
                                    }
                                } else if (parentName.equalsIgnoreCase("armor")) {
                                    // armor width is the pack res times 2, but armor height is half the pack res
                                    if (width != scaleRes) {
                                        int scaleX = scaleRes * 2;
                                        int scaleY = scaleX / 2;
                                        if (width < scaleRes) {
                                            Window.textArea.append("\nUpscaling " + name + " to " + scaleX + " x " + scaleY);
                                            if (ExportWindow.scaleAlgo.equals(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)) {
                                                scaled = Scalar.defaultNearestNeighborRescale(img, scaleX, scaleY, ExportWindow.scaleAlgo);
                                            } else {
                                                scaled = Scalar.biUpScalePixelArt(img, scaleX, scaleY, width, height);
                                            }
                                        } else if (width > scaleRes) {
                                            Window.textArea.append("\nDownscaling " + name + " to " + scaleX + " x " + scaleY);
                                            if (ExportWindow.scaleAlgo.equals(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)) {
                                                scaled = Scalar.defaultNearestNeighborRescale(img, scaleX, scaleY, ExportWindow.scaleAlgo);
                                            } else if (ExportWindow.scaleAlgo.equals(RenderingHints.VALUE_INTERPOLATION_BILINEAR)) {
                                                scaled = Scalar.bilinearDownScalePixelArt(img, scaleX, scaleY, width);
                                            } else {
                                                scaled = Scalar.bicubicScaleImage(img, scaleX, scaleY);
                                            }
                                        }
                                    }
                                } else if ((width != scaleRes || height != scaleRes) && (width == height)) { // for rescaling files with same width and height
                                    // decide if the image needs to be upscaled or downscaled
                                    if (width < scaleRes) {
                                        Window.textArea.append("\nUpscaling " + name + " to " + scaleRes + " x " + scaleRes);
                                        if (ExportWindow.scaleAlgo.equals(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)) {
                                            scaled = Scalar.defaultNearestNeighborRescale(img, scaleRes, scaleRes, ExportWindow.scaleAlgo);
                                        } else {
                                            scaled = Scalar.biUpScalePixelArt(img, scaleRes, scaleRes, width, height);
                                        }
                                    } else if (width > scaleRes) {
                                        Window.textArea.append("\nDownscaling " + name + " to " + scaleRes + " x " + scaleRes);
                                        if (ExportWindow.scaleAlgo.equals(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)) {
                                            scaled = Scalar.defaultNearestNeighborRescale(img, scaleRes, scaleRes, ExportWindow.scaleAlgo);
                                        } else if (ExportWindow.scaleAlgo.equals(RenderingHints.VALUE_INTERPOLATION_BILINEAR)) {
                                            scaled = Scalar.bilinearDownScalePixelArt(img, scaleRes, scaleRes, width);
                                        } else {
                                            scaled = Scalar.bicubicScaleImage(img, scaleRes, scaleRes);
                                        }
                                    }
                                }
                                // write to file and set loading preview
                                if (scaled != null) {
                                    LoadingWindow.setLoadingPreview(scaled);
                                    ImageIO.write(scaled, "png", file);
                                }
                            }
                        }
                    } else if (FilenameUtils.getName(file.getAbsolutePath()).equals("manifest.json")) {
                        isMCPE = true;
                        newManifest(file);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void newManifest(File manifest) {
        if (manifest.exists()) {
            Window.textArea.append("\nCreating manifest.json");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(manifest));
                ArrayList<String> lines = new ArrayList<>();
                String line = reader.readLine();
                while (line != null) {
                    if (line.contains("\"uuid\"")) {
                        String uuid = UUID.randomUUID().toString();
                        String newLine = "\t\"uuid\": \"" + uuid + "\",";
                        lines.add(newLine);
                    } else if (line.contains("\"name\"")) {
                        String newLine = "\t\"name\": \"" + ExportWindow.packName + "\",";
                        lines.add(newLine);
                    } else {
                        lines.add(line);
                    }
                    line = reader.readLine();
                }
                reader.close();
                File temp = new File(manifest.getParentFile().getAbsolutePath() + "\\temp.json");
                if (temp.createNewFile()) {
                    FileWriter writer = new FileWriter(temp);
                    for (String currentLine : lines) {
                        writer.write(currentLine + "\n");
                    }
                    writer.close();
                    FileUtils.forceDelete(manifest);
                    temp.renameTo(manifest);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File compressMCPACK(String dirPath) {
        final Path sourceDir = Paths.get(dirPath);
        String zipFileName = dirPath.concat(".zip");
        try {
            final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = sourceDir.relativize(file);
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.close();
            File zippedPack = new File(dirPath + ".zip");
            File mcpack = new File(dirPath + ".mcpack");
            zippedPack.renameTo(mcpack);
            FileUtils.forceDelete(new File(dirPath));
            return zippedPack;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(dirPath);
    }

    // list of images to not rescale no matter what
    public static void createFileBlackList() {
        fileBlackList.add("gui"); // so far don't have many other things to avoid rescaling
    }

    public static void createLegalDirs() {
        dirs.add("item");
        dirs.add("block");
        dirs.add("items");
        dirs.add("blocks");
        // will need custom rescaling sizes for these files
        dirs.add("armor"); // all the different armor models
        dirs.add("particle"); // particles.png
        // dirs.add("gui"); // icons.png // decided to not rescale anything in the gui
    }
}

