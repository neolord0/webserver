package kr.dogfoot.webserver.server.cache;

import java.util.Comparator;
import java.util.TreeSet;

public class StoredResponseSet extends TreeSet<StoredResponse> {
    public StoredResponseSet() {
        super(new Comparator<StoredResponse>() {
            @Override
            public int compare(StoredResponse o1, StoredResponse o2) {
                if (o2.date() > o1.date()) {
                    return 1;
                } else if (o2.date() < o1.date()) {
                    return -1;
                }
                return 0;
            }
        });
    }
}
