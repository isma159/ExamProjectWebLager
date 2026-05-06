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

    public DBConnector() throws IOException
    {
        synchronized (DBConnector.class) {
            if (dataSource == null) {
                dataSource = createDataSource();
            }
        }
    }

    private static SQLServerDataSource createDataSource() throws IOException {
        Properties databaseProperties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(new File(PROP_FILE))) {
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

    public Connection getConnection() throws SQLServerException {
        return dataSource.getConnection();
    }

    public static void main(String[] args) throws Exception {
        DBConnector databaseConnector = new DBConnector();
        try (Connection connection = databaseConnector.getConnection()) {
            System.out.println("Is it open? " + !connection.isClosed());
        }
    }//Connection gets closed here
}

