/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kr.dogfoot.webserver.util.net.ssl;

import kr.dogfoot.webserver.util.ConfigFileLoader;
import kr.dogfoot.webserver.util.Message;

import java.io.*;
import java.net.URI;
import java.security.DomainLoadStoreParameter;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;

public class KeyStoreUtil {
    public static KeyStore load(String type, String provider, String path,
                                String pass) throws IOException {
        KeyStore ks = null;
        InputStream istream = null;
        try {
            if (provider == null) {
                ks = KeyStore.getInstance(type);
            } else {
                ks = KeyStore.getInstance(type, provider);
            }
            if ("DKS".equalsIgnoreCase(type)) {
                URI uri = ConfigFileLoader.getURI(path);
                ks.load(new DomainLoadStoreParameter(uri, Collections.emptyMap()));
            } else {
                // Some key store types (e.g. hardware) expect the InputStream
                // to be null
                if (!("PKCS11".equalsIgnoreCase(type) ||
                        "".equalsIgnoreCase(path)) ||
                        "NONE".equalsIgnoreCase(path)) {
                    istream = ConfigFileLoader.getFileInputStream(path);
                }

                // The digester cannot differentiate between null and "".
                // Unfortunately, some key stores behave differently with null
                // and "".
                // JKS key stores treat null and "" interchangeably.
                // PKCS12 key stores (Java 7 onwards) don't return the cert if
                // null is used.
                // Key stores that do not use passwords expect null
                // Therefore:
                // - generally use null if pass is null or ""
                // - for JKS or PKCS12 only use null if pass is null
                //   (because JKS will auto-switch to PKCS12)
                char[] storePass = null;
                if (pass != null && (!"".equals(pass) ||
                        "JKS".equalsIgnoreCase(type) || "PKCS12".equalsIgnoreCase(type))) {
                    storePass = pass.toCharArray();
                }
                load(ks, istream, storePass);
            }
        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (IOException ioe) {
            // May be expected when working with a trust store
            // Re-throw. Caller will catch and log as required
            throw ioe;
        } catch (Exception ex) {
            String msg = "keystore load failed";
            Message.log(msg);
            throw new IOException(msg);
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException ioe) {
                    // Do nothing
                }
            }
        }

        return ks;
    }

    private static void load(KeyStore keystore, InputStream is, char[] storePass)
            throws NoSuchAlgorithmException, CertificateException, IOException {
        if (is == null) {
            keystore.load(null, storePass);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int numRead;
            while ((numRead = is.read(buf)) >= 0) {
                baos.write(buf, 0, numRead);
            }
            baos.close();
            // Don't close is. That remains the callers responsibility.

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

            keystore.load(bais, storePass);
        }
    }
}
