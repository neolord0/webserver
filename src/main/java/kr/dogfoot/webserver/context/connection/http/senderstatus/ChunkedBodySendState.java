package kr.dogfoot.webserver.context.connection.http.senderstatus;

public enum ChunkedBodySendState {
    ChunkSize,
    ChunkBody,
    ChunkDataCRLF
}
