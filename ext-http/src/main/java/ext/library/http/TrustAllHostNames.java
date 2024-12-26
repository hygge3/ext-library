package ext.library.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.jetbrains.annotations.Contract;

/**
 * 信任所有 host name
 */
public enum TrustAllHostNames implements HostnameVerifier {

    /**
     * 实例
     */
    INSTANCE;

    @Override
    @Contract(pure = true)
    public boolean verify(String s, SSLSession sslSession) {
        return true;
    }

}
