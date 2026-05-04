package ScanHub.DAL;

import java.net.http.HttpClient;

public class ScanAPIClient {

    private static final String URL = "https://studentiffapi-production.up.railway.app";
    private HttpClient httpClient = HttpClient.newHttpClient();


}
