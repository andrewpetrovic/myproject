package com.itic.mobile.zfyj.qh.contacts.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by JEEKR on 2015/2/6.
 */
public class ContactType {
    public String type_id;
    public String type_name;
    public String type_color;
    @SerializedName("type_abstract")
    public String type_abstract;
    public int order_in_category;
}
