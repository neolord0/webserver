package kr.dogfoot.webserver.context.connection.http.client;

import kr.dogfoot.webserver.context.connection.ConnectionSort;
import kr.dogfoot.webserver.util.Message;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.nio.ByteBuffer;

public class HttpsClientConnection extends HttpClientConnection {
    private static final int Unwrapped_ReadBuffer_Size = 1024 * 26;
    private ByteBuffer unwrappedReadBuffer;
    private SSLEngine sslEngine;

    private HandshakeState handshakeState;
    private ByteBuffer handshakeWrappedBuffer;

    public HttpsClientConnection(int id) {
        super(id);

        unwrappedReadBuffer = ByteBuffer.allocate(Unwrapped_ReadBuffer_Size);
        reader.buffer(unwrappedReadBuffer);

        sslEngine = null;

        handshakeState = HandshakeState.NotBegin;
        handshakeWrappedBuffer = null;
    }

    @Override
    public ConnectionSort sort() {
        return ConnectionSort.HttpsClientConnection;
    }

    @Override
    public void resetForPooled() {
        super.resetForPooled();

        unwrappedReadBuffer.clear();
        sslEngine = null;

        handshakeState = HandshakeState.NotBegin;
        handshakeWrappedBuffer = null;
    }

    @Override
    public boolean isAdjustSSL() {
        return true;
    }

    @Override
    public ByteBuffer readBuffer() {
        return unwrappedReadBuffer;
    }

    @Override
    public boolean prepareReading() {
        receiveBuffer.flip();
        SSLEngineResult unwrapResult = null;
        try {
            unwrapResult = sslEngine.unwrap(receiveBuffer, unwrappedReadBuffer);
        } catch (SSLException e) {
            e.printStackTrace();
        }

        receiveBuffer.compact();
        unwrappedReadBuffer.flip();

        if (unwrapResult == null) {
            return false;
        }

        switch (unwrapResult.getStatus()) {
            case OK:
                return true;

            case BUFFER_OVERFLOW:
                Message.debug(this, "BUFFER_OVERFLOW in unwrapping data");
                return false;

            case BUFFER_UNDERFLOW:
                Message.debug(this, "BUFFER_UNDERFLOW in unwrapping data");
                return false;

            case CLOSED:
                Message.debug(this, "close in unwrapping data");
                return false;

        }
        return false;
    }

    @Override
    public void prepareReceiving() {
        unwrappedReadBuffer.compact();
    }

    public SSLEngine sslEngine() {
        return sslEngine;
    }

    public void sslEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }

    public HandshakeState handshakeState() {
        return handshakeState;
    }

    public void handshakeState(HandshakeState handshakeState) {
        this.handshakeState = handshakeState;
    }

    public ByteBuffer handshakeWrappedBuffer() {
        return handshakeWrappedBuffer;
    }

    public void handshakeWrappedBuffer(ByteBuffer handshakeWrappedBuffer) {
        this.handshakeWrappedBuffer = handshakeWrappedBuffer;
    }

    public boolean handshakeStateIsReceiving() {
        return handshakeState == HandshakeState.ReceiveData;
    }

    public boolean handshakeStateIsSending() {
        switch (handshakeState) {
            case SendData:
            case SendDataAtLast:
                return true;
        }
        return false;
    }
}
