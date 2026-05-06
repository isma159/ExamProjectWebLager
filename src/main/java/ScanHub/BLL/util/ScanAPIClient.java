package ScanHub.BLL.util;

// java imports
import java.net.http.HttpClient;

public class ScanAPIClient {
    private final static String BASE_URL = "https://studentiffapi-production.up.railway.app";
    private final HttpClient httpClient = HttpClient.newHttpClient();


}
