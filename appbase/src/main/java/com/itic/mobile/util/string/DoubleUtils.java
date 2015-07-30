package com.itic.mobile.util.string;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 
 * @author andrew
 *
 */
public class DoubleUtils {
	public static String format(double dNum){
		DecimalFormat df=(DecimalFormat) NumberFormat.getInstance(); 
		df.setGroupingUsed(false);
		df.setMaximumFractionDigits(6);
		return df.format(dNum);
	}
}
