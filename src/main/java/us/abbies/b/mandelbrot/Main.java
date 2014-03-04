package us.abbies.b.mandelbrot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
    private static final int WIDTH = 1050;
    private static final int HEIGHT = 600;
    private static final int[] pixels = new int[WIDTH * HEIGHT];

    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            MandelbrotAction[] actions = new MandelbrotAction[THREAD_COUNT];
            for (int j = 0; j < THREAD_COUNT; j++) {
                actions[j] = new MandelbrotAction(HEIGHT * j / THREAD_COUNT,
                        HEIGHT * (j + 1) / THREAD_COUNT);
            }
            pool.invokeAll(Arrays.asList(actions));
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        }
        pool.shutdown();

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
        try (OutputStream out = Files.newOutputStream(Paths.get("/Users/smckay/out.png"))) {
            ImageIO.write(image, "png", out);
        }

    }

    private static class MandelbrotAction implements Callable<Void> {
        private final int fromY;
        private final int toY;

        public MandelbrotAction(int fromY, int toY) {
            this.fromY = fromY;
            this.toY = toY;
        }

        @Override
        public Void call() throws Exception {
            for (int i = fromY; i < toY; i++) {
                for (int j = 0; j < WIDTH; j++) {
                    double x0 = (j * 3.5d) / 1050d - 2.5d;
                    double y0 = (i * -2d) / 600d + 1d;
                    double x = 0.0;
                    double y = 0.0;
                    int iteration = 0;
                    while (x * x + y * y < 4 && iteration < 1000) {
                        double xtemp = x * x - y * y + x0;
                        y = 2 * x * y + y0;
                        x = xtemp;
                        iteration++;
                    }

                    if (iteration == 1000) {
                        pixels[i * WIDTH + j] = 255 << 24;
                    } else {
                        pixels[i * WIDTH + j] = -1;
                    }
                }
            }
            return null;
        }
    }
}
