package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class CacheDirective implements AppendableToByte {
    private CacheDirectiveSort sort;
    private Long deltaSeconds;
    private ArrayList<String> fieldNameList;

    public CacheDirective() {
        this.sort = CacheDirectiveSort.Unknown;
        deltaSeconds = null;
        fieldNameList = null;
    }

    public CacheDirective(CacheDirectiveSort sort) {
        this.sort = sort;
        deltaSeconds = null;
        createValues();
    }

    private void createValues() {
        if (hasDeltaSeconds()) {
            deltaSeconds = new Long(0);
            fieldNameList = null;
        } else if (hasFieldNameList()) {
            deltaSeconds = null;
            fieldNameList = new ArrayList<String>();
        } else {
            deltaSeconds = null;
            fieldNameList = null;
        }
    }

    private boolean hasDeltaSeconds() {
        return sort == CacheDirectiveSort.MaxAge
                || sort == CacheDirectiveSort.MaxStale
                || sort == CacheDirectiveSort.MinFresh
                || sort == CacheDirectiveSort.SMaxAge;
    }


    private boolean hasFieldNameList() {
        return sort == CacheDirectiveSort.Private ||
                sort == CacheDirectiveSort.NoCache;
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Equal;
        ps.spaceIsSep = false;

        ByteParser.nextItem(value, ps);

        String sort = ps.toString(value);
        setSort(CacheDirectiveSort.fromString(sort));

        if (ByteParser.nextItem(value, ps) >= 0) {
            parseValue(value, ps);
        }

        ParseState.release(ps);
    }

    private void parseValue(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);

        if (hasDeltaSeconds()) {
            long dSec = ByteParser.parseLong(value, ps);
            deltaSeconds = new Long(dSec);
        } else if (hasFieldNameList()) {
            ByteParser.unquote(value, ps);
            ps.separator = HttpString.Comma;
            ps.spaceIsSep = false;

            String fieldName;

            while (ByteParser.nextItem(value, ps) >= 0) {
                fieldName = ps.toString(value);
                if (fieldName != null) {
                    fieldNameList.add(fieldName);
                }
            }
        }
    }

    @Override
    public void append(OutputBuffer buffer) {
        buffer.append(sort.toString());
        if (hasDeltaSeconds() && deltaSeconds != null) {
            buffer.append(HttpString.Equal).appendLong(deltaSeconds);
        } else if (hasFieldNameList() && fieldNameList != null && fieldNameList.size() > 1) {
            buffer.append(HttpString.DQuote)
                    .appendStringArray(HttpString.Comma, fieldNameList.toArray())
                    .append(HttpString.DQuote);
        }
    }

    public CacheDirectiveSort getSort() {
        return sort;
    }

    public void setSort(CacheDirectiveSort sort) {
        this.sort = sort;
        createValues();
    }

    public Long getDeltaSeconds() {
        return deltaSeconds;
    }

    public void setDeltaSeconds(Long deltaSeconds) {
        this.deltaSeconds = deltaSeconds;
    }

    public ArrayList<String> getFieldNameList() {
        return fieldNameList;
    }

}
