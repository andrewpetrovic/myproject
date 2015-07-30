package com.itic.mobile.util.ui;

/**
 * 
 * @author andrew
 *
 */
public class TextViewUtils {
	public static String setText(String string){
		if(string == null || string.equals("null")){
			return "";
		}
		return string;		
	}

    public static boolean isNull(String string){
        if(string == null || string.equals("null")){
            return false;
        }
        return true;
    }
}
