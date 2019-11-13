package kr.dogfoot.webserver.util.net.ssl;

import kr.dogfoot.webserver.server.host.ssl.SSLConfig;
import kr.dogfoot.webserver.server.host.ssl.SSLConfigCertificate;
import kr.dogfoot.webserver.util.ConfigFileLoader;
import kr.dogfoot.webserver.util.Message;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;

public class TrustManagerMaker {
    private final SSLConfigCertificate certificate;
    private final SSLConfig sslHostConfig;

    private TrustManagerMaker(SSLConfigCertificate certificate) {
        this.certificate = certificate;
        sslHostConfig = certificate.getSSLHostConfig();
    }

    public static TrustManagerMaker getInstance(SSLConfigCertificate certificate) {
        return new TrustManagerMaker(certificate);
    }

    public TrustManager[] get() throws Exception {
        KeyStore trustStore = sslHostConfig.getTruststore();
        if (trustStore != null) {
            checkTrustStoreEntries(trustStore);

            String algorithm = sslHostConfig.getTruststoreAlgorithm();
            String revocationListFile = sslHostConfig.getCertificateRevocationListFile();
            boolean revocationEnabled = sslHostConfig.getRevocationEnabled();

            if ("PKIX".equalsIgnoreCase(algorithm)) {
                return forPKIX(trustStore, algorithm, revocationListFile, revocationEnabled);
            } else {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
                tmf.init(trustStore);
                TrustManager[] tms = tmf.getTrustManagers();

                if (revocationListFile != null && revocationListFile.length() > 0) {
                    throw new CRLException("The truststoreProvider [" +
                            algorithm +
                            "] does not support the certificateRevocationFile configuration options");

                }
                // Only warn if the attribute has been explicitly configured
                if (sslHostConfig.isCertificateVerificationDepthConfigured()) {
                    Message.warn("The truststoreProvider [" +
                            algorithm +
                            "] does not support the certificateVerificationDepth configuration option");
                }

                return tms;
            }
        }

        return null;
    }

    private void checkTrustStoreEntries(KeyStore trustStore) throws Exception {
        Enumeration<String> aliases = trustStore.aliases();
        if (aliases != null) {
            Date now = new Date();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (trustStore.isCertificateEntry(alias)) {
                    Certificate cert = trustStore.getCertificate(alias);
                    if (cert instanceof X509Certificate) {
                        try {
                            ((X509Certificate) cert).checkValidity(now);
                        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                            e.printStackTrace();
                            StringBuffer msg = new StringBuffer();
                            msg.append("the trusted certificate with alias [")
                                    .append(alias)
                                    .append("] and DN [")
                                    .append(((X509Certificate) cert).getSubjectDN())
                                    .append("] is not valid due to [")
                                    .append(e.getMessage())
                                    .append("]. Certificates signed by this trusted certificate WILL be accepted");
                            Message.warn(msg.toString());
                        }
                    } else {
                        Message.debug("The validity dates of the trusted certificate with alias [" + alias + "] were not checked as the certificate was of an unknown type");
                    }
                }
            }
        }
    }


    private TrustManager[] forPKIX(KeyStore trustStore, String algorithm, String revocationListFile, boolean revocationEnabled) throws Exception {
        CertPathParameters params = parameters(revocationListFile, trustStore, revocationEnabled);
        ManagerFactoryParameters mfp = new CertPathTrustManagerParameters(params);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        tmf.init(mfp);
        return tmf.getTrustManagers();
    }

    protected CertPathParameters parameters(String revocationListFile, KeyStore trustStore,
                                            boolean revocationEnabled) throws Exception {

        PKIXBuilderParameters xparams =
                new PKIXBuilderParameters(trustStore, new X509CertSelector());
        if (revocationListFile != null && revocationListFile.length() > 0) {
            Collection<? extends CRL> crls = getCRLs(revocationListFile);
            CertStoreParameters csp = new CollectionCertStoreParameters(crls);
            CertStore store = CertStore.getInstance("Collection", csp);
            xparams.addCertStore(store);
            xparams.setRevocationEnabled(true);
        } else {
            xparams.setRevocationEnabled(revocationEnabled);
        }
        xparams.setMaxPathLength(sslHostConfig.getCertificateVerificationDepth());
        return xparams;
    }

    protected Collection<? extends CRL> getCRLs(String revocationListFile)
            throws IOException, CRLException, CertificateException {

        Collection<? extends CRL> crls = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (InputStream is = ConfigFileLoader.getFileInputStream(revocationListFile)) {
                crls = cf.generateCRLs(is);
            }
        } catch (IOException e) {
            throw e;
        } catch (CRLException e) {
            throw e;
        } catch (CertificateException e) {
            throw e;
        }
        return crls;
    }
}
