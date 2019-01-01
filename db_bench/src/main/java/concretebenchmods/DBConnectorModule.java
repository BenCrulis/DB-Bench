package concretebenchmods;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectorModule  {

    public static String POSTGRES_DRIVER_NAME = "org.postgresql.Driver";

    private String url;
    private String user;
    private String password;

    private Connection connection;

    private DBConnectorModule(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DBConnectorModule createDBConnectorModule(String driverClassName,
                                                            String url,
                                                            String user,
                                                            String password) throws ClassNotFoundException {
        Class.forName(driverClassName);
        return new DBConnectorModule(url, user, password);
    }


    public void before() {
        try {

            this.connection = DriverManager.getConnection(this.url, this.user, this.password);

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;

        }

        if (this.connection != null) {

        } else {
            System.out.println("Failed to make connection");
        }


    }

    public void after() {

        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
            }
            this.connection = null;
        }

    }

}
