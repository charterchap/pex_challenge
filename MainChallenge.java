
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import java.util.stream.*;
import javax.imageio.*;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;
// import ImageWorker;



class StaticVar {
    static long var=0;
}

public class MainChallenge {

    public static BufferedImage getImageFromWeb(String path) throws IOException {
        BufferedImage image = null;
        try {
            URL url = new URL(path);
            image = ImageIO.read(url);
        } catch (IOException e) {
        }
        return image;
    }

    /**
     * Returns an Integer Array up to length 3 with the top three colors
     * represented as a 24-bit RGB color found in the image provided to this
     * function
     *
     * @param  ImageName  file path to the image
     */
    public static Integer [] getTopColors(BufferedImage bufferedImage) throws IOException {
        // open image
        // File imgPath = new File(ImageName);
        // BufferedImage bufferedImage = ImageIO.read(imgPath);

        // get DataBufferBytes from Raster
        // WritableRaster raster = bufferedImage .getRaster();
        // DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[] colorData = new int[height*width];
        Map<Integer, Integer> mdata = new HashMap<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                colorData[row*width+col] = bufferedImage.getRGB(col, row);
                mdata.merge(bufferedImage.getRGB(col, row) & 0xFFFFFF, 1, Integer::sum);
            }
        }

        LinkedHashMap<Integer, Integer> sortedMap = 
            mdata.entrySet().stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                     (e1, e2) -> e1, LinkedHashMap::new));
        // System.out.println()
        // return String.valueOf(linkedmap.entrySet().toArray()[linkedmap.size() - 1]);

        // System.out.println(sortedMap.entrySet().toArray()[sortedMap.size() - 1]);

        // System.out.println(sortedMap.keySet().toArray().getClass().getSimpleName());
        Integer [] a = sortedMap.keySet().toArray(new Integer[0]);

        Integer [] b = Arrays.copyOfRange(a, 0, 3);

        // Arrays.copyOfRange(oldArray, startIndex, endIndex)

        // System.out.println(b);
        // System.out.println(Integer.toHexString(sortedMap.entrySet().toArray()[sortedMap.size() - 1]));
        // System.out.println(Integer.toHexString(b[0]));
        // System.out.println(Integer.toHexString(b[1]));
        // System.out.println(Integer.toHexString(b[2]));
        // System.out.println(sortedMap);
        // 2ECC71, E74C3C, 32CB72
        // int [] rgb_array = bufferedImage.getRGB()

        return b;
    }

    public static void printThree(String path) {
        try {
            
            Integer [] cm = getTopColors(getImageFromWeb(path));
            for( int val : cm){
                System.out.println(Integer.toHexString(val));
            }
        } catch (IOException ignore){
            System.out.println(ignore);
        }
    }

    public static void printThree_b(BufferedImage bufferedImage) {
        try {
            if(bufferedImage != null) {
                StaticVar shared_size = new StaticVar();
                Integer [] cm = getTopColors(bufferedImage);
                for( int val : cm){
                    System.out.println(Integer.toHexString(val));
                }
                long imageSize = ((long)bufferedImage.getData().getDataBuffer().getSize()) * 4l;

                shared_size.var -= imageSize;
                System.out.println("psize is:" + shared_size.var +" : "+imageSize);
            } else {
                System.out.println("Image not valid, unable to process");
            }
        } catch (IOException ignore){
            System.out.println(ignore);
        }
    }


    // public static void main(String[] args) {
        
    //     try {
    //         Stream<String> lines = Files.lines(Paths.get(".", "input.txt"));
    //         lines.forEach(MainChallenge::printThree);
            
    //     //     Integer [] cm = getTopColors(getImageFromWeb("http://i.imgur.com/TKLs9lo.jpg"));
    //     //     for( int val : cm){
    //     //         System.out.println(Integer.toHexString(val));
    //     //     }
    //     } catch (IOException ignore){
    //         System.out.println(ignore);
    //     }
    // }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Executor ex= Executors.newCachedThreadPool();
        ExecutorService pool = Executors.newFixedThreadPool(1);
        CompletionService<BufferedImage> cs = new ExecutorCompletionService<BufferedImage>(pool);
        
        
        try {
            List<String> allLines = Files.readAllLines(Paths.get(".", "input.txt"));
            int job_count = 0;
            for (String line : allLines) {
                cs.submit(new ImageWorker(line, new StaticVar()));
                System.out.println(line);

            }

            pool.shutdown();
            try {
              while (!pool.isTerminated()) {
                final Future<BufferedImage> future = cs.take();
                System.out.println("got a future");
                printThree_b(future.get());
                System.gc();
                job_count++;
                System.out.println("j c:" +job_count);
              }
            } catch (ExecutionException | InterruptedException ex) { }
            // for(int i=0; i < job_count; i++){
            //     printThree_b(cs.take().get());
            // }

            // Stream<String> lines = Files.lines(Paths.get(".", "input.txt"));
            // for(String l : lines) {
            //     System.out.println(l);
            // }
            // lines.forEach(MainChallenge::printThree);
            
        //     Integer [] cm = getTopColors(getImageFromWeb("http://i.imgur.com/TKLs9lo.jpg"));
        //     for( int val : cm){
        //         System.out.println(Integer.toHexString(val));
        //     }
        } catch (IOException ignore){
            System.out.println(ignore);
        }
    }
}
