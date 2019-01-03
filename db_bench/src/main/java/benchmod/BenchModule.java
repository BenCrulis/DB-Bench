package benchmod;

import benchresult.ResultRow;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

interface BenchModule<Context> {

    interface ModuleVisitor<R,C> {

        <T> R visit(Contexted<T> contexted);
        R visit(PassiveContextProvider<C> passiveContextProvider);
        R visit(Module module);
        R visit(NeedContextModule<C> needContextModule);
        R visit(Repeat<C> repeatModule);
        R visit(Tag<C> tagModule);
    }

    <R> R accept(ModuleVisitor<R,Context> visitor);


    class BenchModuleIterableProvider<C> implements ModuleVisitor<Function<C,Iterator<ResultRow>>, C> {

        @Override
        public <T> Function<C, Iterator<ResultRow>> visit(Contexted<T> contexted) {
            return null;
        }

        @Override
        public Function<C, Iterator<ResultRow>> visit(PassiveContextProvider<C> passiveContextProvider) {
            return null;
        }

        @Override
        public Function<C, Iterator<ResultRow>> visit(Module module) {
            return null;
        }

        @Override
        public Function<C, Iterator<ResultRow>> visit(NeedContextModule<C> needContextModule) {
            return null;
        }

        @Override
        public Function<C, Iterator<ResultRow>> visit(Repeat<C> repeatModule) {
            return null;
        }

        @Override
        public Function<C, Iterator<ResultRow>> visit(Tag<C> tagModule) {
            return null;
        }
    }

    class BenchModuleIterable implements ModuleVisitor<Iterator<ResultRow>, Void> {

        @Override
        public <C> Iterator<ResultRow> visit(Contexted<C> contexted) {
            C context = contexted.provider.get();
            BenchModuleIterableProvider<C> benchModuleIterableProvider = new BenchModuleIterableProvider<>();
            Function<C, Iterator<ResultRow>> iteratorProvider = contexted.module.accept(benchModuleIterableProvider);
            Iterator<ResultRow> iterator = iteratorProvider.apply(context);

            return new Iterator<ResultRow>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public ResultRow next() {
                    ResultRow nextRow = iterator.next();

                    if (!iterator.hasNext()){
                        contexted.destructor.apply(context);
                    }

                    return nextRow;
                }
            };
        }

        @Override
        public Iterator<ResultRow> visit(PassiveContextProvider<Void> passiveContextProvider) {
            passiveContextProvider.before.accept(null);

            Iterator<ResultRow> iterator = passiveContextProvider.accept(new BenchModuleIterable());

            return new Iterator<ResultRow>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public ResultRow next() {
                    ResultRow nextRow = iterator.next();

                    if (!iterator.hasNext()){
                        passiveContextProvider.after.accept(null);
                    }

                    return nextRow;
                }
            };
        }

        @Override
        public Iterator<ResultRow> visit(Module module) {
            return null;
        }

        @Override
        public Iterator<ResultRow> visit(NeedContextModule<Void> needContextModule) {
            return null;
        }

        @Override
        public Iterator<ResultRow> visit(Repeat<Void> repeatModule) {
            return null;
        }

        @Override
        public Iterator<ResultRow> visit(Tag<Void> tagModule) {
            return null;
        }
    }

    class Module implements BenchModule<Void> {

        Supplier<ResultRow> supplier;

        public Module(Supplier<ResultRow> supplier) {
            this.supplier = supplier;
        }

        @Override
        public <R> R accept(ModuleVisitor<R,Void> visitor) {
            return visitor.visit(this);
        }
    }

    class NeedContextModule<C> implements BenchModule<C> {

        private Function<C,ResultRow> benchFunction;

        public NeedContextModule(Function<C, ResultRow> benchFunction) {
            this.benchFunction = benchFunction;
        }

        public ResultRow execute(C context) {
            return this.benchFunction.apply(context);
        }

        @Override
        public <R> R accept(ModuleVisitor<R, C> visitor) {
            return visitor.visit(this);
        }
    }

    class PassiveContextProvider<C> implements BenchModule<C> {
        private Consumer<C> before;
        private Consumer<C> after;
        private BenchModule<C> module;

        public PassiveContextProvider(Consumer<C> before, Consumer<C> after, BenchModule<C> module) {
            this.before = before;
            this.after = after;
            this.module = module;
        }

        @Override
        public <R> R accept(ModuleVisitor<R, C> visitor) {
            return visitor.visit(this);
        }
    }

    class Contexted<T> implements BenchModule<Void> {

        private Supplier<T> provider;
        private Function<T,Void> destructor;

        private BenchModule<T> module;

        public Contexted(Supplier<T> provider, Function<T, Void> destructor, BenchModule<T> module) {
            this.provider = provider;
            this.destructor = destructor;
            this.module = module;
        }

        @Override
        public <R> R accept(ModuleVisitor<R,Void> visitor) {
            return visitor.visit(this);
        }
    }

    class Repeat<T> implements BenchModule<T> {

        public final int repeats;
        public final String label;
        public final BenchModule<T> benchModule;

        public Repeat(int repeats, String label, BenchModule<T> benchModule) {
            this.repeats = repeats;
            this.label = label;
            this.benchModule = benchModule;
        }

        @Override
        public <R> R accept(ModuleVisitor<R,T> visitor) {
            return visitor.visit(this);
        }
    }

    class Tag<T> implements BenchModule<T> {

        public final String tagName;
        public final String tag;
        public final BenchModule<T> benchModule;

        public Tag(String tagName, String tag, BenchModule<T> benchModule) {
            this.tagName = tagName;
            this.tag = tag;
            this.benchModule = benchModule;
        }

        @Override
        public <R> R accept(ModuleVisitor<R,T> visitor) {
            return visitor.visit(this);
        }
    }


    // [CompleteModule(ContextModule(thing::BenchProcess<A>,NeedContextModule(\c::A -> ...)))]



}
