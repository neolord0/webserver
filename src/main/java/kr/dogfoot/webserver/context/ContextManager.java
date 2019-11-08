package kr.dogfoot.webserver.context;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ContextManager {
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
        usedContexts.add(context);

        context.resetForPooled();

        return context;
    }

    public void releaseAll() throws IOException {
        usedContexts.clear();
        contextPool.clear();
    }

    public void release(Context context) {
        context.changeState(ContextState.Released);
        toPool(context);
    }

    private void toPool(Context context) {
        usedContexts.remove(context);
        contextPool.add(context);
    }
}
