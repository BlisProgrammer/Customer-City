package com.blis.customercity;

import java.io.Serializable;
import java.util.Objects;

public class Record implements Serializable{
    public final String id;
    public final String services_scope_cn;
    public final String service_hotline;
    public final String email;
    public final String address_cn;
    public final String added_detail_cn;
    public final String tips_cn;
    public final String category;
    public final String sub_category;
    public final String company;
    public Record(String id, String category, String sub_category, String company, String services_scope_cn, String service_hotline, String email, String address_cn, String added_detail_cn, String tips_cn){
        this.id = id;
        this.category = category;
        this.sub_category = sub_category;
        this.company = company;
        this.services_scope_cn = services_scope_cn;
        this.service_hotline = service_hotline;
        this.email = email;
        this.address_cn = address_cn;
        this.added_detail_cn = added_detail_cn;
        this.tips_cn = tips_cn;
    }
    public String formatToString(){
        StringBuilder stringBuilder = new StringBuilder();
//        if(!this.id.isEmpty()) stringBuilder.append("ID: ").append(this.id).append("\n");
        if(!this.services_scope_cn.isEmpty()) stringBuilder.append("").append(this.services_scope_cn).append("\n");
        if(!this.service_hotline.isEmpty()) stringBuilder.append("Hotline: ").append(this.service_hotline).append("\n");
        if(!this.email.isEmpty()) stringBuilder.append("Email: ").append(this.email).append("\n");
        if(!this.address_cn.isEmpty()) stringBuilder.append("Address: ").append(this.address_cn).append("\n");
        if(!this.added_detail_cn.isEmpty()) stringBuilder.append("Details: ").append(this.added_detail_cn).append("\n");
        if(!this.tips_cn.isEmpty()) stringBuilder.append("").append(this.tips_cn).append("\n");

        return stringBuilder.toString();
    }
    public String getShareString(){
        return "Contact information for " + company + ": \n" + formatToString();
    }
    public String getLine1Text() {
        return company;
    }
    public String getLine2Text() {
        return formatToString();
    }
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Record)){
            return false;
        }
        Record record2 = (Record) obj;
        return Objects.equals(record2.id, this.id);
    }
}