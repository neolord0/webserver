package kr.dogfoot.webserver.context;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ContextManager {
    private static Context[] ZeroArray = new Context[0];
    private ConcurrentLinkedQueue<Context> contextPool;
    private ConcurrentLinkedQueue<Context> usedContexts;

    public ContextManager() {
        contextPool = new ConcurrentLinkedQueue<Context>();
        usedContexts = new ConcurrentLinkedQueue<Context>();
    }

    public Context pooledObject() {
        Context context = contextPool.poll();
        if (context == null) {
            context = new Context();
        }

        context.changeState(ContextState.Waiting);

        usedContexts.add(context);
        return context;
    }

    public void release(Context context) {
        context.resetForRelease();
        toPool(context);
    }

    private void toPool(Context context) {
        usedContexts.remove(context);
        contextPool.add(context);
    }

    public Context[] usedContexts() {
        return usedContexts.toArray(ZeroArray);
    }
}
