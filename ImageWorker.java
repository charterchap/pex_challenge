import java.io.*;
import java.awt.*;
import java.net.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;

import java.util.concurrent.Callable;


/**
 * Downloads image from uri. Does not check if uri is valid
 *
 * @param  uri address of image to download
 * @return the image at the url as a BufferedImage
 */
public class ImageWorker implements Callable {

    String path;
    public ImageWorker(String uri){
        this.path = uri;
    }

    @Override
    public Pair<String, BufferedImage> call() {
        BufferedImage image = null;
        try {
            // crude memory limiter to keep us under 512 mb - which if we
            // had an image of 256 mb would blow up.
            while( SharedSize.var > 256000000l ){ // bytes,
                Thread.sleep(3000);
                // System.out.println("waiting for:" + path);
            }
            // System.out.println("Downloading image:" + this.path);
            URL url = new URL(this.path);
            image = ImageIO.read(url);

            long isize = ((long)image.getData().getDataBuffer().getSize()) * 4l;
            SharedSize.var += isize;
        } catch (IOException e) {
            System.out.println(e);
        } catch (InterruptedException e){
        }
        return new Pair<String, BufferedImage>(this.path,image);
    }
}
