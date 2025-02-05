package ext.library.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * 信任所有 host name
 */
public enum TrustAllHostNames implements HostnameVerifier {

    /**
     * 实例
     */
    INSTANCE;

    @Override
    public boolean verify(String s, SSLSession sslSession) {
        return true;
    }

}
