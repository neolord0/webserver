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

package kr.dogfoot.webserver.util.net.ssl;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

public class NeoSSLContext {
    private SSLContext context;
    private KeyManager[] kms;
    private TrustManager[] tms;

    public NeoSSLContext(String protocol) throws NoSuchAlgorithmException {
        context = SSLContext.getInstance(protocol);
    }

    public void init(KeyManager[] kms, TrustManager[] tms, SecureRandom sr)
            throws KeyManagementException {
        this.kms = kms;
        this.tms = tms;

        context.init(kms, tms, sr);
    }

    public SSLSessionContext getServerSessionContext() {
        return context.getServerSessionContext();
    }

    public SSLEngine createSSLEngine() {
        return context.createSSLEngine();
    }

    public SSLServerSocketFactory getServerSocketFactory() {
        return context.getServerSocketFactory();
    }

    public SSLParameters getSupportedSSLParameters() {
        return context.getSupportedSSLParameters();
    }

    public X509Certificate[] getCertificateChain(String alias) {
        X509Certificate[] result = null;
        if (kms != null) {
            for (int i = 0; i < kms.length && result == null; i++) {
                if (kms[i] instanceof X509KeyManager) {
                    result = ((X509KeyManager) kms[i]).getCertificateChain(alias);
                }
            }
        }
        return result;
    }

    public X509Certificate[] getAcceptedIssuers() {
        Set<X509Certificate> certs = new HashSet<>();
        if (tms != null) {
            for (TrustManager tm : tms) {
                if (tm instanceof X509TrustManager) {
                    X509Certificate[] accepted = ((X509TrustManager) tm).getAcceptedIssuers();
                    if (accepted != null) {
                        for (X509Certificate c : accepted) {
                            certs.add(c);
                        }
                    }
                }
            }
        }
        return certs.toArray(new X509Certificate[certs.size()]);
    }
}
