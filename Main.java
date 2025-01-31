import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    // View size
    static int width = 800, height = 800;

    // The exponent (k) - armCount = k - 1
    static double exp = 3;

    // The zoom to start on
    static double scalingFactor = 0.005;
    static double xOffset = -2;
    static double yOffset = -2;

    // What to zoom in by
    static double zoomFactor = 0.5;

    // Colors
    static Color fractalColor = Color.BLACK;
    // The outside is colored based on how fast it goes toward infinity
    static Color maxColor = new Color(0x0f172a); // this is te color for the fastest
    static Color minColor = Color.WHITE; // And this one for the slowes

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("fractal");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.WHITE);
        frame.setVisible(true);
        frame.setSize(width, height);
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) { // Left mouse button
                    System.out.println("Zoom");
                    zoomIn(e.getX(), e.getY());
                    render(frame);
                    frame.repaint();
                    frame.revalidate();
                    frame.requestFocus();
                }
            }
        });
        render(frame);
        frame.repaint();
        frame.revalidate();
    }

    static void zoomIn(int mouseX, int mouseY) {
        double newCenterX = mouseX * scalingFactor + xOffset;
        double newCenterY = mouseY * scalingFactor + yOffset;

        scalingFactor *= zoomFactor;
        xOffset = newCenterX - (width / 2) * scalingFactor;
        yOffset = newCenterY - (height / 2) * scalingFactor;
    }

    static void render(JFrame frame) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int y = 0; y < height; y++) {
            final int currentY = y;
            executor.submit(() -> {
                for (int x = 0; x < width; x++) {
                    ComplexDouble c = new ComplexDouble((double)(x * scalingFactor) + xOffset, ((double)(currentY) * scalingFactor) + yOffset);
                    int pass = 256;
                    long max = 10;
                    boolean passed = false;
                    for (int i = 0; i < 250; i++) {
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
                        // min and max color are reversed because fast = less and slow = more
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

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
            }
        };

        frame.getContentPane().removeAll(); // Remove all existing panels
        frame.add(panel);
        frame.revalidate();
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