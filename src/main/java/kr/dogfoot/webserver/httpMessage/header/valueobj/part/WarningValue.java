package kr.dogfoot.webserver.httpMessage.header.valueobj.part;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpDateMaker;
import kr.dogfoot.webserver.util.http.HttpString;

public class WarningValue implements AppendableToByte {
    private WarnCodeSort code;
    private String agent;
    private String text;
    private Long date;

    public WarningValue() {
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Space;
        ps.spaceIsSep = true;
        ps.permitNoSeparator = false;

        if (ByteParser.nextItem(value, ps) < 0) {
            throw new ParserException("Invalid warning value");
        }
        code = WarnCodeSort.fromString(ps.toString(value));

        if (ByteParser.nextItem(value, ps) < 0) {
            throw new ParserException("Invalid warning value");
        }
        agent = ps.toString(value);

        ps.permitNoSeparator = true;
        if (ByteParser.nextItem(value, ps) > 0) {
            ParseState ps2 = ParseState.pooledObject();
            ps2.prepare(ps);
            ByteParser.unquote(value, ps2);
            text = ps2.toString(value);
            ParseState.release(ps2);
        }

        if (ByteParser.nextItem(value, ps) > 0) {
            ParseState ps2 = ParseState.pooledObject();
            ps2.prepare(ps);
            ByteParser.unquote(value, ps2);
            date = ByteParser.parseDate(value, ps2);
            ParseState.release(ps2);
        }
        ParseState.release(ps);
    }

    @Override
    public void append(OutputBuffer buffer) {
        if (code != null) {
            buffer.append(code.toString()).append(HttpString.Space).append(agent);
            if (text != null) {
                buffer.append(HttpString.Space).appendQuoted(text);
            } else {
                buffer.append(HttpString.Space).appendQuoted(code.getDefaultText());
            }
            if (date != null) {
                buffer.append(HttpString.Space).appendQuoted(HttpDateMaker.makeBytes(date));
            }
        }
    }

    public WarnCodeSort getCode() {
        return code;
    }

    public void setCode(WarnCodeSort code) {
        this.code = code;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
