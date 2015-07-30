package com.itic.mobile.util.string;

import android.util.Log;

import com.itic.mobile.util.bean.jt_t808.BCDHelper;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharUtils {

//	// 国标码和区位码转换常量
//	static final int GB_SP_DIFF = 160;
//	//存放国标一级汉字不同读音的起始区位码
//	static final int[] secPosValueList = {
//			1601, 1637, 1833, 2078, 2274, 2302, 2433, 2594, 2787,
//			3106, 3212, 3472, 3635, 3722, 3730, 3858, 4027, 4086,
//			4390, 4558, 4684, 4925, 5249, 5600};
//	static final char[] firstLetter = {
//			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j',
//			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
//			't', 'w', 'x', 'y', 'z'};
//
//	//获取一个字符串的拼音码
//	public static String getFirstLetter(String oriStr) {
//		String str = oriStr.toLowerCase();
//		StringBuffer buffer = new StringBuffer();
//		char ch;
//		char[] temp;
//		for (int i = 0; i < str.length(); i++) { //依次处理str中每个字符
//			ch = str.charAt(i);
//			temp = new char[] {ch};
//			byte[] uniCode = new String(temp).getBytes();
//			if (uniCode[0] < 128 && uniCode[0] > 0) { // 非汉字
//				buffer.append(temp);
//			} else {
//				buffer.append(convert(uniCode));
//			}
//		}
//		return buffer.toString();
//	}
//
//	/** 获取一个汉字的拼音首字母。
//	 * GB码两个字节分别减去160，转换成10进制码组合就可以得到区位码
//	 * 例如汉字“你”的GB码是0xC4/0xE3，分别减去0xA0（160）就是0x24/0x43
//	 * 0x24转成10进制就是36，0x43是67，那么它的区位码就是3667，在对照表中读音为‘n’
//	 */
//	static char convert(byte[] bytes) {
//		char result = '-';
//		int secPosValue = 0;
//		int i;
//		for (i = 0; i < bytes.length; i++) {
//			bytes[i] -= GB_SP_DIFF;
//		}
//		secPosValue = bytes[0] * 100 + bytes[1];
//		for (i = 0; i < 23; i++) {
//			if (secPosValue >= secPosValueList[i] && secPosValue < secPosValueList[i + 1]) {
//				result = firstLetter[i];
//				break;
//			}
//		}
//		return result;
//	}

//	public static String getFullPinYin(String source) {
////		if (!Arrays.asList(Collator.getAvailableLocales()).contains(Locale.CHINA)) {
////			return source;
////		}
//		ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(source);
//		if (tokens == null || tokens.size() == 0) {
//			return source;
//		}
//		StringBuffer result = new StringBuffer();
//		for (HanziToPinyin.Token token : tokens) {
//			if (token.type == HanziToPinyin.Token.PINYIN) {
//				result.append(token.target);
//			} else {
//				result.append(token.source);
//			}
//		}
//		return result.toString().toUpperCase();
//	}
//
//	public static String getFirstPinYin(String source) {
//		if (!Arrays.asList(Collator.getAvailableLocales()).contains(Locale.CHINA)) {
//			return source;
//		}
//		ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(source);
//		if (tokens == null || tokens.size() == 0) {
//			return source;
//		}
//		StringBuffer result = new StringBuffer();
//		for (HanziToPinyin.Token token : tokens) {
//			if (token.type == HanziToPinyin.Token.PINYIN) {
//				result.append(token.target.charAt(0));
//			} else {
//				result.append("#");
//			}
//		}
//		return result.toString().toUpperCase();
//	}

//	/**
//	 * Description : 根据汉字获得此汉字的拼音
//	 *
//	 * @param hanzhis
//	 * @return
//	 *
//	 */
//	public static String getPinYin(String hanzhis)
//	{
//		return getPinYin(hanzhis, false);
//	}
//
//	/**
//	 * Description : 根据汉字获得此汉字的拼音首字母
//	 *
//	 * @param hanzhis
//	 * @return
//	 *
//	 */
//	public static String getPinYinHeadChar(String hanzhis)
//	{
//		return getPinYin(hanzhis, true);
//	}
//
//	private static String getPinYin(String hanzhis, boolean isHeadChar)
//	{
//		int len = hanzhis.length();
//		char[] hanzhi = hanzhis.toCharArray();
//
//		// 设置输出格式
//		HanyuPinyinOutputFormat formatParam = new HanyuPinyinOutputFormat();
//		formatParam.setCaseType(HanyuPinyinCaseType.UPPERCASE);
//		formatParam.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//		formatParam.setVCharType(HanyuPinyinVCharType.WITH_V);
//
//		StringBuilder py = new StringBuilder();
//		Pattern pattern = Pattern.compile("^[/u4e00-/u9fa5]{0,128}$");
//		for (int i = 0; i < len; i++)
//		{
//			char c = hanzhi[i];
//			Matcher matcher = pattern.matcher(String.valueOf(c));
//			// 检查是否是汉字,如果不是汉字就不转换
//			if (!matcher.matches())
//			{
//				py.append(c);
//				continue;
//			}
//			// 对汉字进行转换成拼音
//			try
//			{
//				String[] t2 = PinyinHelper.toHanyuPinyinStringArray(c,
//						formatParam);
//				if (isHeadChar)
//				{
//					py.append(t2[0].charAt(0));
//				} else
//				{
//					py.append(t2[0]);
//				}
//
//			} catch (BadHanyuPinyinOutputFormatCombination e)
//			{
//				Log.e("CharUtils" , c + " to pinyin error!");
//				py.append(c);
//			}
//		}
//
//		return py.toString();
//	}

	public static String getPinyin(String hanzi){
		String source = hanzi.trim();
		return HanyuToPinyin.converterToSpell(source).toUpperCase();
	}

	public static String getPinYinHeadChar(String hanzi){
		String source = hanzi.trim();
		return HanyuToPinyin.converterToFirstSpell(source).toUpperCase();
	}

	/**
	 * 判断Username是否包含中日韩文字
	 * @param strName
	 * @return
	 */
	public static boolean isCJHLanguage(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isCJHLanguage(c)) {
				return true;
			}
		}
		return false;
	}
	
	// 判断字符是否是中日韩文字
	private static boolean isCJHLanguage(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS // 中日韩统一表意文字
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS // 中日韩兼容字符
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A // 中日韩统一表意文字扩充A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B // 中日韩统一表意文字扩充B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION // CJK符号和标点
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS // 半角及全角形式
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION // 一般标点符号
				|| ub == Character.UnicodeBlock.HANGUL_SYLLABLES // 朝鲜文音节
				|| ub == Character.UnicodeBlock.HANGUL_JAMO // 朝鲜文
				|| ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO // 朝鲜文兼容字母
				|| ub == Character.UnicodeBlock.HIRAGANA // 日语平假名
				|| ub == Character.UnicodeBlock.KATAKANA // 日语片假名
				|| ub == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS // 片假名音标扩充
		) {
			return true;
		}
		return false;
	}

	
	// 根据Unicode编码判断中文汉字和符号
	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS // 中日韩统一表意文字
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS // 中日韩兼容字符
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A // 中日韩统一表意文字扩充A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B // 中日韩统一表意文字扩充B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  //CJK 符号和标点 
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS //半角及全角形式
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION // 一般标点符号
                ) {
			return true;
		}
		return false;
	}

	// 完整的判断中文汉字和符号
	public static boolean isChinese(String strName) {
		char[] ch = strName.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isChinese(c)) {
				return true;
			}
		}
		return false;
	}

    // 完整的判断中文汉字
    public static boolean isChineseCharacters(String strName){
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChineseCharacter(c)) {
                return true;
            }
        }
        return false;
    }

    // 根据Unicode编码判断中文汉字
    private static boolean isChineseCharacter(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS // 中日韩统一表意文字
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS // 中日韩兼容字符
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A // 中日韩统一表意文字扩充A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B // 中日韩统一表意文字扩充B
                ) {
            return true;
        }
        return false;
    }

    // 只能判断部分CJK字符（CJK统一汉字）
	public static boolean isChineseByREG(String str) {
		if (str == null) {
			return false;
		}
		Pattern pattern = Pattern.compile("[\\u4E00-\\u9FBF]+");
		return pattern.matcher(str.trim()).find();
	}

	// 只能判断部分CJK字符（CJK统一汉字）
	public static boolean isChineseByName(String str) {
		if (str == null) {
			return false;
		}
		// 大小写不同：\\p 表示包含，\\P 表示不包含 
		// \\p{Cn} 的意思为 Unicode 中未被定义字符的编码，\\P{Cn} 就表示 Unicode中已经被定义字符的编码
		String reg = "\\p{InCJK Unified Ideographs}&&\\P{Cn}";
		Pattern pattern = Pattern.compile(reg);
		return pattern.matcher(str.trim()).find();
	}


}
