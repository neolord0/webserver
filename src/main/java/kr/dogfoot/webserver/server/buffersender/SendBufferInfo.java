package kr.dogfoot.webserver.server.buffersender;

import kr.dogfoot.webserver.context.Context;

import java.nio.ByteBuffer;

public class SendBufferInfo {
    private Protocol protocol;
    private JobType jobType;
    private Context context;
    private ByteBuffer buffer;
    private boolean willRelease;
    private boolean wrapped;

    public SendBufferInfo() {
        wrapped = false;
    }

    public SendBufferInfo client(Context context, ByteBuffer buffer, boolean willRelease) {
        this.protocol = Protocol.Client;
        this.jobType = JobType.SendBuffer;
        this.context = context;
        this.buffer = buffer;
        this.willRelease = willRelease;
        return this;
    }

    public SendBufferInfo clientClose(Context context) {
        this.protocol = Protocol.Client;
        this.jobType = JobType.Close;
        this.context = context;
        this.buffer = null;
        this.willRelease = false;
        return this;
    }

    public SendBufferInfo ajpProxy(Context context, ByteBuffer buffer, boolean willRelease) {
        this.protocol = Protocol.AjpProxy;
        this.jobType = JobType.SendBuffer;
        this.context = context;
        this.buffer = buffer;
        this.willRelease = willRelease;
        return this;
    }

    public SendBufferInfo ajpProxyClose(Context context) {
        this.protocol = Protocol.AjpProxy;
        this.jobType = JobType.Close;
        this.context = context;
        this.buffer = null;
        this.willRelease = false;
        return this;
    }

    public SendBufferInfo httpProxy(Context context, ByteBuffer buffer, boolean willRelease) {
        this.protocol = Protocol.HttpProxy;
        this.jobType = JobType.SendBuffer;
        this.context = context;
        this.buffer = buffer;
        this.willRelease = willRelease;
        return this;
    }

    public SendBufferInfo httpProxyClose(Context context) {
        this.protocol = Protocol.HttpProxy;
        this.jobType = JobType.Close;
        this.context = context;
        this.buffer = null;
        this.willRelease = false;
        return this;
    }

    public Protocol protocol() {
        return protocol;
    }

    public JobType jobType() {
        return jobType;
    }

    public Context context() {
        return context;
    }

    public ByteBuffer buffer() {
        return buffer;
    }

    public void buffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public boolean willRelease() {
        return willRelease;
    }

    public boolean isHttps() {
        return protocol == Protocol.Client && context.clientConnection().isAdjustSSL();
    }

    public boolean isWrapped() {
        return wrapped;
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
    }

    public enum Protocol {
        Client,
        AjpProxy,
        HttpProxy
    }

    public enum JobType {
        SendBuffer,
        Close
    }
}
