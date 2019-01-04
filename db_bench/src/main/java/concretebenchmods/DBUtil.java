package concretebenchmods;

import benchmark.API;
import benchmod.BenchMod;
import benchresult.ResultRow;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.HashMap;

public class DBUtil {

    public enum INDEX_TYPE {
        hash,
        btree
    }

    public static String POSTGRES_DRIVER_NAME = "org.postgresql.Driver";

    public static BenchMod.ContextProvider<Void, Connection> postgresContext(String host, String db, String user, String password) {
        try {
            Class.forName(POSTGRES_DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Driver "+POSTGRES_DRIVER_NAME+" not found.");
        }

        String url = "jdbc:postgresql://"+host+":5432/"+db;

        return API.provideContext(() -> {
            Connection conn = null;

            System.out.println("Connecting to database.");
            try {
                conn = DriverManager.getConnection(url,user,password);
            } catch (SQLException e) {
                System.out.println("Connection Failed! Check output console");
                e.printStackTrace();
            }
            return conn;

        }, (Connection conn) -> {
            System.out.println("Closing connection to database.");
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static BenchMod.Module<Connection> query(String rawQuery){
        return new BenchMod.Module<>((connection) -> {
            try {
                HashMap<String,Object> results = new HashMap<>();

                CallableStatement preparedCall = connection.prepareCall("EXPLAIN (ANALYSE, FORMAT JSON) "+rawQuery);

                ResultSet resultSet = preparedCall.executeQuery();

                resultSet.next();
                String obj = resultSet.getString(1);

                JSONArray jsonArray = new JSONArray(obj);

                JSONObject jsonObject = jsonArray.getJSONObject(0);

                results.put("jsonPlan", jsonObject);

                float planningTime = jsonObject.getFloat("Planning Time");
                float execTime = jsonObject.getFloat("Execution Time");

                results.put("PlanningTime(ms)", planningTime);
                results.put("ExecutionTime(ms)", execTime);

                if (jsonObject.has("Plan")){
                    results.put("TotalCost(ms)", jsonObject.getJSONObject("Plan").getFloat("Total Cost"));
                }

                preparedCall.close();

                return new ResultRow(results);
            } catch (SQLException e) {
                e.printStackTrace();
                return ResultRow.single("error", "yes");
            }
        });
    }

    public static BenchMod.ContextProvider<Connection, Connection> indexContext(INDEX_TYPE index_type, String columns, String table, String name, boolean unique){
        return API.passContext((connection) -> {
                    try {
                        PreparedStatement preparedStatement = connection.prepareStatement(
                                "CREATE "+(unique?"UNIQUE":"")+" INDEX "+name+" on "+table+" USING "+index_type+ " ("+columns+");");
                        preparedStatement.execute();
                        preparedStatement.close();
                        System.out.println("Created index "+name);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                },
                (connection) -> {
                    PreparedStatement preparedStatement = null;
                    try {
                        preparedStatement = connection.prepareStatement("DROP INDEX "+name+";");
                        preparedStatement.execute();
                        preparedStatement.close();
                        System.out.println("Deleted index "+name);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                });
    }

    public static BenchMod.ContextProvider<Connection,Connection> foreignKeyContext(String name, String table, String column, String reference){
        return API.passContext((connection) -> {
                    try {
                        PreparedStatement preparedStatement = connection.prepareStatement(
                                "ALTER TABLE "+table+" ADD CONSTRAINT "+name+" FOREIGN KEY ("+column+") REFERENCES "+reference+";");
                        preparedStatement.execute();
                        preparedStatement.close();
                        System.out.println("Created foreign key "+name);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                },
        (connection) -> {
            String sql = "ALTER TABLE "+table+" DROP CONSTRAINT "+name+";";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.execute();
                preparedStatement.close();
                System.out.println("Deleted foreign key "+name);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } );
    }

}
