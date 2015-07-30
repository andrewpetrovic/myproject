package com.itic.mobile.util.bean.jt_t808;

import java.util.ArrayList;

/**
 * 获得CRC计算和异或计算结果
 * @version  v1.0.0
 * @author  Andrea Ji
 * @date  2015-02-09
 * 变更历史:
 * 提交日期  姓名  主线版本  修改原因
 * ---------------------------------------------------------*
 * 2015-02-09  Andrea Ji v1.0.0  第一次提交
 */
public class CheckHelper {

    /**
     * 得出CRC计算结果
     * @param buf 要计算CRC的字符串
     * @return int
     */
    public static int getCRC(String buf) {
        int crc = 0xFFFF; // initial value
        int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)

        for (int j = 0; j < buf.length(); j++) {
            char b = buf.charAt(j);
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        return crc;
    }

    /**
     * 得出CRC计算结果
     * @param buf 要计算CRC的字符串
     * @return String
     */
    public static String getCRCString(String buf) {
        int crc = 0xFFFF; // initial value
        int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)

        for (int j = 0; j < buf.length(); j++) {
            char b = buf.charAt(j);
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        String str = "" + (char) (crc / 256) + (char) (crc % 256);
        return str;
    }

    /**
     * 得出异或计算结果
     * @param data   要计算异或的数据
     * @param offset 偏移位
     * @param length 长度
     * @return byte
     */
    public static byte checkXOR(byte[] data, int offset, int length) {
        byte xda = 0;
        for (int i = 0; i < length; i++) {
            xda = (byte) ((data[i + offset] ^ xda) & 0xFF);
        }
        return xda;
    }

    /**
     * 校验并转义(异或效验)
     * @param data   要计算异或的数据
     * @param offset 偏移位
     * @param length 长度
     */
    public static void XORAndEscape(ArrayList<Byte> data, int offset, int length) {
        byte xda = 0;
        for (int i = offset; i < offset + length; i++) {
            xda = (byte) (data.get(i) ^ xda);
            if (data.get(i) == 0x7e)// 转义 向后插入一个0x02
            {
                data.set(i, (byte) 0x7d);
                data.add(i + 1, (byte) 0x02);
                length++;
                i++;
            } else if (data.get(i) == 0x7d)// 转义 向后插入一个0x01
            {
                data.add(i + 1, (byte) 0x01);
                length++;
                i++;
            }
        }
        data.add(offset + length, xda);
        if (xda == 0x7e)// 转义 向后插入一个0x02
            data.add(offset + length + 1, (byte) 0x02);
        else if (xda == 0x7d)// 转义 向后插入一个0x01
            data.add(offset + length + 1, (byte) 0x01);
    }
}
