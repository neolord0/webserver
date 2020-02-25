package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.MediaType;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueAccept extends HeaderValue {
    private static final MediaType[] Zero_Array = new MediaType[0];
    private ArrayList<MediaType> mediaTypeList;

    public HeaderValueAccept() {
        mediaTypeList = new ArrayList<MediaType>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Accept;
    }

    @Override
    public void reset() {
        mediaTypeList.clear();
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            MediaType mt = new MediaType();
            try {
                mt.parse(value, ps);

                mediaTypeList.add(mt);
            } catch (ParserException e) {
                e.printStackTrace();
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendArray(HttpString.Comma, mediaTypeList.toArray());
        return buffer.getBytesAndRelease();
    }

    @Override
    public Float getQvalue(String compare) {
        if (compare == null) {
            return (float) -1;
        }
        float qvalue = -1;

        for (MediaType mt : mediaTypeList) {
            if (mt.isMatch(compare)) {
                float mt_qvalue = (mt.qvalue() == null) ? 1 : mt.qvalue();
                qvalue = Math.max(mt_qvalue, qvalue);
            }
        }
        return qvalue;
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Accept) {
            HeaderValueAccept other2 = (HeaderValueAccept) other;
            int includedCount = 0;
            for (MediaType mt : other2.mediaTypeList) {
                if (isInclude(mt)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.mediaTypeList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(MediaType other) {
        for (MediaType mt : mediaTypeList) {
            if (mt.isMatch(other)) {
                return true;
            }
        }
        return false;
    }

    public MediaType addNewMediaType() {
        MediaType mt = new MediaType();
        mediaTypeList.add(mt);
        return mt;
    }

    public MediaType[] mediaTypes() {
        return mediaTypeList.toArray(Zero_Array);
    }
}


