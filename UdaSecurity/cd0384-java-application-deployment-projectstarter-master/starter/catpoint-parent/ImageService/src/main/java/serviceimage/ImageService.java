package serviceimage;

import java.awt.image.BufferedImage;
public interface ImageService {
    boolean imageContainsCat(BufferedImage image, float confidenceThreshhold);
}
