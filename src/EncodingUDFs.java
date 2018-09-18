import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class EncodingUDFs {

    // IPv4 占用 4 个字节，IPv6 占用 16 个字节，端口号占用 2 个字节 (unsigned short)
    // 所以 IPv4 + 端口号编码后是 6 个字节，IPv6 + 端口号编码后是 18 个字节。
    private static final int V4SZ = 6;
    private static final int V6SZ = 18;

    /***
     * 将 IP 地址和端口号编码成二进制字节序列。
     * @param IPString IP 地址字符串
     * @param port 端口号，VoltDB 不支持 unsigned short，需要传入一个 4 字节整型
     * @return 编码好的字节序列
     * @throws UnknownHostException
     */
    public byte[] encode(String IPString, Integer port) throws UnknownHostException {
        // 如果字符串包含的不是有效的 IP 地址，这里会抛出 UnknownHostException
        // 在 VoltDB 中会表现为 SQL 异常。
        InetAddress ip = InetAddress.getByName(IPString);
        // 验证是不是有效的端口号。
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port number");
        }
        ByteBuffer resultBuffer;
        // 根据传入的 IP 是 IPv4 还是 IPv6，构建不同大小的字节缓存。
        if (ip instanceof Inet4Address) {
            resultBuffer = ByteBuffer.allocate(V4SZ);
        }
        else {
            resultBuffer = ByteBuffer.allocate(V6SZ);
        }
        // 放入二进制形式的 IP 地址
        resultBuffer.put(ip.getAddress());
        // 将 Integer 转换为 unsigned short 形式存入缓存。
        resultBuffer.putShort((short)(port & 0xffff));
        return resultBuffer.array();
    }

    /**
     * 验证编码好的二进制序列是不是有效（只检测序列长度）
     * @param bytes 传入的二进制序列
     */
    private void validateBytes(byte[] bytes) {
        if (bytes == null || (bytes.length != V4SZ && bytes.length != V6SZ)) {
            throw new IllegalArgumentException("Invalid encoded data");
        }
    }

    /**
     * 从二进制序列中解析出 IP 地址
     * @param bytes 传入的二进制序列
     * @return 字符串形式的 IP 地址
     * @throws UnknownHostException
     */
    public String extractIP(byte[] bytes) throws UnknownHostException {
        validateBytes(bytes);
        InetAddress ip = InetAddress.getByAddress(Arrays.copyOfRange(bytes, 0, bytes.length - 2));
        return ip.getHostAddress();
    }

    /**
     * 从二进制序列中解析出端口号
     * @param bytes bytes 传入的二进制序列
     * @return 端口号
     */
    public Integer extractPort(byte[] bytes) {
        validateBytes(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        // 取出的短整型默认为有符号的，要做一次转换。
        return buffer.getShort(buffer.capacity() - 2) & 0xffff;
    }
}
