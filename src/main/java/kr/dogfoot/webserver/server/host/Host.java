package kr.dogfoot.webserver.server.host;

import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.host.proxy_info.ProxyInfo;
import kr.dogfoot.webserver.server.host.ssl.SSLConfig;
import kr.dogfoot.webserver.server.host.ssl.SSLConfigCertificate;
import kr.dogfoot.webserver.server.resource.ResourceDirectory;
import kr.dogfoot.webserver.server.resource.look.LookResult;
import kr.dogfoot.webserver.server.resource.look.LookState;
import kr.dogfoot.webserver.util.Message;
import kr.dogfoot.webserver.util.net.ssl.NeoSSLContext;
import kr.dogfoot.webserver.util.net.ssl.SSLUtil;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

public class Host {
    private static final int DEFAULT_PROXY_INFO_COUNT = 3;

    private Server server;
    private HostObjects hostObjects;
    private ResourceDirectory root;

    private String name;
    private String domain;
    private String ipAddress;
    private int port;
    private boolean defaultHost;

    private boolean adjustSSL;
    private SSLConfig sslConfig;
    private NeoSSLContext sslContext;

    private ProxyInfo[] proxyInfos;
    private int proxyInfoCount;


    public Host(Server server) {
        this.server = server;

        hostObjects = new HostObjects(server.objects());
        root = new ResourceDirectory(null);

        proxyInfos = new ProxyInfo[DEFAULT_PROXY_INFO_COUNT];
        proxyInfoCount = 0;
    }

    public LookResult look(Request request) {
        LookState ls = new LookState(request);
        LookResult lr = new LookResult(request, this);
        root.look(ls, lr);
        return lr;
    }

    public boolean isMatched(String hostAddr, int port) {
        if (hostAddr == null) {
            return false;
        }
        if (port == -1 || port == this.port) {
            return hostAddr.equalsIgnoreCase(domain) || hostAddr.equals(ipAddress);
        }
        return false;
    }

    public Server server() {
        return server;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public String domain() {
        return domain;
    }

    public void domain(String domain) {
        this.domain = domain;
    }

    public String ipAddress() {
        return ipAddress;
    }

    public void ipAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int port() {
        return port;
    }

    public void port(int port) {
        this.port = port;
    }

    public boolean isAdjustSSL() {
        return adjustSSL;
    }

    public void defaultHost(boolean defaultHost) {
        this.defaultHost = defaultHost;
    }

    public boolean defaultHost() {
        return defaultHost;
    }

    public SSLConfig getSSLConfig() {
        if (sslConfig == null) {
            sslConfig = new SSLConfig();
            adjustSSL = true;
        }
        return sslConfig;
    }

    public HostObjects hostObjects() {
        return hostObjects;
    }

    public ResourceDirectory root() {
        return root;
    }

    public void initialize() {
        if (sslConfig != null) {
            createSSLContext();

            Message.debug("create SSL Context for " + ipAddress + ":" + port);
        }
    }

    private void createSSLContext() {
        boolean firstCertificate = true;
        for (SSLConfigCertificate certificate : sslConfig.getCertificates(true)) {
            SSLUtil sslUtil = new SSLUtil(certificate);
            if (firstCertificate) {
                firstCertificate = false;
                sslConfig.setEnabledProtocols(sslUtil.getEnabledProtocols());
                sslConfig.setEnabledCiphers(sslUtil.getEnabledCiphers());
            }

            try {
                sslContext = sslUtil.createSSLContext();
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            certificate.setSslContext(sslContext);
        }
    }

    public SSLEngine createSSLEngine() {
        SSLEngine engine = sslContext.createSSLEngine();

        switch (sslConfig.getCertificateVerification()) {
            case NONE:
                engine.setNeedClientAuth(false);
                engine.setWantClientAuth(false);
                break;
            case OPTIONAL:
            case OPTIONAL_NO_CA:
                engine.setWantClientAuth(true);
                break;
            case REQUIRED:
                engine.setNeedClientAuth(true);
                break;
        }

        engine.setUseClientMode(false);
        engine.setEnableSessionCreation(true);
        engine.setEnabledCipherSuites(sslConfig.getEnabledCiphers());
        engine.setEnabledProtocols(sslConfig.getEnabledProtocols());

        SSLParameters sslParameters = engine.getSSLParameters();
        sslParameters.setUseCipherSuitesOrder(sslConfig.isHonorCipherOrder());
        engine.setSSLParameters(sslParameters);

        return engine;
    }

    public ProxyInfo addNewProxyInfo() {
        if (proxyInfos.length <= proxyInfoCount) {
            ProxyInfo[] newArray = new ProxyInfo[proxyInfos.length * 2];
            System.arraycopy(proxyInfos, 0, newArray, 0, proxyInfos.length);
            proxyInfos = newArray;
        }

        proxyInfos[proxyInfoCount++] = new ProxyInfo(proxyInfoCount - 1);
        return proxyInfos[proxyInfoCount - 1];
    }

    public ProxyInfo findProxyInfo(String url) {
        for (int i = 0; i < proxyInfoCount; i++) {
            if (proxyInfos[i].isMatchURL(url)) {
                return proxyInfos[i];
            }
        }
        return null;
    }
}
