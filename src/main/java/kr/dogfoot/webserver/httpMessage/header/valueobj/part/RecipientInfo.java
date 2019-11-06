package kr.dogfoot.webserver.httpMessage.header.valueobj.part;


import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.AppendableToByte;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

public class RecipientInfo implements AppendableToByte {
    private String protocolName;
    private String protocolVersion;
    private String receivedBy;
    private String comment;

    public RecipientInfo() {
    }

    public void parse(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Space;
        ps.spaceIsSep = true;
        ps.permitNoSeparator = false;

        if (ByteParser.nextItem(value, ps) < 0) {
            throw new ParserException("Invalid recipient info.");
        }
        parseProtocol(value, ps);

        ps.permitNoSeparator = true;
        if (ByteParser.nextItem(value, ps) > 0) {
            receivedBy = ps.toString(value);
        }

        if (ByteParser.nextItem(value, ps) > 0) {
            ParseState ps2 = ParseState.pooledObject();
            ps2.prepare(ps);
            ByteParser.unquote(value, ps2);
            comment = ps2.toString(value);
            ParseState.release(ps2);
        }
        ParseState.release(ps);
    }

    private void parseProtocol(byte[] value, ParseState parentPS) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.prepare(parentPS);
        ps.separator = HttpString.Slash;
        ps.spaceIsSep = false;
        ps.permitNoSeparator = false;

        if (ByteParser.nextItem(value, ps) > 0) {
            protocolName = ps.toString(value);
            ps.rest();
            protocolVersion = ps.toString(value);
        } else {
            protocolVersion = parentPS.toString(value);
        }
        ParseState.release(ps);
    }

    @Override
    public void append(OutputBuffer buffer) {
        if (protocolName != null) {
            buffer.append(protocolName).append(HttpString.Slash);
        }
        buffer.append(protocolVersion)
                .append(HttpString.Space)
                .append(receivedBy);
        if (comment != null) {
            buffer.append(HttpString.Space).appendQuoted(comment);
        }
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
