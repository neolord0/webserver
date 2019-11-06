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


import kr.dogfoot.webserver.server.host.ssl.SSLConfig;
import kr.dogfoot.webserver.server.host.ssl.SSLConfigCertificate;

import javax.net.ssl.SSLSessionContext;

public class SSLUtil {
    protected final SSLConfig sslHostConfig;
    protected final SSLConfigCertificate certificate;
    private final String[] enabledProtocols;
    private final String[] enabledCiphers;

    public SSLUtil(SSLConfigCertificate certificate) {
        this(certificate, true);
    }

    public SSLUtil(SSLConfigCertificate certificate, boolean warnTls13) {
        this.certificate = certificate;
        this.sslHostConfig = certificate.getSSLHostConfig();

        enabledProtocols = SSLInfomation.enabledProtocols(warnTls13, sslHostConfig);
        enabledCiphers = SSLInfomation.getEnabledCiiphers(sslHostConfig);
    }

    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }

    public String[] getEnabledCiphers() {
        return enabledCiphers;
    }

    public final NeoSSLContext createSSLContext() throws Exception {
        NeoSSLContext sslContext = new NeoSSLContext(sslHostConfig.getSslProtocol());
        sslContext.init(KeyManagerMaker.getInstance(certificate).get(),
                TrustManagerMaker.getInstance(certificate).get(),
                null);

        SSLSessionContext sessionContext = sslContext.getServerSessionContext();
        if (sessionContext != null) {
            configureSessionContext(sessionContext);
        }

        return sslContext;
    }

    public void configureSessionContext(SSLSessionContext sslSessionContext) {
        if (sslHostConfig.getSessionCacheSize() > 0) {
            sslSessionContext.setSessionCacheSize(sslHostConfig.getSessionCacheSize());
        }
        if (sslHostConfig.getSessionTimeout() > 0) {
            sslSessionContext.setSessionTimeout(sslHostConfig.getSessionTimeout());
        }
    }
}
