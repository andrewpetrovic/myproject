package com.itic.mobile.util.string;
import java.util.regex.Pattern;

/**
 * 字符工具类
 */
public class CharUtils {

	/**
	 * 获得全拼
	 * @param hanzi 汉字字符串
	 * @return 全品字符串
	 */
	public static String getPinyin(String hanzi){
		String source = hanzi.trim();
		return HanyuToPinyin.converterToSpell(source).toUpperCase();
	}

	/**
	 * 获得拼音首字母
	 * @param hanzi 汉字字符串
	 * @return 拼音首字母
	 */
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

	/**
	 * 判断字符是否是中日韩文字
	 * @param c
	 * @return 是中日韩文字则反返回true
	 */
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


	/**
	 * 根据Unicode编码判断是否是中文汉字和符号
	 * @param strName
	 * @return 是中文汉字或者中文符号则返回true
	 */
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

	/**
	 * 根据Unicode编码判断中文汉字
	 * @param strName
	 * @return 是中文汉字则返回true
	 */
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

	/**
	 * 只能判断部分CJK字符（CJK统一汉字）
	 */
	public static boolean isChineseByREG(String str) {
		if (str == null) {
			return false;
		}
		Pattern pattern = Pattern.compile("[\\u4E00-\\u9FBF]+");
		return pattern.matcher(str.trim()).find();
	}

	/**
	 * 只能判断部分CJK字符（CJK统一汉字）
	 */
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
