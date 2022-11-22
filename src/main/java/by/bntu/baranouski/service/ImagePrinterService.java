package by.bntu.baranouski.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImagePrinterService {
    public void printToJpg(BufferedImage image, String fileName) throws IOException {
        try(OutputStream fos = new BufferedOutputStream(new FileOutputStream(fileName))){
            ImageIO.write(image, "jpg", fos);
        }
    }
}
