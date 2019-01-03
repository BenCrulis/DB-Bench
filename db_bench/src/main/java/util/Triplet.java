package util;

public class Triplet<A,B,C> {
    public final A a;
    public final B b;
    public final C c;

    public Triplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A getA(){
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }
}
