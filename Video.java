import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.File;


public class Video {
    // View size
    static int width = 1438/2, height = 815;

    // The exponent (k) - armCount = k - 1
    static int exp = 3;

    // The zoom to start on
    static double scalingFactor = 0.005;

    // Will be multiplied to the scale factor each frame
    static double zoomFactor = 0.95;

    static double xOffset = 0;
    static double yOffset =  Math.sqrt(2);

    // How many frames
    static int frameCount = 100;

    // Where to zoom in on
    static Point2D.Double zoomInPoint = new Point2D.Double(0, Math.sqrt(2)); // If you find a general formula for zooming in on an arm pls let me know
                                                                                 // Right now I only know that for k=3 √2 works perfectly

    // Colors
    static Color fractalColor = Color.BLACK;
    // The outside is colored based on how fast it goes toward infinity
    static Color maxColor = new Color(0x0f172a); // this is te color for the fastest
    static Color minColor = Color.WHITE; // And this one for the slowes

    public static void main(String[] args) {
        if(args.length == 1) {
            frameCount = Integer.parseInt(args[0]);
        }
        File dir = new File("frames");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < frameCount; i++) {
            BufferedImage image = render();
            File outputfile = new File(dir, "frame_" + i + ".png");
            try {
            javax.imageio.ImageIO.write(image, "png", outputfile);
            } catch (IOException e) {
            e.printStackTrace();
            }
            scalingFactor *= zoomFactor;
            xOffset = zoomInPoint.getX() - (width / 2) * scalingFactor;
            yOffset = zoomInPoint.getY() - (height / 2) * scalingFactor;

            int size = 50;
            System.out.print("\r[");
            int progress = (int)((double)i / frameCount * size);
            System.out.print("=");
            for (int j = 0; j < progress; j++) {
                System.out.print("=");
            }
            for (int j = 0; j < size - progress; j++) {
                System.out.print(" ");
            }

            long estimatedTime = (System.currentTimeMillis() - startTime)/(i+1) * (frameCount-i);
            long hours = estimatedTime / 3600000;
            estimatedTime %= 3600000;
            long minutes = estimatedTime / 60000;
            estimatedTime %= 60000;
            long seconds = estimatedTime / 1000;
            // long milliseconds = estimatedTime % 1000;

            String time = "";
            if (hours > 0) {
                time += String.format("%02d hours ", hours);
            }
            if (minutes > 0) {
                time += String.format("%02d min ", minutes);
            }
            if (seconds > 0) {
                time += String.format("%02d sec ", seconds);
            }
            // if (milliseconds > 0) {
            //     time += String.format("%03d ms", milliseconds);
            // }

            System.out.print("] " + (int)((double)i/frameCount * 100) + "% [" + i + " / " + frameCount + "] ETR: " + time);
        }
    }
    

    static BufferedImage render() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int y = 0; y < height; y++) {
            final int currentY = y;
            executor.submit(() -> {
                for (int x = 0; x < width; x++) {
                    ComplexDouble c = new ComplexDouble((double)(x * scalingFactor) + xOffset, (double)(currentY) * scalingFactor + yOffset);
                    int pass = 256;
                    long max = 10;
                    boolean passed = false;
                    for (int i = 0; i < 110; i++) {
                        if (Math.hypot(c.real, c.imaginary) > max && !passed) {
                            pass = i;
                            passed = true;
                        }
                        ComplexDouble z = ComplexDouble.add(c, ComplexDouble.power(c, exp));
                        c = z;
                    }
                    if (c.real < 1 || c.imaginary < 1) {
                        image.setRGB(x, currentY, fractalColor.getRGB());
                    } else {
                        // image.setRGB(x, currentY, (int) map(pass, 0, 7, Color.RED.getRGB(), Color.BLUE.getRGB()));
                        image.setRGB(x, currentY, mapColor(pass, 0, 10, maxColor, minColor).getRGB());
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return image;
    }

    static double map(double value, double minIn, double maxIn, double minOut, double maxOut) {
        return (value - minIn) * (maxOut - minOut) / (maxIn - minIn) + minOut;
    }

    static Color mapColor(double value, double minIn, double maxIn, Color minOut, Color maxOut) {
        if(value > maxIn) return maxOut;
        double ratio = (value - minIn) / (maxIn - minIn);
        int red = (int) (minOut.getRed() + ratio * (maxOut.getRed() - minOut.getRed()));
        int green = (int) (minOut.getGreen() + ratio * (maxOut.getGreen() - minOut.getGreen()));
        int blue = (int) (minOut.getBlue() + ratio * (maxOut.getBlue() - minOut.getBlue()));
        return new Color(red, green, blue);
    }
}
