package kr.dogfoot.webserver.util.net.ssl;

import kr.dogfoot.webserver.server.host.ssl.SSLConfig;
import kr.dogfoot.webserver.server.host.ssl.SSLConfigCertificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;

public class KeyManagerMaker {
    private final SSLConfigCertificate certificate;
    private final SSLConfig sslHostConfig;
    private String keyAlias;
    private String keyPass;
    private char[] keyPassArray;

    private KeyManagerMaker(SSLConfigCertificate certificate) {
        this.certificate = certificate;
        this.sslHostConfig = certificate.getSSLHostConfig();
    }

    public static KeyManagerMaker getInstance(SSLConfigCertificate certificate) {
        return new KeyManagerMaker(certificate);
    }

    public KeyManager[] get() throws Exception {
        keyAlias = certificate.getKeyAlias();
        keyPass = certificate.getKeyPassword();

        // This has to be here as it can't be moved to SSLConfig since the
        // defaults vary between JSSE and OpenSSL.
        if (keyPass == null) {
            keyPass = certificate.getKeystorePassword();
        }

        KeyStore ks = certificate.getKeystore();
        KeyStore ksUsed;

        keyPassArray = keyPass.toCharArray();

        if (ks != null) {
            if (keyAlias != null && !ks.isKeyEntry(keyAlias)) {
                throw new IOException(keyAlias + "Alias does not identify a key entry");
            } else if (keyAlias == null) {
                getKeyAliasOfFirstPriveKey(ks);

                if (keyAlias == null) {
                    throw new IOException("null Alias does not identify a key entry");
                }
            }

            Key k = ks.getKey(keyAlias, keyPassArray);
            if (k != null && !"DKS".equalsIgnoreCase(certificate.getKeystoreType()) &&
                    "PKCS#8".equalsIgnoreCase(k.getFormat())) {
                ksUsed = forPKCS8Key(k, ks.getCertificateChain(keyAlias));
            } else {
                ksUsed = ks;
            }
        } else {
            if (certificate.getCertificateFile() == null) {
                throw new IOException("SSLConfig attribute certificateFile must be defined when using an SSL connector");
            }

            ksUsed = fromPEMFile();
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(sslHostConfig.getKeyManagerAlgorithm());
        kmf.init(ksUsed, keyPassArray);

        KeyManager[] kms = kmf.getKeyManagers();
        if (kms != null && ksUsed == ks) {
            String alias = keyAlias;
            if ("JKS".equals(certificate.getKeystoreType())) {
                alias = alias.toLowerCase(Locale.ENGLISH);
            }
            for (int i = 0; i < kms.length; i++) {
                kms[i] = new JSSEKeyManager((X509KeyManager) kms[i], alias);
            }
        }
        return kms;
    }

    private String getKeyAliasOfFirstPriveKey(KeyStore ks) throws KeyStoreException, IOException {
        Enumeration<String> aliases = ks.aliases();
        if (!aliases.hasMoreElements()) {
            throw new IOException("No aliases for private keys found in key store");
        }

        String alias = null;
        while (aliases.hasMoreElements() && alias == null) {
            alias = aliases.nextElement();
            if (ks.isKeyEntry(alias) == false) {
                alias = null;
            }
        }
        return alias;
    }

    private KeyStore forPKCS8Key(Key k, Certificate[] chain) throws KeyStoreException, NoSuchProviderException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks;

        String provider = certificate.getKeystoreProvider();
        if (provider == null) {
            ks = KeyStore.getInstance(certificate.getKeystoreType());
        } else {
            ks = KeyStore.getInstance(certificate.getKeystoreType(), provider);
        }

        ks.load(null, null);
        ks.setKeyEntry(keyAlias, k, keyPassArray, chain);
        return ks;
    }

    private KeyStore fromPEMFile() throws IOException, GeneralSecurityException {
        PEMFile privateKeyFile = new PEMFile(
                certificate.getKeyFile() != null ? certificate.getKeyFile() : certificate.getCertificateFile(),
                keyPass);
        PEMFile certificateFile = new PEMFile(certificate.getCertificateFile());

        Collection<Certificate> chain = new ArrayList<>();
        chain.addAll(certificateFile.getCertificates());
        if (certificate.getChainFile() != null) {
            PEMFile certificateChainFile = new PEMFile(certificate.getChainFile());
            chain.addAll(certificateChainFile.getCertificates());
        }

        if (keyAlias == null) {
            keyAlias = "mykey";
        }

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        ks.setKeyEntry(keyAlias, privateKeyFile.getPrivateKey(), keyPass.toCharArray(),
                chain.toArray(new Certificate[chain.size()]));
        return ks;
    }
}
