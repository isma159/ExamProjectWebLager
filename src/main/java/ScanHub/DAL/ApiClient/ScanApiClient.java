package ScanHub.DAL.ApiClient;

import ScanHub.DAL.interfaces.IScanSource;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ScanApiClient implements IScanSource {

    private static final String BASE_URL = "https://studentiffapi-production.up.railway.app";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * GET /getRandomFile
     * Barcode detection is then done in {@link ScanHub.BLL.util.BarcodeDetector}.
     * @return TIFF as raw bytes
     */
    @Override
    public ScanResult fetchNextScan() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/getRandomFile")).GET().build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        byte[] tiffBytes = extractTiffFromZip(response.body());
        return new ScanResult(tiffBytes, false);
    }

    /**
     * GET /getById
     * Used for fetching a barcode specific file as first file in the first document in a box.
     * @return TIFF as raw bytes
     */
    @Override
    public ScanResult fetchBarcodeFile() throws Exception {
        int[] ids = {3, 7, 13, 16, 21, 27}; // id's of the files with a barcode
        int randomId = ids[new java.util.Random().nextInt(ids.length)];

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/getById/" + randomId)).GET().build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        byte[] tiffBytes = extractTiffFromZip(response.body());
        return new ScanResult(tiffBytes, false);
    }

    /**
     * Extracts files from zip to bytes
     * @return Zip as raw bytes
     */
    private byte[] extractTiffFromZip(byte[] zipBytes) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry = zis.getNextEntry();
            if (entry == null) {
                throw new Exception("API zip response contained no files");
            }
            return zis.readAllBytes();
        }
    }
}
