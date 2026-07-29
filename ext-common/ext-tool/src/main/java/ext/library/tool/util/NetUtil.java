package ext.library.tool.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 网络相关工具
 */
public final class NetUtil {

    private static final String localHost = "127.0.0.1";

    private NetUtil() {
    }

    /**
     * 获取 服务器 hostname
     *
     * @return hostname
     */
    public static String getHostName() {
        String hostname;
        try {
            InetAddress address = InetAddress.getLocalHost();
            // force a best effort reverse DNS lookup
            hostname = address.getHostName();
            if (StringUtil.isBlank(hostname)) {
                hostname = address.toString();
            }
        } catch (UnknownHostException ignore) {
            hostname = localHost;
        }
        return hostname;
    }

    /**
     * 获取 服务器 HostIp
     *
     * @return HostIp
     */
    public static String getHostIp() {
        String hostAddress;
        try {
            InetAddress address = getLocalHostLanAddress();
            // force a best effort reverse DNS lookup
            hostAddress = address.getHostAddress();
            if (StringUtil.isBlank(hostAddress)) {
                hostAddress = address.toString();
            }
        } catch (UnknownHostException ignore) {
            hostAddress = localHost;
        }
        return hostAddress;
    }

    /**
     * <a href=
     * "https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java">getting-the-ip-address-of-the-current-machine-using-java</a>
     *
     * <p>
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the
     * machine's LAN IP address.
     * <p/>
     * This method is intended for use as a replacement of JDK method
     * <code>InetAddress.getLocalHost</code>, because that method is ambiguous on Linux
     * systems. Linux systems enumerate the loopback network interface the same way as
     * regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code>
     * method does not specify the algorithm used to select the address returned under
     * such circumstances, and will often return the loopback address, which is not valid
     * for network communication. Details
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the host
     * machine to determine the IP address most likely to be the machine's LAN address. If
     * the machine has multiple IP addresses, this method will prefer a site-local IP
     * address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and
     * will return the first site-local address if the machine has more than one), but if
     * the machine does not hold a site-local address, this method will return simply the
     * first non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection algorithm,
     * it will fall back to calling and returning the result of JDK method
     * <code>InetAddress.getLocalHost</code>.
     * <p/>
     *
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     */
    private static InetAddress getLocalHostLanAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration<NetworkInterface> iFaces = NetworkInterface.getNetworkInterfaces(); iFaces.hasMoreElements(); ) {
                NetworkInterface iFace = iFaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration<InetAddress> inetAdders = iFace.getInetAddresses(); inetAdders.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAdders.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it
                            // immediately...
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local
                            // address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback
                            // non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will
                            // be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other
                // non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it
                // might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("JDK InetAddress.getLocalHost() 方法意外返回 null");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("无法确定局域网地址：" + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    /**
     * 尝试端口是否可用
     *
     * @param port 端口号
     *
     * @return 端口可用返回 true，被占用返回 false
     */
    public static boolean tryPort(int port) {
        try (ServerSocket ignore = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将 ip 转成 InetAddress
     *
     * @param ip ip
     *
     * @return InetAddress
     */
    public static InetAddress getInetAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 判断是否内网 ip
     *
     * @param ip ip
     *
     * @return boolean
     */
    public static boolean isInternalIp(String ip) {
        return isInternalIp(getInetAddress(ip));
    }

    /**
     * 判断是否内网 ip
     *
     * @param address InetAddress
     *
     * @return boolean
     */
    public static boolean isInternalIp(InetAddress address) {
        if (isLocalIp(address)) {
            return true;
        }
        return isInternalIp(address.getAddress());
    }

    /**
     * 判断是否本地 ip
     *
     * @param address InetAddress
     *
     * @return boolean
     */
    public static boolean isLocalIp(InetAddress address) {
        return address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isSiteLocalAddress();
    }

    /**
     * 判断是否内网 ip（支持 IPv4 和 IPv6）
     * <p>
     * IPv4 内网地址范围：
     * <ul>
     *   <li>10.0.0.0/8 (10.x.x.x)</li>
     *   <li>172.16.0.0/12 (172.16.x.x ~ 172.31.x.x)</li>
     *   <li>192.168.0.0/16 (192.168.x.x)</li>
     * </ul>
     * <p>
     * IPv6 内网地址范围：
     * <ul>
     *   <li>::1/128 - 环回地址</li>
     *   <li>fe80::/10 - 链路本地地址</li>
     *   <li>fc00::/7 - 唯一本地地址 (ULA)</li>
     * </ul>
     *
     * @param addr ip 地址字节数组（IPv4 为 4 字节，IPv6 为 16 字节）
     *
     * @return 是否为内网 ip
     */
    public static boolean isInternalIp(byte[] addr) {
        return switch (addr.length) {
            case 4 -> isInternalIpv4(addr);
            case 16 -> isInternalIpv6(addr);
            default -> false;
        };
    }

    /**
     * 判断是否 IPv4 内网地址
     */
    private static boolean isInternalIpv4(byte[] addr) {
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        // 10.x.x.x/8
        final byte SECTION_10 = 0x0A;
        // 172.16.x.x/12 ~ 172.31.x.x
        final byte SECTION_172 = (byte) 0xAC;
        final byte SECTION_172_MIN = (byte) 0x10;
        final byte SECTION_172_MAX = (byte) 0x1F;
        // 192.168.x.x/16
        final byte SECTION_192 = (byte) 0xC0;
        final byte SECTION_168 = (byte) 0xA8;

        return switch (b0) {
            case SECTION_10 -> true;
            case SECTION_172 -> b1 >= SECTION_172_MIN && b1 <= SECTION_172_MAX;
            case SECTION_192 -> b1 == SECTION_168;
            default -> false;
        };
    }

    /**
     * 判断是否 IPv6 内网地址
     * <ul>
     *   <li>::1 - 环回地址（前 15 字节全 0，最后字节为 1）</li>
     *   <li>fe80::/10 - 链路本地地址（第一字节 0xFE，第二字节高 2 位为 10）</li>
     *   <li>fc00::/7 - 唯一本地地址（第一字节为 0xFC 或 0xFD）</li>
     * </ul>
     */
    private static boolean isInternalIpv6(byte[] addr) {
        final byte b0 = addr[0];
        final byte b1 = addr[1];

        // ::1 环回地址检测
        if (isIpv6Loopback(addr)) {
            return true;
        }
        // fe80::/10 链路本地地址：第一字节 0xFE，第二字节 0x80-0xBF
        if (b0 == (byte) 0xFE && (b1 & 0xC0) == 0x80) {
            return true;
        }
        // fc00::/7 唯一本地地址：第一字节为 0xFC 或 0xFD
        return b0 == (byte) 0xFC || b0 == (byte) 0xFD;
    }

    /**
     * 判断是否 IPv6 环回地址 (::1)
     */
    private static boolean isIpv6Loopback(byte[] addr) {
        for (int i = 0; i < 15; i++) {
            if (addr[i] != 0) {
                return false;
            }
        }
        return addr[15] == 1;
    }

}