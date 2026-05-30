package ScanHub.DAL.DB;

//project imports
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

//java imports
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

public class DBConnector {
    private static final String PROP_FILE = "config/config.settings";
    private static SQLServerDataSource dataSource;

    static {
        try {
            dataSource = createDataSource();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static SQLServerDataSource createDataSource() throws IOException {
        Properties databaseProperties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(PROP_FILE)) {
            databaseProperties.load(inputStream);
        }

        SQLServerDataSource dataSource = new SQLServerDataSource();
        dataSource.setServerName(databaseProperties.getProperty("Server"));
        dataSource.setDatabaseName(databaseProperties.getProperty("Database"));
        dataSource.setUser(databaseProperties.getProperty("User"));
        dataSource.setPassword(databaseProperties.getProperty("Password"));
        dataSource.setPortNumber(1433);
        dataSource.setTrustServerCertificate(true);
        return dataSource;
    }

    public static Connection getConnection() throws SQLServerException {
        return dataSource.getConnection();
    }
}

