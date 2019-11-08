package kr.dogfoot.webserver.context.connection.http.parserstatus;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;

public class ParserStatus {
    private ParsingBuffer buffer;

    private ParsingState state;
    private boolean skippingSpace;
    private boolean doneCR;
    private boolean doneCRLF;

    private HeaderSort parsingHeaderSort;

    private BodyParsingType bodyParsingType;
    private int contentLength;
    private int readBodySize;
    private ChunkParsingState chunkState;
    private int chunkSize;
    private int positionInChunk;

    public ParserStatus() {
        buffer = new ParsingBuffer();
        reset();
    }

    public void reset() {
        buffer.reset();

        state = ParsingState.FirstCRLF;

        skippingSpace = false;
        doneCR = false;
        doneCRLF = false;

        parsingHeaderSort = null;

        bodyParsingType = null;
        contentLength = -1;
        readBodySize = 0;
        chunkState = ChunkParsingState.ChunkSize;
        chunkSize = -1;
        positionInChunk = 0;
    }

    public ParserStatus resetForBodyStart() {
        contentLength = -1;
        readBodySize = 0;

        chunkState = ChunkParsingState.ChunkSize;
        chunkSize = -1;
        positionInChunk = 0;
        return this;
    }

    public ParsingState state() {
        return state;
    }

    public void changeState(ParsingState state) {
        switch (state) {
            case FirstCRLF:
            case HeaderName:
                skippingSpace = false;

            case StatusCode:
            case Reason:
            case RequestURI:
            case Version:
            case HeaderValue:
                skippingSpace = true;
                break;

            case CRLF:
            case HeaderEnd:
                doneCR = false;
                break;

            case BodyStart:
                resetForBodyStart();
                break;
        }

        this.state = state;
        buffer.reset();
    }

    public ParsingBuffer buffer() {
        return buffer;
    }

    public boolean skippingSpace() {
        return skippingSpace;
    }

    public void skippingSpace(boolean skippingSpace) {
        this.skippingSpace = skippingSpace;
    }

    public boolean doneCR() {
        return doneCR;
    }

    public void doneCR(boolean doneCR) {
        this.doneCR = doneCR;
    }

    public boolean doneCRLF() {
        return doneCRLF;
    }

    public void doneCRLF(boolean doneCRLF) {
        this.doneCRLF = doneCRLF;
    }

    public HeaderSort parsingHeaderSort() {
        return parsingHeaderSort;
    }

    public void parsingHeaderSort(HeaderSort headerSort) {
        this.parsingHeaderSort = headerSort;
    }

    public void prepareBodyParsing(BodyParsingType bodyParsingType, int contentLength) {
        this.bodyParsingType = bodyParsingType;
        this.contentLength = contentLength;
        readBodySize = 0;
        changeChunkState(ChunkParsingState.ChunkSize);
    }

    public BodyParsingType bodyParsingType() {
        return bodyParsingType;
    }

    public int contentLength() {
        return contentLength;
    }

    public ParserStatus contentLength(int contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public void addReadBodySize(int size) {
        readBodySize += size;
    }

    public int remainingReadBodySize() {
        return contentLength - readBodySize;
    }

    public boolean hasRemainingReadBodySize() {
        return remainingReadBodySize() > 0;
    }

    public ChunkParsingState chunkState() {
        return chunkState;
    }

    public void changeChunkState(ChunkParsingState chunkState) {
        switch (chunkState) {
            case ChunkSize:
                chunkSize = -1;
                break;
            case ChunkSizeCRLF:
            case ChunkDataCRLF:
                doneCR = false;
                break;
            case ChunkData:
                positionInChunk = 0;
                break;
            case ChunkEnd:
                break;
        }
        this.chunkState = chunkState;
    }

    public boolean isBeforeChunkData() {
        switch (chunkState) {
            case ChunkSize:
            case ChunkSizeCRLF:
                return true;
        }
        return false;
    }

    public int chunkSize() {
        return chunkSize;
    }

    public void chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void addPositionInChunk(int size) {
        positionInChunk += size;
    }

    public int remainingChunkSize() {
        return chunkSize - positionInChunk;
    }
}

