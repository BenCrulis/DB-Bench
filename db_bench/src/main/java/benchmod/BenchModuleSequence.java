package benchmod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BenchModuleSequence<T> extends BenchModule<T> {

    private ArrayList<BenchModule<T>> list;

    private BenchModuleSequence(ArrayList<BenchModule<T>> l){
        this.list = l;
    }

    public static <T> BenchModuleSequence<T> fromModuleList(List<BenchModule<T>> l){
        if (l.stream().anyMatch(x -> x == null)){
            throw new IllegalArgumentException("One or more module is null");
        }
        ArrayList<BenchModule<T>> list =  new ArrayList<>();
        list.addAll(l);
        return new BenchModuleSequence(list);
    }

    public static <T> BenchModuleSequence<T> fromModuleList(BenchModule<T>... l){
        return fromModuleList(Arrays.asList(l));
    }

    public void addToSequence(BenchModule<T> bm) {
        if (bm == null) {
            throw new IllegalArgumentException("Benchmark module cannot be null");
        }
        this.list.add(bm);
    }

    @Override
    public void before() {
        for (BenchModule<T> bm : this.list){
            bm.acceptContext(this.getContext());
            bm.before();
            bm.after();
        }
    }

    @Override
    public void after() {

    }

}
