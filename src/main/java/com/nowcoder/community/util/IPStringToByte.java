package com.nowcoder.community.util;

import java.nio.ByteBuffer;

/**
 * @author 曾文亮
 * @version 1.0.0
 * @email wenliang_zeng416@163.com
 * @date 2023年12月05日 22:25:48
 * @packageName com.nowcoder.community.util
 * @className IPStringToByte
 * @describe TODO
 */
public class IPStringToByte {
    private static final int IPV4_PART_COUNT = 4;
    private static final int IPV6_PART_COUNT = 8;
        /**
     * 将IP地址字符串转换为字节数组
     * @param ipString IP地址字符串
     * @return 转换后的字节数组
     */
    public static byte[] ipStringToBytes(String ipString) {
        // 首次遍历字符串，对字符进行分类
        boolean hasColon = false;
        boolean hasDot = false;
        for (int i = 0; i < ipString.length(); i++) {
            char c = ipString.charAt(i);
            if (c == '.') {
                hasDot = true;
            } else if (c == ':') {
                if (hasDot) {
                    return null;  // 不能在点号后面出现冒号
                }
                hasColon = true;
            } else if (Character.digit(c, 16) == -1) {
                return null;  // 其他字符必须是十进制或十六进制数字
            }
        }

        // 现在决定解析哪个地址族
        if (hasColon) {
            if (hasDot) {
                ipString = convertDottedQuadToHex(ipString);
                if (ipString == null) {
                    return null;
                }
            }
            return textToNumericFormatV6(ipString);
        } else if (hasDot) {
            return textToNumericFormatV4(ipString);
        }
        return null;
    }

        /**
     * 将点分十进制的IP地址转换为十六进制表示
     * @param ipString 点分十进制的IP地址，格式为"initialPart:penultimate ultimate"
     * @return 转换后的十六进制表示的IP地址，格式为"initialPart:penultimate:ultimate"
     */
    private static String convertDottedQuadToHex(String ipString) {
        int lastColon = ipString.lastIndexOf(':');
        String initialPart = ipString.substring(0, lastColon + 1);
        String dottedQuad = ipString.substring(lastColon + 1);
        byte[] quad = textToNumericFormatV4(dottedQuad);

        // 如果转换失败，则返回null
        if (quad == null) {
            return null;
        }

        // 将前三位和最后两位转换为十六进制字符串
        String penultimate = Integer.toHexString(((quad[0] & 0xff) << 8) | (quad[1] & 0xff));
        String ultimate = Integer.toHexString(((quad[2] & 0xff) << 8) | (quad[3] & 0xff));

        // 返回转换后的IP地址
        return initialPart + penultimate + ":" + ultimate;
    }

