import java.io.*;
import java.awt.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.nio.file.*;
import java.util.Set;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import java.util.stream.*;
import javax.imageio.*;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageWorker implements Callable {

    StaticVar shared_size;

    String path;
    public ImageWorker(String path, StaticVar shared_size){
        this.path = path;
        this.shared_size = shared_size;
    }

    @Override
    public BufferedImage call() {
        BufferedImage image = null;
        try {
            // crude memory limiter to keep us under 512 mb - which if we
            // had an image of 256 mb would blow up.
            while( this.shared_size.var > 256000000l ){ // bytes,
                Thread.sleep(3000);
                System.out.println("waiting for:" + path);
            }
            System.out.println("Downloading image:" + path);
            URL url = new URL(path);
            image = ImageIO.read(url);

            long isize = ((long)image.getData().getDataBuffer().getSize()) * 4l;
            System.out.println("image size: "+isize);
            this.shared_size.var += isize;

            System.out.println("worker size: "+this.shared_size.var);
        } catch (IOException e) {
            System.out.println(e);
        } catch (InterruptedException e){
        }
        return image;
    }
}
