package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.string.StringUtils;

import java.util.ArrayList;

public class MediaType implements AppendableToByte {
    private String type;
    private String subtype;

    private Float qvalue;
    private String charset;
    private ArrayList<Parameter> parameterList;

    public MediaType() {
        parameterList = new ArrayList<Parameter>();
    }

    public MediaType(String type, String subtype) {
        this.type = type;
        this.subtype = subtype;
        this.parameterList = new ArrayList<Parameter>();
    }

    public void reset() {
        type = null;
        subtype = null;
        qvalue = null;
        charset = null;
        parameterList.clear();
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Semicolon;
        ps.spaceIsSep = false;

        boolean isFirst = true;
        while (ByteParser.nextItem(value, ps) >= 0) {
            if (isFirst) {
                parseMediaRange(value, ps);
                isFirst = false;
            } else {
                Parameter p = new Parameter();
                try {
                    p.parse(value, ps);

                    if (p.name().equalsIgnoreCase(HttpString.Q)) {
                        qvalue = new Float(p.value());
                    } else if (p.name().equalsIgnoreCase(HttpString.Charset_String)) {
                        charset = p.value();
                    } else {
                        parameterList.add(p);
                    }
                } catch (ParserException e) {
                    e.printStackTrace();
                }
            }
        }
        ParseState.release(ps);
    }

    private void parseMediaRange(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Slash;
        ps.spaceIsSep = false;
        ps.permitNoSeparator = false;

        if (ByteParser.nextItem(value, ps) < 0) {
            throw new ParserException("no media sub_type");
        }
        type = ps.toString(value);
        ps.rest();
        subtype = ps.toString(value);
        ParseState.release(ps);
    }

    @Override
    public void append(OutputBuffer buffer) {
        if (subtype != null) {
            buffer.append(type, HttpString.Slash, subtype);
        } else {
            buffer.append(type);
        }
        if (qvalue != null) {
            buffer.append(HttpString.Semicolon)
                    .appendQValue(qvalue);
        }
        if (charset != null) {
            buffer.append(HttpString.Semicolon)
                    .append(HttpString.Charset, HttpString.Equal, charset.getBytes());
        }
        for (Parameter p : parameterList) {
            buffer.append(HttpString.Semicolon);
            p.append(buffer);
        }
    }

    public boolean isMatch(MediaType other) {
        if ("*".equals(type) && "*".equals(subtype)) {
            return true;
        }
        if ("*".equals(subtype)) {
            return StringUtils.equalsWithNull(type, other.type);
        } else {
            return StringUtils.equalsWithNull(type, other.type)
                    && StringUtils.equalsWithNull(subtype, other.subtype);
        }
    }

    public boolean isMatch(String compare) {
        if ("*".equals(type) && "*".equals(subtype)) {
            return true;
        }

        String[] types = compare.split(HttpString.Slash_String);
        if (type != null && "*".equals(subtype)) {
            return type.equals(types[0]);
        } else if (type != null && subtype != null) {
            return types.length == 2 && type.equals(types[0]) && subtype.equals(types[1]);
        }
        return false;
    }


    public String type() {
        return type;
    }

    public void type(String type) {
        this.type = type;
    }

    public String subtype() {
        return subtype;
    }

    public void subtype(String subtype) {
        this.subtype = subtype;
    }

    public Float qvalue() {
        return qvalue;
    }

    public void qvalue(Float qvalue) {
        this.qvalue = qvalue;
    }

    public String charset() {
        return charset;
    }

    public void charset(String charset) {
        this.charset = charset;
    }

    public ArrayList<Parameter> parameterList() {
        return parameterList;
    }
}