    /**
     * 将IPv6地址字符串转换为其数字字节数组格式。
     * @param ipString 要转换的IPv6地址字符串
     * @return IPv6地址的数字字节数组表示形式，如果输入无效则返回null
     * @throws NumberFormatException 如果输入包含无效字符
     * @since 1.0
     */
    private static byte[] textToNumericFormatV6(String ipString) {
        // 将IPv6地址字符串分割成多个部分
        String[] parts = ipString.split(":", IPV6_PART_COUNT + 2);
        if (parts.length < 3 || parts.length > IPV6_PART_COUNT + 1) {
            return null;  // 如果部分数量不符合IPv6地址的规范，则返回null
        }

        // 检查是否存在 "::"，表示省略了一段连续的0
        int skipIndex = -1;
        for (int i = 1; i < parts.length - 1; i++) {
            if (parts[i].length() == 0) {
                if (skipIndex >= 0) {
                    return null;  // 不能有多个 "::"
                }
                skipIndex = i;
            }
        }

        int partsHi;  // 要从 "::" 之前复制的部分数量
        int partsLo;  // 要从 "::" 之后复制的部分数量
        if (skipIndex >= 0) {
            // 如果存在 "::"，则检查是否同时覆盖了起始和结束位置
            partsHi = skipIndex;
            partsLo = parts.length - skipIndex - 1;
            if (parts[0].length() == 0 && --partsHi != 0) {
                return null;  // ^: 需要 ^::
            }
            if (parts[parts.length - 1].length() == 0 && --partsLo != 0) {
                return null;  // :$ 需要 ::$
            }
        } else {
            // 否则，将整个地址分配给 partsHi。起始和结束位置可能为空，但 parseHextet() 方法会检查这一点。
            partsHi = parts.length;
            partsLo = 0;
        }

        // 如果存在 "::"，则必须省略至少一部分。否则，部分数量必须刚好正确。
        int partsSkipped = IPV6_PART_COUNT - (partsHi + partsLo);
        if (!(skipIndex >= 0 ? partsSkipped >= 1 : partsSkipped == 0)) {
            return null;
        }

        // 现在将 hextet 解析为字节数组
        ByteBuffer rawBytes = ByteBuffer.allocate(2 * IPV6_PART_COUNT);
        try {
            for (int i = 0; i < partsHi; i++) {
                rawBytes.putShort(parseHextet(parts[i]));  // 解析并将高位部分放入字节缓冲区
            }
            for (int i = 0; i < partsSkipped; i++) {
                rawBytes.putShort((short) 0);  // 将省略的部分填充为0
            }
            for (int i = partsLo; i > 0; i--) {
                rawBytes.putShort(parseHextet(parts[parts.length - i]));  // 解析并将低位部分放入字节缓冲区
            }
        } catch (NumberFormatException ex) {
            return null;  // 如果解析 hextet 时发生异常，则返回null
        }
        return rawBytes.array();  // 返回字节缓冲区的字节数组表示形式
    }


    /**
     * 将IP地址字符串转换为字节数组（IPv4格式）
     * @param ipString IP地址字符串，格式为四个十进制数，每个数之间用点号分隔
     * @return 转换后的字节数组，每个字节表示IPv4地址的四个十进制数
     *         转换失败时返回null
     */
    private static byte[] textToNumericFormatV4(String ipString) {
        // 将IP地址字符串按点号分隔为四个部分
        String[] address = ipString.split("\\.", IPV4_PART_COUNT + 1);
        // 如果分隔得到的部分数量不等于4，则转换失败，返回null
        if (address.length != IPV4_PART_COUNT) {
            return null;
        }

        // 创建一个长度为4的字节数组
        byte[] bytes = new byte[IPV4_PART_COUNT];
        try {
            // 将每个部分解析为十进制数，并存储到字节数组中
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = parseOctet(address[i]);
            }
        } catch (NumberFormatException ex) {
            // 解析失败，转换失败，返回null
            return null;
        }

        // 转换成功，返回字节数组
        return bytes;
    }

        private static short parseHextet(String ipPart) {
        // 注意：我们已经验证过这个字符串只包含十六进制数字。
        int hextet = Integer.parseInt(ipPart, 16);
        // 如果十六进制数大于0xffff，抛出数字格式异常。
        if (hextet > 0xffff) {
            throw new NumberFormatException();
        }
        // 将十六进制数转换为短整型并返回。
        return (short) hextet;
    }

        private static byte parseOctet(String ipPart) {
        // 注意：我们已经验证了该字符串仅包含十六进制数字。
        int octet = Integer.parseInt(ipPart);
        // 不允许出现前导零，因为在IP地址中没有明确的规范是将前导零解释为十进制还是八进制。
        if (octet > 255 || (ipPart.startsWith("0") && ipPart.length() > 1)) {
            throw new NumberFormatException();
        }
        return (byte) octet;
    }


    public static void main(String[] args) {
        String ip = "127.0.0.1";
        String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        byte[] bytes = ipStringToBytes(ipv6);
        System.out.println(bytes);
        System.out.println(ipStringToBytes(ip));
    }
}
