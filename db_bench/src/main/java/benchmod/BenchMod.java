package benchmod;

import benchresult.ResultRow;
import util.Iterators;
import util.Triplet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.*;

public interface BenchMod<A,B> {

    Triplet<Iterator<ResultRow>, Supplier<B>, Consumer<B>> exec(A a);

    interface UseContextOnly<C> extends BenchMod<C, Void> {

        Iterator<ResultRow> getIterator(C context);

        @Override
        default Triplet<Iterator<ResultRow>, Supplier<Void>, Consumer<Void>> exec(C c) {
            return new Triplet<>(getIterator(c), () -> null, (x) -> {return;});
        }
    }

    class ContextProvider<A,B> implements BenchMod<A,B> {

        private Function<A,B> supplier;
        private BiConsumer<A,B> consumer;

        public ContextProvider(Function<A, B> supplier, BiConsumer<A, B> consumer) {
            this.supplier = supplier;
            this.consumer = consumer;
        }

        @Override
        public Triplet<Iterator<ResultRow>, Supplier<B>, Consumer<B>> exec(A a) {
            return new Triplet<>(Collections.emptyIterator(), () -> supplier.apply(a), (b) -> consumer.accept(a,b));
        }
    }

    class Module<C> implements UseContextOnly<C> {

        private Function<C,ResultRow> benchFunction;

        public Module(Function<C, ResultRow> benchFunction) {
            this.benchFunction = benchFunction;
        }

        @Override
        public Iterator<ResultRow> getIterator(C context) {
            return new Iterator<ResultRow>() {
                boolean notSent = true;
                @Override
                public boolean hasNext() {
                    return notSent;
                }

                @Override
                public ResultRow next() {
                    notSent = false;
                    return benchFunction.apply(context);
                }
            };
        }
    }


    class Sequence<A> implements UseContextOnly<A> {

        private List<BenchMod<A,Void>> benchMods;

        public Sequence(BenchMod<A, Void>[] benchMods) {
            this.benchMods = Arrays.asList(benchMods);
        }

        public Sequence(List<BenchMod<A,Void>> benchMods) {
            this.benchMods = benchMods;
        }

        public static <T> Sequence<T> sequence(BenchMod<T,Void>... benchMods){
            return new BenchMod.Sequence<>(benchMods);
        }

        @Override
        public Iterator<ResultRow> getIterator(A a) {

            Iterator<BenchMod<A,Void>> iterator = this.benchMods.iterator();

            return new Iterator<ResultRow>() {
                Iterator<ResultRow> actual = iterator.hasNext() ? iterator.next().exec(a).getA() : null;

                @Override
                public boolean hasNext() {

                    if (actual == null) {
                        return false;
                    }

                    while (!actual.hasNext() && iterator.hasNext() ){
                        actual = iterator.next().exec(a).getA();
                    }

                    return actual.hasNext();
                }

                @Override
                public ResultRow next() {
                    if (actual == null || !actual.hasNext()) {
                        Triplet<Iterator<ResultRow>, Supplier<Void>, Consumer<Void>> triplet = iterator.next().exec(a);

                        actual = triplet.a;
                    }

                    return actual.next();
                }
            };
        }
    }

    class AsContext<A,B,C> implements BenchMod<A,C> {

        private BenchMod<A,B> contextModule;
        private BenchMod<B,C> internalModule;

        public AsContext(BenchMod<A, B> contextModule, BenchMod<B, C> internalModule) {
            this.contextModule = contextModule;
            this.internalModule = internalModule;
        }


        @Override
        public Triplet<Iterator<ResultRow>, Supplier<C> ,Consumer<C>> exec(A context) {
            Triplet<Iterator<ResultRow>, Supplier<B>, Consumer<B>> contextTriplet = this.contextModule.exec(context);
            B b = contextTriplet.b.get();

            Triplet<Iterator<ResultRow>, Supplier<C>, Consumer<C>> internalTriplet = this.internalModule.exec(b);

            Iterator<ResultRow> iterator = Iterators.concat(contextTriplet.a, internalTriplet.a);

            return new Triplet<>(new Iterator<ResultRow>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public ResultRow next() {
                    ResultRow resultRow = iterator.next();

                    if (!iterator.hasNext()) {
                        contextTriplet.c.accept(b);
                    }

                    return resultRow;
                }
            },internalTriplet.b, internalTriplet.c);
        }
    }

    class Tag<A,B> implements BenchMod<A,B> {

        private String tagName;
        private String tagValue;

        private BenchMod<A,B> benchMod;

        public Tag(String tagName, String tagValue, BenchMod<A, B> benchMod) {
            this.tagName = tagName;
            this.tagValue = tagValue;
            this.benchMod = benchMod;
        }

        @Override
        public Triplet<Iterator<ResultRow>, Supplier<B>, Consumer<B>> exec(A a) {
            Triplet<Iterator<ResultRow>, Supplier<B>, Consumer<B>> triplet = this.benchMod.exec(a);

            return new Triplet<>(Iterators.map((rr) -> rr.put(this.tagName, this.tagValue), triplet.a), triplet.b, triplet.c);
        }
    }

    class Repeat<A> implements UseContextOnly<A> {

        private int repeat;
        private String iterTag;
        private UseContextOnly<A> benchMod;

        public Repeat(int repeat, String iterTag, UseContextOnly<A> benchMod) {
            this.repeat = repeat;
            this.iterTag = iterTag;
            this.benchMod = benchMod;
        }

        @Override
        public Iterator<ResultRow> getIterator(A context) {
            return new Iterator<ResultRow>() {
                int i = 1;

                Iterator<ResultRow> actual = null;

                @Override
                public boolean hasNext() {
                    if (actual != null && actual.hasNext()) {
                        return true;
                    }

                    return i <= repeat;
                }

                @Override
                public ResultRow next() {
                    if (actual == null){
                        actual = benchMod.getIterator(context);
                    }

                    ResultRow resultRow = actual.next().put(iterTag, i);

                    if (!actual.hasNext()){
                        i += 1;
                        actual = null;
                    }

                    return resultRow;
                }
            };
        }
    }

    class ErrorCatch<A> implements UseContextOnly<A> {

        BenchMod<A,Void> benchMod;
        Function<Exception,ResultRow> catchFunction;

        public ErrorCatch(BenchMod<A, Void> benchMod, Function<Exception, ResultRow> catchFunction) {
            this.benchMod = benchMod;
            this.catchFunction = catchFunction;
        }

        @Override
        public Iterator<ResultRow> getIterator(A context) {
            ErrorCatch<A> th = this;

            try {
                Triplet<Iterator<ResultRow>, Supplier<Void>, Consumer<Void> > triplet = this.benchMod.exec(context);

                Iterator<ResultRow> iterator = triplet.getA();

                return new Iterator<ResultRow>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public ResultRow next() {
                        return iterator.next();
                    }


                };
            }
            catch (Exception e) {
                return new Iterator<ResultRow>() {
                    boolean notSent = true;

                    @Override
                    public boolean hasNext() {
                        return notSent;
                    }

                    @Override
                    public ResultRow next() {
                        notSent = false;

                        return th.catchFunction.apply(e);
                    }
                };
            }

        }

    }

}
