package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueContentLanguage extends HeaderValue {
    private static final String[] Zero_Array = new String[0];
    private ArrayList<String> languageTagList;

    public HeaderValueContentLanguage() {
        languageTagList = new ArrayList<String>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Content_Language;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        String languageTag;
        while (ByteParser.nextItem(value, ps) >= 0) {
            languageTag = ps.toString(value);

            if (languageTag != null) {
                languageTagList.add(languageTag);
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendStringArray(HttpString.Comma, languageTagList.toArray());
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    public void addLanguageTag(String languageTag) {
        languageTagList.add(languageTag);
    }

    public String[] languageTags() {
        return languageTagList.toArray(Zero_Array);
    }
}
