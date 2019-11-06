package kr.dogfoot.webserver.context.connection.http.parserstatus;

public enum ChunkParsingState {
    ChunkSize,
    ChunkSizeCRLF,
    ChunkData,
    ChunkDataCRLF,
    ChunkEnd
}
