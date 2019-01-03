package benchresult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResultRow {

    private HashMap<String,Object> row;

    public ResultRow(HashMap<String, Object> row) {
        this.row = row;
    }

    public static ResultRow single(String column, String value) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(column, value);
        return new ResultRow(hashMap);
    }

    public static ResultRow emptyRow(){
        return new ResultRow(new HashMap<>());
    }

    public int size(){
        return this.row.size();
    }

    public Set<Map.Entry<String,Object>> entrySet(){
        return this.row.entrySet();
    }

    public Object get(String key){
        return this.row.get(key);
    }

    public Object getOrDefault(String key, Object def){
        return this.row.getOrDefault(key, def);
    }

    public ResultRow put(String key, Object value){
        HashMap<String,Object> newRow = (HashMap<String, Object>) this.row.clone();
        newRow.put(key,value);
        return new ResultRow(newRow);
    }

    public ResultRow putIfAbsent(String key, Object value){
        HashMap<String,Object> newRow = (HashMap<String, Object>) this.row.clone();
        newRow.putIfAbsent(key,value);
        return new ResultRow(newRow);
    }

    public ResultRow augmentWith(ResultRow other) {
        HashMap<String, Object> newRow = (HashMap<String, Object>) this.row.clone();
        for (Map.Entry<String, Object> entry : other.row.entrySet()) {
            newRow.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return new ResultRow(newRow);
    }

    public HashMap<String, Object> getRow() {
        return row;
    }

    @Override
    public String toString() {
        return "ResultRow{" +
                "row=" + row +
                '}';
    }
}
