package kr.dogfoot.webserver.server.resource.performer.util;

import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueRange;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.Range;

import java.util.ArrayList;

public class ContentRangeInfoMaker {
    public static ContentRangeInfo make(HeaderValueRange range, long contentLength) {
        if (range.rangeCount() == 0 || contentLength == 0) {
            return null;
        }

        ContentRange[] contentRanges = makeContentRangeArray(range.rangeList(), contentLength);
        if (invalid(contentRanges)) {
            return new ContentRangeInfo();
        } else {
            sort(contentRanges);
            merge(contentRanges);

            return new ContentRangeInfo()
                    .unit(range.unit())
                    .instanceLength(contentLength)
                    .setRanges(contentRanges);
        }
    }

    private static boolean invalid(ContentRange[] contentRanges) {
        for (ContentRange cr : contentRanges) {
            if (cr == null) {
                return true;
            }
        }
        return false;
    }

    private static ContentRange[] makeContentRangeArray(ArrayList<Range> rangeList, long contentLength) {
        ContentRange[] contentRanges = new ContentRange[rangeList.size()];
        int count = rangeList.size();
        for (int index = 0; index < count; index++) {
            contentRanges[index] = toContentRange(rangeList.get(index), contentLength);
        }
        return contentRanges;
    }

    private static ContentRange toContentRange(Range range, long contentLength) {
        long firstPos = -1;
        long lastPos = -1;

        if (range.isSuffix() == false) {
            if (range.firstPos() >= contentLength) {
                return null;
            }
            if (range.lastPos() != null && range.firstPos() > range.lastPos()) {
                return null;
            }

            firstPos = range.firstPos();
            if (range.lastPos() == null) {
                lastPos = contentLength - 1;
            } else {
                if (range.lastPos() >= contentLength) {
                    lastPos = contentLength - 1;
                } else {
                    lastPos = range.lastPos();
                }
            }
        } else {
            if (range.lastPos() > contentLength) {
                firstPos = 0;
            } else {
                firstPos = contentLength - range.lastPos();
            }
            lastPos = contentLength - 1;
        }
        return new ContentRange(firstPos, lastPos);
    }


    private static void sort(ContentRange[] contentRanges) {
        int count = contentRanges.length;
        for (int i = 0; i < count - 1; i++) {
            for (int j = i + 1; j < count; j++) {
                if (contentRanges[i].firstPos() > contentRanges[j].firstPos()) {
                    ContentRange temp = contentRanges[j];
                    contentRanges[j] = contentRanges[i];
                    contentRanges[i] = temp;
                }
            }
        }
    }

    private static void merge(ContentRange[] contentRanges) {
        int count = contentRanges.length;
        for (int i = 0; i < count - 1; i++) {
            if (contentRanges[i] != null) {
                for (int j = i + 1; j < count; j++) {
                    if (contentRanges[j] != null) {
                        if (contentRanges[i].merge(contentRanges[j])) {
                            contentRanges[j] = null;
                        }
                    }
                }
            }
        }
    }
}
