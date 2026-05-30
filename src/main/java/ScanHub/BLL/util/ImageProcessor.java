package ScanHub.BLL.util;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ImageProcessor {

    public ImageProcessor() {}

    public static BufferedImage sharpen(BufferedImage source, float strength) {
        float center = 1 + (4 * strength);
        float edge = -strength;

        float[] kernelInfo = {
                0f, edge, 0f,
                edge, center, edge,
                0f, edge, 0f
        };

        Kernel kernel = new Kernel(3, 3, kernelInfo);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        return op.filter(source, null);
    }

}
