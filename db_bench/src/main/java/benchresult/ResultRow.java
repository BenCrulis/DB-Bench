package benchresult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResultRow {

    private HashMap<String,Object> row;

    public ResultRow(HashMap<String, Object> row) {
        this.row = row;
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
        HashMap<String,Object> newRow = (HashMap<String, Object>) this.row.clone();
        for (Map.Entry<String, Object> entry : other.row.entrySet()){
            newRow.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return new ResultRow(newRow);
    }
}
