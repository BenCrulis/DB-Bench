package concretebenchmods;

import benchmark.ModuleAPI;
import benchmod.BenchMod;
import benchresult.ResultRow;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

public class DatabaseUtil {

    enum INDEX_TYPE {
        HASH,
        Test
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

        return ModuleAPI.provideContext(() -> {
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

    public static void test(){

    }

}
