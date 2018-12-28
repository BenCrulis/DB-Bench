package benchmod;

import java.sql.Connection;

public abstract class BenchModule<Context> {

    Context context;

    // internal
    public abstract void before();
    public abstract void after();

    // context
    public Context getContext(){ return this.context; }
    public void acceptContext(Context context){ this.context = context; }

    // composition
    public BenchModule asContextFor(BenchModule bm) {
        final BenchModule th = this;
        return new BenchModule() {
            @Override
            public void before() {
                th.before();
                bm.acceptContext(th.getContext());
                bm.before();
            }

            @Override
            public void after() {
                bm.after();
                th.after();
            }

            @Override
            public Object getContext() {
                return th.getContext();
            }
        };
    }

}
