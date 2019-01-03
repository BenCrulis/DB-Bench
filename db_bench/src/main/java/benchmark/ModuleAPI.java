package benchmark;

import benchmod.BenchMod;
import benchresult.ResultRow;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModuleAPI {

    public static <A,B> Iterable<ResultRow> iterate(BenchMod<A,B> benchMod, A context){
        return () -> benchMod.exec(context).a;
    }

    public static <A> Iterable<ResultRow> iterate(BenchMod<Void, A> benchMod){
        return iterate(benchMod, null);
    }

    public static <A> BenchMod.Module<A> module(Function<A, ResultRow> function){
        return new BenchMod.Module<>(function);
    }

    public static <A,B> BenchMod.ContextProvider<A,B> context(Function<A,B> supplier, BiConsumer<A,B> biConsumer) {
        return new BenchMod.ContextProvider<>(supplier,biConsumer);
    }

    public static <A> BenchMod.ContextProvider<Void,A> provideContext(Supplier<A> supplier, Consumer<A> destructor) {
        return new BenchMod.ContextProvider<>((x) -> supplier.get(), (a,b) -> destructor.accept(b));
    }

    public static <A> BenchMod.Sequence<A> sequence(BenchMod<A,Void>... benchMods ){
        return BenchMod.Sequence.sequence(benchMods);
    }

    public static <A,B> BenchMod.Tag<A,B> tag(String tagName, String tagValue, BenchMod<A,B> benchMod) {
        return new BenchMod.Tag<>(tagName, tagValue, benchMod);
    }

    public static <A,B,C> BenchMod.AsContext<A,B,C> asContext(BenchMod<A,B> contextMod, BenchMod<B,C> internalMod) {
        return new BenchMod.AsContext<>(contextMod, internalMod);
    }

    public static <A,B> BenchMod.AsContext<A,B,Void> asContext(Function<A,B> supplier, BiConsumer<A,B> biConsumer, BenchMod<B,Void> benchMod) {
        return new BenchMod.AsContext<>(context(supplier,biConsumer), benchMod);
    }
}