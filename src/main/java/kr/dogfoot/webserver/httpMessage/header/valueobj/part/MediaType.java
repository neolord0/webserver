package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

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

                    if (p.getName().equalsIgnoreCase(HttpString.Q)) {
                        qvalue = new Float(p.getValue());
                    } else if (p.getName().equalsIgnoreCase(HttpString.Charset_String)) {
                        charset = p.getValue();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public Float getQvalue() {
        return qvalue;
    }

    public void setQvalue(Float qvalue) {
        this.qvalue = qvalue;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }


    public ArrayList<Parameter> getParameterList() {
        return parameterList;
    }

    public String getParameterValue(String name) {
        if (name == null) {
            return null;
        }
        for (Parameter p : parameterList) {
            if (name.equalsIgnoreCase(p.getName())) {
                return p.getValue();
            }
        }
        return null;
    }

    public long getWriteSize() {
        long size = 0;
        size += type.length() + 1 + subtype.length();
        if (qvalue != null) {
            size += 1;
            OutputBuffer.getWriteSize_QValue(qvalue);
        }
        for (Parameter p : parameterList) {
            size += 1;
            size += p.getWriteSize();
        }
        return size;
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
}