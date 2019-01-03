package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class VirtualCSV
{
    public List<String> columnsHeaders;
    public List<List<String>> columns;
    int rows = 0;
    String separator = ",";

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public VirtualCSV(String... columnNames){
        columnsHeaders = new ArrayList<>(Arrays.asList(columnNames));
        this.columns = new ArrayList<>(columnsHeaders.size());
    }

    public void addColumn(String columnName){
        columnsHeaders.add(columnName);
        columns.add(new ArrayList<>(Collections.nCopies(rows, "null")));
    }

    public void addColumns(String... columnNames ){
        Arrays.stream(columnNames).forEach(this::addColumn);
    }

    public void addRow(HashMap<String, Object> row){
        Set<String> requested = new HashSet<>(row.keySet());

        //check if something is new
        if (!columnsHeaders.containsAll(requested)){
            requested.removeAll(columnsHeaders);
            addColumns(requested.toArray(new String[requested.size()]));
        }

        row.forEach((key, value) -> columns.get(columnsHeaders.indexOf(key)).add(value.toString()));

        // Fill in the nulls
        if (!requested.containsAll(columnsHeaders)){
            HashSet<String> toNull = new HashSet<>(columnsHeaders);
            toNull.remove(row.keySet());
            toNull.forEach(col -> columns.get(columnsHeaders.indexOf(col)).add("null"));
        }

        rows++;
    }

    public void save(String path){
        int c = columnsHeaders.size();
        try {
            PrintWriter out = new PrintWriter(new File(path));
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < c - 1; j++) {
                    out.print(columns.get(j).get(i) + ",");
                }
                out.println(columns.get(c - 1).get(i));
            }
            out.close();
        } catch (FileNotFoundException e) {
            System.err.printf("Path '%s' to save the CSV is invalid.%n", path);
        }
    }

}
