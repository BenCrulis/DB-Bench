package util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class Iterators {

    public static <A> Iterator<A> concat(Iterator<A>... iterators) {

        List<Iterator<A>> iteratorList = Arrays.asList(iterators);

        Iterator<Iterator<A>> nested = iteratorList.iterator();

        return Iterators.concat(nested);
    }

    public static <A> Iterator<A> concat(Iterator<Iterator<A>> iterators) {

        Iterator<Iterator<A>> nested = iterators;

        return new Iterator<A>() {

            Iterator<A> actual = nested.hasNext() ? nested.next() : null;

            @Override
            public boolean hasNext() {

                if (actual == null) {
                    return false;
                }

                while (!actual.hasNext() && nested.hasNext() ){
                    actual = nested.next();
                }

                return actual.hasNext();
            }

            @Override
            public A next() {
                if (actual == null || !actual.hasNext()){
                    actual = nested.next();
                }

                return actual.next();
            }
        };
    }

    public static <A,B> Iterator<B> map(Function<A,B> function, Iterator<A> iterator) {
        return new Iterator<B>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public B next() {
                return function.apply(iterator.next());
            }
        };
    }

    public static <A,B> Iterator<B> concatMap(Function<A,Iterator<B>> function, Iterator<A> iterator) {
        return Iterators.concat(map(function, iterator));
    }

}
