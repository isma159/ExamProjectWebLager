package ScanHub.DAL.ApiClient;

import ScanHub.DAL.interfaces.IScanSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
                .uri(URI.create(BASE_URL + "/getRandomFile"))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofByteArray());

        return new ScanResult(response.body(), false); // isBarcode always false here cause it is detected later
    }

    /**
     * GET /getById/{id} - for reloading a specific file
     * when displaying/exporting from DB, to bypass storing
     * the full blob locally during a session.
     */
    public byte[] fetchById(int id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/getById/" + id))
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
    }

    /**
     * GET /getCount if we wanted to know how many TIFFs the API has available.
     */
    public int fetchCount() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/getCount"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return Integer.parseInt(response.body().trim());
    }
}
