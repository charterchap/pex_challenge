import java.io.*;
import java.awt.*;
import java.net.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.*;
import java.util.List;
import java.util.stream.*;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.concurrent.*;


/**
 * class so we can have a shared memory size tracker
 * needs a mutex before the threadpool can increase above 1
 */
class SharedSize {
    static long var=0;
}


/**
 * basic pair class
 */
class Pair<A,B>{
    A x;
    B y;
    public Pair(A x, B y) {
        this.x = x;
        this.y = y;
    }
}


public class MainChallenge {

    /**
     * Returns an Integer Array up to length 3 with the top three colors
     * represented as a 24-bit RGB color found in the image provided to this
     * function
     *
     * @param  bufferedImage a BufferedImage
     * @return an array of size 3 with the top 3 colors as an int. If there is 
     *         less than 3 top color it will return a 0 instead. Since 0 is 
     *         black, this is an obvious bug that would need to be dealt with.
     *         0th element is highest
     */
    public static Integer [] getTopColors(BufferedImage bufferedImage) throws IOException {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[] colorData = new int[height*width];
        Map<Integer, Integer> mdata = new HashMap<>();

        // This is not the most generalized way of doing this, but it should cut
        // down on memory usage. I think normally it would be better to have an
        // array of pairs and sort them in the end.
        Pair<Integer, Integer> one = new Pair<Integer, Integer>(0,0); // bug see doc above
        Pair<Integer, Integer> two = new Pair<Integer, Integer>(0,0);
        Pair<Integer, Integer> three = new Pair<Integer, Integer>(0,0);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                colorData[row*width+col] = bufferedImage.getRGB(col, row);
                Integer key = bufferedImage.getRGB(col, row) & 0xFFFFFF;
                mdata.merge(key, 1, Integer::sum);

                if     (mdata.get(key) >= one.y){ one = new Pair<Integer,Integer>(key,mdata.get(key));}
                else if(mdata.get(key) >= two.y){ two = new Pair<Integer,Integer>(key,mdata.get(key));}
                else if(mdata.get(key) > three.y){ three = new Pair<Integer,Integer>(key,mdata.get(key));}
            }
        }

        Integer [] b = {one.x,two.x,three.x};
        return b;
    }


    public static void processImage(Pair<String, BufferedImage> bufferedImage) {
        String csv_line = bufferedImage.x;

        try {
            if(bufferedImage.y != null) {
                Integer [] top_colors = getTopColors(bufferedImage.y);

                for(int val : top_colors){
                    csv_line += String.format(",#%06X",val);
                }

                // uncount memory
                long imageSize = ((long)bufferedImage.y.getData().getDataBuffer().getSize()) * 4l;
                SharedSize.var -= imageSize; //thread safety?
            } else {
                csv_line += ",0,0,0"; // Obviously not the best error handling
                System.out.println("Image not valid, unable to process: "+bufferedImage.x);
            }
        } catch (IOException ignore){
            System.out.println(ignore);
        }

        try(FileWriter fw = new FileWriter("output.csv", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(csv_line);
            System.out.println(csv_line);
        } catch (IOException e) {
            System.out.println("Could not write to csv file");
        }
    }


    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        CompletionService<Pair<String, BufferedImage>> cs = new ExecutorCompletionService<Pair<String, BufferedImage>>(pool);
        
        
        try {
            List<String> allLines = Files.readAllLines(Paths.get(".", "input.txt"));
            int job_count = 0;
            for (String line : allLines) {
                cs.submit(new ImageWorker(line));
            }

            pool.shutdown();
            try {
              while (!pool.isTerminated()) {
                final Future<Pair<String, BufferedImage>> future = cs.take();
                processImage(future.get());
                System.gc();
                job_count++;
              }
            } catch (ExecutionException | InterruptedException ex) { }
        } catch (IOException ignore){
            System.out.println(ignore);
        }
    }
}
