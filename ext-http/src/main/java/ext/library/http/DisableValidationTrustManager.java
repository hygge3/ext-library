package ext.library.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * 不进行证书校验
 *
 */
public enum DisableValidationTrustManager implements X509TrustManager {

    /**
     * 实例
     */
    INSTANCE;

    /**
     * 获取 TrustManagers
     *
     * @return TrustManager 数组
     */
    @NotNull
    @Contract(value = "->new", pure = true)
    public TrustManager[] getTrustManagers() {
        return new TrustManager[]{this};
    }

    @Override
    @Contract(pure = true)
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
    }

    @Override
    @Contract(pure = true)
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
    }

    @NotNull
    @Contract(value = "->new", pure = true)
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

}
