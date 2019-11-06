package kr.dogfoot.webserver.context.connection.http;

import kr.dogfoot.webserver.context.connection.Connection;
import kr.dogfoot.webserver.context.connection.http.parserstatus.ParserStatus;
import kr.dogfoot.webserver.context.connection.http.senderstatus.SenderStatus;
import kr.dogfoot.webserver.parser.util.CachedReader;

import java.nio.ByteBuffer;

public abstract class HttpConnection extends Connection {
    private static final int Receive_Buffer_Size = 16921;

    protected CachedReader reader;

    protected ParserStatus parserStatus;
    protected SenderStatus senderStatus;

    public HttpConnection(int id) {
        super(id);

        reader = new CachedReader()
                .buffer(readBuffer());

        parserStatus = new ParserStatus();
        senderStatus = new SenderStatus();
    }

    public abstract boolean adjustSSL();

    @Override
    public void resetForPooled() {
        super.resetForPooled();

        reader.reset();

        parserStatus.reset();
        senderStatus.reset();
    }

    @Override
    public int receiveBufferSize() {
        return Receive_Buffer_Size;
    }

    public boolean prepareReading() {
        receiveBuffer.flip();
        return true;
    }

    public ByteBuffer readBuffer() {
        return receiveBuffer;
    }

    public void prepareReceiving() {
        receiveBuffer.compact();
    }

    public CachedReader reader() {
        return reader;
    }

    public ParserStatus parserStatus() {
        return parserStatus;
    }

    public SenderStatus senderStatus() {
        return senderStatus;
    }
}
