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

import kr.dogfoot.webserver.util.net.ssl.KeyStoreUtil;
import kr.dogfoot.webserver.util.net.ssl.NeoSSLContext;
import kr.dogfoot.webserver.util.net.ssl.cipher.Authentication;

import java.io.IOException;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.Set;

public class SSLConfigCertificate {

    static final String DEFAULT_KEYSTORE_PROVIDER =
            System.getProperty("javax.ssl.ssl.keyStoreProvider");
    static final String DEFAULT_KEYSTORE_TYPE =
            System.getProperty("javax.ssl.ssl.keyStoreType", "JKS");
    private final SSLConfig sslConfig;
    private final Type type;
    private transient NeoSSLContext sslContext;
    private String keyPassword = null;

    // keystore
    private String keyAlias;
    private String keystorePassword = "changeit";
    private String keystoreFile = System.getProperty("user.home") + "/.keystore";
    private String keystoreProvider = DEFAULT_KEYSTORE_PROVIDER;
    private String keystoreType = DEFAULT_KEYSTORE_TYPE;
    private KeyStore keystore = null;

    // PEM
    private String chainFile;
    private String certificateFile;
    private String keyFile;

    // Certificate store type
    private StoreType storeType = null;

    public SSLConfigCertificate() {
        this(null, Type.UNDEFINED);
    }

    public SSLConfigCertificate(SSLConfig sslHostConfig, Type type) {
        this.sslConfig = sslHostConfig;
        this.type = type;
    }


    public NeoSSLContext getSslContext() {
        return sslContext;
    }


    public void setSslContext(NeoSSLContext sslContext) {
        this.sslContext = sslContext;
    }


    public SSLConfig getSSLHostConfig() {
        return sslConfig;
    }

    // Common

    public Type getType() {
        return type;
    }


    public String getKeyPassword() {
        return keyPassword;
    }


    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getKeystoreFile() {
        return keystoreFile;
    }

    public void setKeystoreFile(String keystoreFile) {
        this.keystoreFile = keystoreFile;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getKeystoreProvider() {
        return keystoreProvider;
    }

    public void setKeystoreProvider(String keystoreProvider) {
        setStoreType("Certificate.keystoreProvider", StoreType.KEYSTORE);
        this.keystoreProvider = keystoreProvider;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(String keystoreType) {
        setStoreType("Certificate.keystoreType", StoreType.KEYSTORE);
        this.keystoreType = keystoreType;
    }

    public KeyStore getKeystore() throws IOException {
        KeyStore result = keystore;

        if (result == null && storeType == StoreType.KEYSTORE) {
            result = KeyStoreUtil.load(getKeystoreType(),
                    getKeystoreProvider(), getKeystoreFile(),
                    getKeystorePassword());
        }

        return result;
    }

    public void setKeystore(KeyStore keystore) {
        this.keystore = keystore;
    }

    public String getChainFile() {
        return chainFile;
    }

    // for PEM
    public void setChainFile(String chainFile) {
        setStoreType("Certificate.chainFile", StoreType.PEM);
        this.chainFile = chainFile;
    }

    public String getCertificateFile() {
        return certificateFile;
    }

    public void setCertificateFile(String certificateFile) {
        setStoreType("Certificate.certificateFile", StoreType.PEM);
        this.certificateFile = certificateFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        setStoreType("Certificate.keyFile", StoreType.PEM);
        this.keyFile = keyFile;
    }

    private void setStoreType(String name, StoreType type) {
        if (storeType == null) {
            storeType = type;
        } else if (storeType != type) {
            StringBuffer msg = new StringBuffer();
            msg.append("The property [")
                    .append(name)
                    .append("] was set on the SSLConfigCertificate is for certificate storage type [")
                    .append(type)
                    .append("] but the certificate is being used with a storage of type [")
                    .append(this.storeType)
                    .append("]");

            // log.warn(msg.toString());
        }
    }

    public enum Type {

        UNDEFINED,
        RSA(Authentication.RSA),
        DSA(Authentication.DSS),
        EC(Authentication.ECDH, Authentication.ECDSA);

        private final Set<Authentication> compatibleAuthentications;

        Type(Authentication... authentications) {
            compatibleAuthentications = new HashSet<>();
            if (authentications != null) {
                for (Authentication authentication : authentications) {
                    compatibleAuthentications.add(authentication);
                }
            }
        }

        public boolean isCompatibleWith(Authentication au) {
            return compatibleAuthentications.contains(au);
        }
    }

    enum StoreType {
        KEYSTORE,
        PEM
    }
}
