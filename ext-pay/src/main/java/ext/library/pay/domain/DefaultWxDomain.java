package ext.library.pay.domain;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import ext.library.pay.enums.RequestSuffix;
import ext.library.pay.util.WxPayUtil;
import ext.library.tool.constant.Symbol;
import ext.library.tool.util.HttpUtil;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * 微信域名管理
 */
public class DefaultWxDomain implements WxDomain {

    private static final String FLAG = Symbol.SLASH;

    /**
     * 是否使用沙箱
     */
    private final boolean sandbox;

    @Contract(pure = true)
    private DefaultWxDomain(boolean sandbox) {
        this.sandbox = sandbox;
    }

    @NotNull
    @Contract(value = "_->new", pure = true)
    public static DefaultWxDomain of(boolean sandbox) {
        return new DefaultWxDomain(sandbox);
    }

    @SneakyThrows({ParserConfigurationException.class, TransformerException.class, IOException.class, InterruptedException.class})
    @Override
    public String sendRequest(Map<String, String> params, @NotNull RequestSuffix rs) {
        // 获取请求地址
        final String url = getUrl(rs.getSuffix());
        return HttpUtil.post(url, Map.of("Content-Type", "application/xml"), WxPayUtil.mapToXml(params));
    }

    /**
     * 根据微信的建议，这里后续需要加上主备切换的功能
     *
     * @return java.lang.String
     */
    public String getDomain() {
        return MAIN1;
    }

    public String getUrl(@NotNull String suffix) {
        if (suffix.startsWith(FLAG)) {
            suffix = suffix.substring(1);
        }

        if (this.sandbox) {
            return getDomain() + "sandboxnew/pay/" + suffix;
        }
        return getDomain() + "pay/" + suffix;
    }

}
