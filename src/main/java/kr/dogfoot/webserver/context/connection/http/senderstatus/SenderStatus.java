package kr.dogfoot.webserver.context.connection.http.senderstatus;

import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;

import java.nio.channels.FileChannel;

public class SenderStatus {
    private SendingState state;

    private FileChannel resourceFileCh;

    private int rangeCount;
    private int rangeIndex;
    private ContentRange range;
    private int sentSizeInRange;

    private ChunkedBodySendState chunkedBodySendState;

    public SenderStatus() {
        reset();
    }

    public SenderStatus reset() {
        state = SendingState.BeforeBody;

        resourceFileCh = null;

        rangeCount = 0;
        rangeIndex = 0;
        range = null;
        sentSizeInRange = 0;

        chunkedBodySendState = ChunkedBodySendState.ChunkSize;

        return this;
    }

    public FileChannel resourceFileCh() {
        return resourceFileCh;
    }

    public void resourceFileCh(FileChannel resourceFileCh) {
        this.resourceFileCh = resourceFileCh;
    }

    public boolean openedResourceFile() {
        return resourceFileCh != null;
    }

    public void changeState(SendingState state) {
        this.state = state;
    }

    public boolean stateIsBeforeBody() {
        return state == SendingState.BeforeBody;
    }

    public boolean stateIsEndBody() {
        return state == SendingState.BodyEnd;
    }

    public int rangeIndex() {
        return rangeIndex;
    }

    public SenderStatus nextRange() {
        rangeIndex++;
        sentSizeInRange = 0;
        return this;
    }

    public void rangeCount(int rangeCount) {
        this.rangeCount = rangeCount;
    }

    public boolean isMultipart() {
        return rangeCount >= 2;
    }

    public ContentRange range() {
        return range;
    }

    public void range(ContentRange range) {
        this.range = range;
    }

    public long readingPosition() {
        return range.firstPos() + sentSizeInRange;
    }

    public void addSentSizeInRange(long sent) {
        sentSizeInRange += sent;
    }

    public boolean isStartRange() {
        return sentSizeInRange == 0;
    }

    public boolean isEndRange() {
        return sentSizeInRange >= range.length();
    }

    public int remainingSendSize() {
        return (int) (range.length() - sentSizeInRange);
    }

    public boolean isLastRange() {
        return rangeIndex >= rangeCount - 1;
    }

    public ChunkedBodySendState chunkedBodySendState() {
        return chunkedBodySendState;
    }

    public void changeChunkedBodySendState(ChunkedBodySendState chunkedBodySendState) {
        this.chunkedBodySendState = chunkedBodySendState;
    }
}
