package com.itic.mobile.zfyj.qh.contacts.model;

import android.database.Cursor;

import com.itic.mobile.util.model.HashUtils;
import com.itic.mobile.util.string.CharUtils;
import com.itic.mobile.zfyj.qh.provider.Contract;

/**
 * Contact model类，用于UI与数据绑定
 * @author Andrea Ji
 */
public class Contact {
    public String contact_id;
    public int contact_color;
    public String contact_type;
    public String contact_name;
    public String org_name;
    public String post;
    public String tel_office;
    public String tel_cell;
    public String sim_imsi;
    public String tel_home;
    public String email;
    public String fax;
    public String txlmlid;
    public String pxh;

    public String getSortKey(){
        if (contact_name == null){
            return "#";
        }
        String pinyin = CharUtils.getPinYinHeadChar(contact_name);
        char c = pinyin.charAt(0);
        if (Character.isLetter(c)){
            return String.valueOf(c);
        }else{
            return "#";
        }
    }

    public String getImportHashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("contact_id").append(contact_id == null ? "" : contact_id)
                .append("contact_color").append(contact_color == 0 ? 0 : contact_color)
                .append("contact_type").append(contact_type == null ? "" : contact_type)
                .append("contact_name").append(contact_name == null ? "" : contact_name)
                .append("org_name").append(org_name == null ? "" : org_name)
                .append("post").append(post == null ? "" : post)
                .append("tel_office").append(tel_office == null ? "" : tel_office)
                .append("tel_cell").append(tel_cell == null ? "" : tel_cell)
                .append("sim_imsi").append(sim_imsi == null ? "" : sim_imsi)
                .append("tel_home").append(tel_home == null ? "" : tel_home)
                .append("email").append(email == null ? "" : email)
                .append("fax").append(fax == null ? "" : fax)
                .append("txlmlid").append(txlmlid == null? "" :txlmlid)
                .append("pxh").append(pxh == null? "" :pxh)
//                .append("sort_key").append(getSortKey() == null?"":getSortKey())
        ;
        return HashUtils.computeWeakHash(sb.toString());
    }
}
