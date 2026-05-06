package ScanHub.BLL.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class BarcodeDetector {

    private static final MultiFormatReader READER = new MultiFormatReader();

    /**
     * Returns true if the TIFF bytes contain a recognizable barcode.
     * Used to trigger document splitting when SplitBehavior is BARCODE.
     */
    public static boolean containsBarcode(byte[] tiffData) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(tiffData));
            if (image == null) return false;

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            READER.decode(bitmap); // throws NotFoundException if no barcode found
            return true;
        } catch (NotFoundException e) {
            return false; // no barcode - normal TIFF page
        } catch (Exception e) {
            return false;
        }
    }
}