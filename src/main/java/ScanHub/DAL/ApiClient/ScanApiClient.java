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
     * GET /getRandomFile - returns a random TIFF as raw bytes.
     * Barcode detection is then done in {@link ScanHub.BLL.util.BarcodeDetector}.
     */
    @Override
    public ScanResult fetchNextScan() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/getRandomFile")).GET().build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        byte[] tiffBytes = extractTiffFromZip(response.body());
        return new ScanResult(tiffBytes, false);
    }

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
