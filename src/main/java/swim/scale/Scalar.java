package swim.scale;

import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class Scalar {

    // this method uses the java graphics library rendering hints for scaling, this really only works well for nearest neighbor scaling
    // sadly for the other two algorithms the java graphics library does poorly with, so I had to reinvent the wheel a little bit
    public static BufferedImage defaultNearestNeighborRescale(BufferedImage originalImage, int targetWidth, int targetHeight, Object interpolationHint) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHint);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }

    // prevents integer division by 0, returns a value between 0.0 to 1.0 if a <= b,
    // otherwise it will return values above 1.0 to represents percents over 100%
    public static double safeSingleBasePercentOf(double a, double b) {
        if (b == 0) {
            return 0.0;
        }
        return a / b; // literally just normal decimal division
    }

    // simply increases pixel size on an equivalently scaled up canvas
    // I decided to have bicubic and bilinear upscaling share the same method as they are both of the same surrounding neighbor theory and this works losslessly anyways
    public static BufferedImage biUpScalePixelArt(BufferedImage pixelArt, int scaledWidth, int scaledHeight, int originalWidth, int originalHeight) {
        try {
            // Create a new BufferedImage to hold the scaled pixel art
            BufferedImage scaledPixelArt = new BufferedImage(scaledWidth, scaledHeight, TYPE_INT_ARGB);
            // Get the percent in single digit decimal point for scaling with
            double percentRes = safeSingleBasePercentOf(scaledWidth, originalWidth);
            // Loop through the original pixel art and copy the colors to the corresponding pixels in the scaled pixel art
            for (int i = 0; i < originalHeight; i++) {
                for (int j = 0; j < originalWidth; j++) {
                    int color = pixelArt.getRGB(j, i);
                    // Copy the color to the corresponding pixels in the scaled pixel art
                    for (int k = 0; k < percentRes; k++) {
                        for (int l = 0; l < percentRes; l++) {
                            scaledPixelArt.setRGB((int) (j * percentRes + l), (int) (i * percentRes + k), color);
                        }
                    }
                }
            }
            // the finished and upscaled pixel art
            return scaledPixelArt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pixelArt;
    }

    // this uses color interpolation on surrounding pixels to preserve image quality as best as possible when down scaling
    // strangely enough I didn't need the original height for this algorithm
    public static BufferedImage bilinearDownScalePixelArt(BufferedImage pixelArt, int scaledWidth, int scaledHeight, int originalWidth) {
        try {
            // Create a new BufferedImage to hold the scaled pixel art
            BufferedImage scaledPixelArt = new BufferedImage(scaledWidth, scaledHeight, TYPE_INT_ARGB);
            // Get the percent in single digit decimal point for scaling with
            double percentRes = safeSingleBasePercentOf(scaledWidth, originalWidth);
            // Loop through the scaled pixel art and interpolate the colors from the original pixel art
            for (int i = 0; i < scaledHeight; i++) {
                for (int j = 0; j < scaledWidth; j++) {
                    // Calculate the coordinates of the corresponding pixel in the original pixel art
                    double x = j / percentRes;
                    double y = i / percentRes;
                    // Interpolate the color from the original pixel art
                    int color = bilinearColorInterpolation(pixelArt, x, y);
                    // Set the color of the pixel in the scaled pixel art
                    scaledPixelArt.setRGB(j, i, color);
                }
            }
            return scaledPixelArt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pixelArt;
    }

    // returns an interpolated color based on the 4 direct surrounding pixels of a coordinate in an image from North, East, South, and West cardinal directions
    public static int bilinearColorInterpolation(BufferedImage pixelArt, double x, double y) {
        // Calculate the coordinates of the four surrounding pixels
        int x1 = (int) Math.floor(x);
        int y1 = (int) Math.floor(y);
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        // Check if the coordinates are within the bounds of the original pixel art (point to 2D rectangle collision check)
        if (x1 < 0 || x1 >= pixelArt.getWidth() || y1 < 0 || y1 >= pixelArt.getHeight() || x2 < 0 || x2 >= pixelArt.getWidth() || y2 < 0 || y2 >= pixelArt.getHeight()) {
            return 0; // If not, return the default color
        }
        // Get the colors of the surrounding pixels
        int color1 = pixelArt.getRGB(x1, y1);
        int color2 = pixelArt.getRGB(x1, y2);
        int color3 = pixelArt.getRGB(x2, y1);
        int color4 = pixelArt.getRGB(x2, y2);
        // Calculate the weights for each color
        double w1 = (x2 - x) * (y2 - y);
        double w2 = (x2 - x) * (y - y1);
        double w3 = (x - x1) * (y2 - y);
        double w4 = (x - x1) * (y - y1);
        // Calculate the interpolated color by summing the weighted colors across the RGB values
        int r = (int) (w1 * ((color1 >> 16) & 0xff) + w2 * ((color2 >> 16) & 0xff) + w3 * ((color3 >> 16) & 0xff) + w4 * ((color4 >> 16) & 0xff));
        int g = (int) (w1 * ((color1 >> 8) & 0xff) + w2 * ((color2 >> 8) & 0xff) + w3 * ((color3 >> 8) & 0xff) + w4 * ((color4 >> 8) & 0xff));
        int b = (int) (w1 * (color1 & 0xff) + w2 * (color2 & 0xff) + w3 * (color3 & 0xff) + w4 * (color4 & 0xff));
        // Calculate the interpolated alpha value by summing the weighted alpha values
        int a = (int) (w1 * ((color1 >> 24) & 0xff) + w2 * ((color2 >> 24) & 0xff) + w3 * ((color3 >> 24) & 0xff) + w4 * ((color4 >> 24) & 0xff));
        // Return the interpolated color as an integer
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // this only works well for down scaling, bicubic upscaling results in blurry imagery, so instead we use the biUpScalePixelArt method
    public static BufferedImage bicubicScaleImage(BufferedImage image, int width, int height) {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        BufferedImage scaledImage = new BufferedImage(width, height, TYPE_INT_ARGB);

        // Compute the scaling factors
        double xScale = (double) width / originalWidth;
        double yScale = (double) height / originalHeight;

        // Iterate over the pixels in the scaled image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Determine the corresponding pixel in the original image
                double sourceX = x / xScale;
                double sourceY = y / yScale;

                // Compute the weights for each surrounding pixel
                double[] xWeights = getCubicWeights(sourceX - (int) sourceX);
                double[] yWeights = getCubicWeights(sourceY - (int) sourceY);

                // Initialize the weighted sum of colors
                double r = 0;
                double g = 0;
                double b = 0;
                double a = 0;

                // Iterate over the surrounding pixels
                for (int i = 0; i <= 3; i++) {
                    for (int j = 0; j <= 3; j++) {
                        // Get the color of the surrounding pixel
                        int cx = (int) sourceX + i - 1;
                        int cy = (int) sourceY + j - 1;
                        Color color = new Color(0, 0, 0, 0); // default just in case
                        if (cx >= 0 && cx < originalWidth && cy >= 0 && cy < originalHeight) {
                            color = new Color(image.getRGB(cx, cy), true);
                        }
                        r += color.getRed() * xWeights[i] * yWeights[j] * color.getAlpha();
                        g += color.getGreen() * xWeights[i] * yWeights[j] * color.getAlpha();
                        b += color.getBlue() * xWeights[i] * yWeights[j] * color.getAlpha();
                        a += color.getAlpha() * xWeights[i] * yWeights[j];
                    }
                }

                // Set the pixel in the scaled image
                int ir = (int) Math.round(r / a);
                int ig = (int) Math.round(g / a);
                int ib = (int) Math.round(b / a);
                int ia = (int) Math.round(a);
                scaledImage.setRGB(x, y, new Color(ir, ig, ib, ia).getRGB());
            }
        }

        return scaledImage;
    }

    private static double[] getCubicWeights(double t) {
        double[] weights = new double[4];
        double tt = t * t;
        weights[0] = (1 / 6.0) * (-tt * t + 3 * tt - 3 * t + 1);
        weights[1] = (1 / 6.0) * (3 * tt * t - 6 * tt + 4);
        weights[2] = (1 / 6.0) * (-3 * tt * t + 3 * tt + 3 * t + 1);
        weights[3] = (1 / 6.0) * tt * t;
        return weights;
    }

}
