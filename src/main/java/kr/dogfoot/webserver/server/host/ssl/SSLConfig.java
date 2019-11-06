/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kr.dogfoot.webserver.server.host.ssl;

import kr.dogfoot.webserver.util.net.ssl.Constants;
import kr.dogfoot.webserver.util.net.ssl.KeyStoreUtil;
import kr.dogfoot.webserver.util.net.ssl.cipher.Cipher;
import kr.dogfoot.webserver.util.net.ssl.cipher.OpenSSLCipherConfigurationParser;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SSLConfig {
    protected static final Set<String> SSL_PROTO_ALL_SET = new HashSet<>();

    static {
        SSL_PROTO_ALL_SET.add(Constants.SSL_PROTO_SSLv2Hello);
        SSL_PROTO_ALL_SET.add(Constants.SSL_PROTO_TLSv1);
        SSL_PROTO_ALL_SET.add(Constants.SSL_PROTO_TLSv1_1);
        SSL_PROTO_ALL_SET.add(Constants.SSL_PROTO_TLSv1_2);
        SSL_PROTO_ALL_SET.add(Constants.SSL_PROTO_TLSv1_3);
    }

    private String[] enabledCiphers;
    private String[] enabledProtocols;

    private Set<String> protocols = new HashSet<>();
    private Set<String> explicitlyRequestedProtocols = new HashSet<>();

    private String ciphers = "HIGH:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!MD5:!kRSA";
    private LinkedHashSet<Cipher> cipherList = null;
    private List<String> jsseCipherNames = null;
    private boolean honorCipherOrder = false;

    private int sessionCacheSize = 0;
    private int sessionTimeout = 86400;

    private SSLConfigCertificate defaultCertificate = null;
    private Set<SSLConfigCertificate> certificates = new LinkedHashSet<>(4);

    private String certificateRevocationListFile;
    private CertificateVerification certificateVerification = CertificateVerification.NONE;
    private int certificateVerificationDepth = 10;
    private boolean certificateVerificationDepthConfigured = false;

    private String keyManagerAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
    private boolean revocationEnabled = false;
    private String sslProtocol = Constants.SSL_PROTO_TLS;
    private String truststoreAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
    private String truststoreFile = System.getProperty("javax.ssl.ssl.trustStore");
    private String truststorePassword = System.getProperty("javax.ssl.ssl.trustStorePassword");
    private String truststoreProvider = System.getProperty("javax.ssl.ssl.trustStoreProvider");
    private String truststoreType = System.getProperty("javax.ssl.ssl.trustStoreType");
    private KeyStore truststore = null;

    public SSLConfig() {
        // Set defaults that can't be (easily) set when defining the fields.
        setProtocols(Constants.SSL_PROTO_ALL);
    }

    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }

    public void setEnabledProtocols(String[] enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    public String[] getEnabledCiphers() {
        return enabledCiphers;
    }

    public void setEnabledCiphers(String[] enabledCiphers) {
        this.enabledCiphers = enabledCiphers;
    }

    public Set<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(String input) {
        protocols.clear();
        explicitlyRequestedProtocols.clear();

        // List of protocol names, separated by ",", "+" or "-".
        // Semantics is adding ("+") or removing ("-") from left
        // to right, starting with an empty protocol set.
        // Tokens are individual protocol names or "all" for a
        // default set of supported protocols.
        // Separator "," is only kept for compatibility and has the
        // same semantics as "+", except that it warns about a potentially
        // missing "+" or "-".

        // Split using a positive lookahead to keep the separator in
        // the capture so we can check which case it is.
        for (String value : input.split("(?=[-+,])")) {
            String trimmed = value.trim();
            // Ignore token which only consists of prefix character
            if (trimmed.length() > 1) {
                if (trimmed.charAt(0) == '+') {
                    trimmed = trimmed.substring(1).trim();
                    if (trimmed.equalsIgnoreCase(Constants.SSL_PROTO_ALL)) {
                        protocols.addAll(SSL_PROTO_ALL_SET);
                    } else {
                        protocols.add(trimmed);
                        explicitlyRequestedProtocols.add(trimmed);
                    }
                } else if (trimmed.charAt(0) == '-') {
                    trimmed = trimmed.substring(1).trim();
                    if (trimmed.equalsIgnoreCase(Constants.SSL_PROTO_ALL)) {
                        protocols.removeAll(SSL_PROTO_ALL_SET);
                    } else {
                        protocols.remove(trimmed);
                        explicitlyRequestedProtocols.remove(trimmed);
                    }
                } else {
                    if (trimmed.charAt(0) == ',') {
                        trimmed = trimmed.substring(1).trim();
                    }
                    if (!protocols.isEmpty()) {
                        /*
                        log.warn("The protocol [" +
                                trimmed +
                                "] was added to the list of protocols on the SSLConfig named [" +
                                getHostName() +
                                "]. Check if a +/- prefix is missing.");
                         */
                    }
                    if (trimmed.equalsIgnoreCase(Constants.SSL_PROTO_ALL)) {
                        protocols.addAll(SSL_PROTO_ALL_SET);
                    } else {
                        protocols.add(trimmed);
                        explicitlyRequestedProtocols.add(trimmed);
                    }
                }
            }
        }
    }

    public boolean isExplicitlyRequestedProtocol(String protocol) {
        return explicitlyRequestedProtocols.contains(protocol);
    }

    public String getCiphers() {
        return ciphers;
    }

    public void setCiphers(String ciphersList) {
        // Ciphers is stored in OpenSSL format. Convert the provided value if
        // necessary.
        if (ciphersList != null && !ciphersList.contains(":")) {
            StringBuilder sb = new StringBuilder();
            // Not obviously in OpenSSL format. May be a single OpenSSL or JSSE
            // cipher name. May be a comma separated list of cipher names
            String[] ciphers = ciphersList.split(",");
            for (String cipher : ciphers) {
                String trimmed = cipher.trim();
                if (trimmed.length() > 0) {
                    String openSSLName = OpenSSLCipherConfigurationParser.jsseToOpenSSL(trimmed);
                    if (openSSLName == null) {
                        // Not a JSSE name. Maybe an OpenSSL name or alias
                        openSSLName = trimmed;
                    }
                    if (sb.length() > 0) {
                        sb.append(':');
                    }
                    sb.append(openSSLName);
                }
            }
            this.ciphers = sb.toString();
        } else {
            this.ciphers = ciphersList;
        }
        this.cipherList = null;
        this.jsseCipherNames = null;
    }

    public LinkedHashSet<Cipher> getCipherList() {
        if (cipherList == null) {
            cipherList = OpenSSLCipherConfigurationParser.parse(getCiphers());
        }
        return cipherList;
    }

    public List<String> getJsseCipherNames() {
        if (jsseCipherNames == null) {
            jsseCipherNames = OpenSSLCipherConfigurationParser.convertForJSSE(getCipherList());
        }
        return jsseCipherNames;
    }

    public boolean getHonorCipherOrder() {
        return honorCipherOrder;
    }

    public void setHonorCipherOrder(boolean honorCipherOrder) {
        this.honorCipherOrder = honorCipherOrder;
    }

    public int getSessionCacheSize() {
        return sessionCacheSize;
    }

    public void setSessionCacheSize(int sessionCacheSize) {
        this.sessionCacheSize = sessionCacheSize;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    private void registerDefaultCertificate() {
        if (defaultCertificate == null) {
            defaultCertificate = new SSLConfigCertificate(
                    this, SSLConfigCertificate.Type.UNDEFINED);
            certificates.add(defaultCertificate);
        }
    }

    public void addCertificate(SSLConfigCertificate certificate) {
        // Need to make sure that if there is more than one certificate, none of
        // them have a type of undefined.
        if (certificates.size() == 0) {
            certificates.add(certificate);
            return;
        }

        if (certificates.size() == 1 &&
                certificates.iterator().next().getType() == SSLConfigCertificate.Type.UNDEFINED ||
                certificate.getType() == SSLConfigCertificate.Type.UNDEFINED) {
            // Invalid config
            throw new IllegalArgumentException("Multiple certificates were specified and at least one is missing the required attribute type");
        }

        certificates.add(certificate);
    }

    public Set<SSLConfigCertificate> getCertificates() {
        return getCertificates(false);
    }

    public Set<SSLConfigCertificate> getCertificates(boolean createDefaultIfEmpty) {
        if (certificates.size() == 0 && createDefaultIfEmpty) {
            registerDefaultCertificate();
        }
        return certificates;
    }

    public String getKeyPassword() {
        registerDefaultCertificate();
        return defaultCertificate.getKeyPassword();
    }

    public void setKeyPassword(String certificateKeyPassword) {
        registerDefaultCertificate();
        defaultCertificate.setKeyPassword(certificateKeyPassword);
    }

    public String getCertificateRevocationListFile() {
        return certificateRevocationListFile;
    }

    public void setCertificateRevocationListFile(String certificateRevocationListFile) {
        this.certificateRevocationListFile = certificateRevocationListFile;
    }

    public CertificateVerification getCertificateVerification() {
        return certificateVerification;
    }

    public void setCertificateVerification(String certificateVerification) {
        try {
            this.certificateVerification =
                    CertificateVerification.fromString(certificateVerification);
        } catch (IllegalArgumentException iae) {
            // If the specified value is not recognised, default to the
            // strictest possible option.
            this.certificateVerification = CertificateVerification.REQUIRED;
            throw iae;
        }
    }

    public String getCertificateVerificationAsString() {
        return certificateVerification.toString();
    }

    public void setCertificateVerificationAsString(String certificateVerification) {
        setCertificateVerification(certificateVerification);
    }

    public int getCertificateVerificationDepth() {
        return certificateVerificationDepth;
    }

    public void setCertificateVerificationDepth(int certificateVerificationDepth) {
        this.certificateVerificationDepth = certificateVerificationDepth;
        certificateVerificationDepthConfigured = true;
    }

    public boolean isCertificateVerificationDepthConfigured() {
        return certificateVerificationDepthConfigured;
    }

    public String getKeyAlias() {
        registerDefaultCertificate();
        return defaultCertificate.getKeyAlias();
    }

    public void setKeyAlias(String keyAlias) {
        registerDefaultCertificate();
        defaultCertificate.setKeyAlias(keyAlias);
    }

    public String getKeystoreFile() {
        registerDefaultCertificate();
        return defaultCertificate.getKeystoreFile();
    }

    public void setKeystoreFile(String keystoreFile) {
        registerDefaultCertificate();
        defaultCertificate.setKeystoreFile(keystoreFile);
    }

    public String getKeystorePassword() {
        registerDefaultCertificate();
        return defaultCertificate.getKeystorePassword();
    }

    public void setKeystorePassword(String keystorePassword) {
        registerDefaultCertificate();
        defaultCertificate.setKeystorePassword(keystorePassword);
    }

    public String getKeystoreProvider() {
        registerDefaultCertificate();
        return defaultCertificate.getKeystoreProvider();
    }

    public void setKeystoreProvider(String keystoreProvider) {
        registerDefaultCertificate();
        defaultCertificate.setKeystoreProvider(keystoreProvider);
    }

    public String getKeystoreType() {
        registerDefaultCertificate();
        return defaultCertificate.getKeystoreType();
    }

    public void setKeystoreType(String keystoreType) {
        registerDefaultCertificate();
        defaultCertificate.setKeystoreType(keystoreType);
    }

    public String getKeyManagerAlgorithm() {
        return keyManagerAlgorithm;
    }

    public void setKeyManagerAlgorithm(String keyManagerAlgorithm) {
        this.keyManagerAlgorithm = keyManagerAlgorithm;
    }

    public boolean getRevocationEnabled() {
        return revocationEnabled;
    }

    public void setRevocationEnabled(boolean revocationEnabled) {
        this.revocationEnabled = revocationEnabled;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public String getTruststoreAlgorithm() {
        return truststoreAlgorithm;
    }

    public void setTruststoreAlgorithm(String truststoreAlgorithm) {
        this.truststoreAlgorithm = truststoreAlgorithm;
    }

    public String getTruststoreFile() {
        return truststoreFile;
    }

    public void setTruststoreFile(String truststoreFile) {
        this.truststoreFile = truststoreFile;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public String getTruststoreProvider() {
        if (truststoreProvider == null) {
            Set<SSLConfigCertificate> certificates = getCertificates();
            if (certificates.size() == 1) {
                return certificates.iterator().next().getKeystoreProvider();
            }
            return SSLConfigCertificate.DEFAULT_KEYSTORE_PROVIDER;
        } else {
            return truststoreProvider;
        }
    }

    public void setTruststoreProvider(String truststoreProvider) {
        this.truststoreProvider = truststoreProvider;
    }

    public String getTruststoreType() {
        if (truststoreType == null) {
            Set<SSLConfigCertificate> certificates = getCertificates();
            if (certificates.size() == 1) {
                String keystoreType = certificates.iterator().next().getKeystoreType();
                // Don't use keystore type as the default if we know it is not
                // going to be used as a trust store type
                if (!"PKCS12".equalsIgnoreCase(keystoreType)) {
                    return keystoreType;
                }
            }
            return SSLConfigCertificate.DEFAULT_KEYSTORE_TYPE;
        } else {
            return truststoreType;
        }
    }

    public void setTruststoreType(String truststoreType) {
        this.truststoreType = truststoreType;
    }

    public void setTrustStore(KeyStore truststore) {
        this.truststore = truststore;
    }


    public KeyStore getTruststore() throws IOException {
        KeyStore result = truststore;
        if (result == null) {
            if (truststoreFile != null) {
                try {
                    result = KeyStoreUtil.load(getTruststoreType(), getTruststoreProvider(),
                            getTruststoreFile(), getTruststorePassword());
                } catch (IOException ioe) {
                    Throwable cause = ioe.getCause();
                    if (cause instanceof UnrecoverableKeyException) {
                        // Log a warning we had a password issue
                        /*
                        log.warn("The provided trust store password could not be used to unlock and/or validate the trust store. Retrying to access the trust store with a null password which will skip validation.",
                                cause);

                         */
                        // Re-try
                        result = KeyStoreUtil.load(getTruststoreType(), getTruststoreProvider(),
                                getTruststoreFile(), null);
                    } else {
                        // Something else went wrong - re-throw
                        throw ioe;
                    }
                }
            }
        }
        return result;
    }

    public String getChainFile() {
        registerDefaultCertificate();
        return defaultCertificate.getChainFile();
    }

    public void setChainFile(String certificateChainFile) {
        registerDefaultCertificate();
        defaultCertificate.setChainFile(certificateChainFile);
    }


    public String getCertificateFile() {
        registerDefaultCertificate();
        return defaultCertificate.getCertificateFile();
    }

    public void setCertificateFile(String certificateFile) {
        registerDefaultCertificate();
        defaultCertificate.setCertificateFile(certificateFile);
    }


    public String getKeyFile() {
        registerDefaultCertificate();
        return defaultCertificate.getKeyFile();
    }

    public void setKeyFile(String certificateKeyFile) {
        registerDefaultCertificate();
        defaultCertificate.setKeyFile(certificateKeyFile);
    }

    public enum CertificateVerification {
        NONE,
        OPTIONAL_NO_CA,
        OPTIONAL,
        REQUIRED;

        public static CertificateVerification fromString(String value) {
            if ("true".equalsIgnoreCase(value) ||
                    "yes".equalsIgnoreCase(value) ||
                    "require".equalsIgnoreCase(value) ||
                    "required".equalsIgnoreCase(value)) {
                return REQUIRED;
            } else if ("optional".equalsIgnoreCase(value) ||
                    "want".equalsIgnoreCase(value)) {
                return OPTIONAL;
            } else if ("optionalNoCA".equalsIgnoreCase(value) ||
                    "optional_no_ca".equalsIgnoreCase(value)) {
                return OPTIONAL_NO_CA;
            } else if ("false".equalsIgnoreCase(value) ||
                    "no".equalsIgnoreCase(value) ||
                    "none".equalsIgnoreCase(value)) {
                return NONE;
            } else {
                // Could be a typo. Don't default to NONE since that is not
                // secure. Force user to fix config. Could default to REQUIRED
                // instead.
                throw new IllegalArgumentException("The certificate verification value [" + value + "] is not recognised");
            }
        }
    }
}
