package kr.dogfoot.webserver.util.net.ssl;

import kr.dogfoot.webserver.server.host.ssl.SSLConfig;
import kr.dogfoot.webserver.util.Message;
import kr.dogfoot.webserver.util.net.ssl.compat.JreVendor;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SSLInfomation {
    private static final Set<String> implementedProtocols;
    private static final Set<String> implementedCiphers;

    static {
        implementedProtocols = initImplmementedProtocols();
        implementedCiphers = initImplmementedCiphers();
    }

    private static Set<String> initImplmementedProtocols() {
        SSLContext context = defaultSSLContext();

        String[] protocolArray = context.getSupportedSSLParameters().getProtocols();
        Set<String> protocols = new HashSet<>(protocolArray.length);

        // Filter out SSLv2 from the list of implemented protocols (just in case
        // we are running on a JVM that supports it) since it is no longer
        // considered secure but allow SSLv2Hello.
        // Note SSLv3 is allowed despite known insecurities because some users
        // still have a requirement for it.
        for (String protocol : protocolArray) {
            String protocolUpper = protocol.toUpperCase(Locale.ENGLISH);
            if (!"SSLV2HELLO".equals(protocolUpper) && !"SSLV3".equals(protocolUpper)) {
                if (protocolUpper.contains("SSL")) {
                    Message.debug("The SSL protocol [" +
                            protocol + "] which is supported in this JRE was excluded from the protocols available to Tomcat");
                    continue;
                }
            }
            protocols.add(protocol);
        }

        if (protocols.size() == 0) {
            Message.warn("unable to determine a default for sslEnabledProtocols. Set an explicit value to ensure the connector can start.");
        }

        return protocols;
    }

    private static SSLContext defaultSSLContext() {
        SSLContext context;
        try {
            context = SSLContext.getInstance(Constants.SSL_PROTO_TLS);
            context.init(null, null, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // This is fatal for the connector so throw an exception to prevent
            // it from starting
            throw new IllegalArgumentException(e);
        }
        return context;
    }

    private static Set<String> initImplmementedCiphers() {
        SSLContext context = defaultSSLContext();

        String[] implementedCipherSuiteArray = context.getSupportedSSLParameters().getCipherSuites();
        // The IBM JRE will accept cipher suites names SSL_xxx or TLS_xxx but
        // only returns the SSL_xxx form for supported cipher suites. Therefore
        // need to filter the requested cipher suites using both forms with an
        // IBM JRE.
        if (JreVendor.IS_IBM_JVM) {
            Set<String> ciphers = new HashSet<>(implementedCipherSuiteArray.length * 2);
            for (String name : implementedCipherSuiteArray) {
                ciphers.add(name);
                if (name.startsWith("SSL")) {
                    ciphers.add("TLS" + name.substring(3));
                }
            }
            return ciphers;
        } else {
            Set<String> ciphers = new HashSet<>(implementedCipherSuiteArray.length);
            ciphers.addAll(Arrays.asList(implementedCipherSuiteArray));
            return ciphers;
        }
    }

    public static String[] enabledProtocols(boolean warnTls13, SSLConfig sslHostConfig) {
        Set<String> configured = sslHostConfig.getProtocols();

        // If TLSv1.3 is not implemented and not explicitly requested we can
        // ignore it. It is included in the defaults so it may be configured.
        if (!implementedProtocols.contains(Constants.SSL_PROTO_TLSv1_3) &&
                !sslHostConfig.isExplicitlyRequestedProtocol(Constants.SSL_PROTO_TLSv1_3)) {
            configured.remove(Constants.SSL_PROTO_TLSv1_3);
        }

        // Newer JREs are dropping support for SSLv2Hello. If it is not
        // implemented and not explicitly requested we can ignore it. It is
        // included in the defaults so it may be configured.
        if (!implementedProtocols.contains(Constants.SSL_PROTO_SSLv2Hello) &&
                !sslHostConfig.isExplicitlyRequestedProtocol(Constants.SSL_PROTO_SSLv2Hello)) {
            configured.remove(Constants.SSL_PROTO_SSLv2Hello);
        }

        List<String> enabled =
                getEnabled("protocols", warnTls13, configured, implementedProtocols);

        if (enabled.contains("SSLv3")) {
            Message.warn("SSLv3 has been explicitly enabled. This protocol is known to be insecure.");
        }

        if (enabled.contains(Constants.SSL_PROTO_TLSv1_3) &&
                sslHostConfig.getCertificateVerification() == SSLConfig.CertificateVerification.OPTIONAL &&
                warnTls13) {
            Message.warn("The JSSE TLS 1.3 implementation does not support authentication after the initial handshake and is therefore incompatible with optional client authentication");
        }

        return enabled.toArray(new String[enabled.size()]);
    }

    private static <T> List<T> getEnabled(String name, boolean warnOnSkip, Collection<T> configured,
                                          Collection<T> implemented) {

        List<T> enabled = new ArrayList<>();

        if (implemented.size() == 0) {
            // Unable to determine the list of available protocols. This will
            // have been logged previously.
            // Use the configuredProtocols and hope they work. If not, an error
            // will be generated when the list is used. Not ideal but no more
            // can be done at this point.
            enabled.addAll(configured);
        } else {
            enabled.addAll(configured);
            enabled.retainAll(implemented);

            if (enabled.isEmpty()) {
                // Don't use the defaults in this case. They may be less secure
                // than the configuration the user intended.
                // Force the failure of the connector
                throw new IllegalArgumentException(
                        /*sm.getString("sslUtilBase.noneSupported", name, configured)*/);
            }

            Message.debug("The [" + name + "] that are active are : [" + enabled + "]");
            if (warnOnSkip) {
                if (enabled.size() != configured.size()) {
                    List<T> skipped = new ArrayList<>();
                    skipped.addAll(configured);
                    skipped.removeAll(enabled);
                    String msg = "Some of the specified [" + name + "] are not supported by the SSL engine and have been skipped: [" + skipped + "]";
                    if (warnOnSkip) {
                        Message.warn(msg);
                    } else {
                        Message.debug(msg);
                    }
                }
            }
        }

        return enabled;
    }

    public static String[] getEnabledCiiphers(SSLConfig sslHostConfig) {
        List<String> configured = sslHostConfig.getJsseCipherNames();

        List<String> enabled =
                getEnabled("cipher", false, configured, implementedCiphers);

        return enabled.toArray(new String[enabled.size()]);
    }

    public Set<String> implementedProtocols() {
        return implementedProtocols;
    }

    public Set<String> implementedCiphers() {
        return implementedCiphers;
    }


}
