package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.string.StringUtils;

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
    public void reset() {
        languageTagList.clear();
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
        return buffer.getBytes();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Content_Language) {
            HeaderValueContentLanguage other2 = (HeaderValueContentLanguage) other;
            int includedCount = 0;
            for (String lt : other2.languageTagList) {
                if (isInclude(lt)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.languageTagList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(String other) {
        for (String lt : languageTagList) {
            if (StringUtils.equalsIgnoreCaseWithNull(lt, other)) {
                return true;
            }
        }
        return false;
    }

    public void addLanguageTag(String languageTag) {
        languageTagList.add(languageTag);
    }

    public String[] languageTags() {
        return languageTagList.toArray(Zero_Array);
    }
}
