package com.blis.customercity.data;

import android.content.Context;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.blis.customercity.R;

import java.io.Serializable;
import java.util.ArrayList;

public class OnlineRecord implements Serializable {
    private String id;
    private String company_id;
    private String company_name_en;
    private String company_name_cn;
    private String distributor_en;
    private String distributor_cn;
    private String services_scope_en;
    private String services_scope_cn;
    private String service_hotline;
    private String email;
    private String address_cn;
    private String address_en;
    private String added_detail_en;
    private String added_detail_cn;
    private String tips_en;
    private String tips_cn;
    private String records_meta_keywords_en;
    private String records_meta_keywords_cn;
    private String records_meta_desc_en;
    private String records_meta_desc_cn;
    private String companies_meta_keywords_en;
    private String companies_meta_keywords_cn;
    private String companies_meta_desc_en;
    private String companies_meta_desc_cn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getCompany_name_en() {
        return company_name_en;
    }

    public void setCompany_name_en(String company_name_en) {
        this.company_name_en = company_name_en;
    }

    public String getCompany_name_cn() {
        return company_name_cn;
    }

    public void setCompany_name_cn(String company_name_cn) {
        this.company_name_cn = company_name_cn;
    }

    public String getDistributor_en() {
        return distributor_en;
    }

    public void setDistributor_en(String distributor_en) {
        this.distributor_en = distributor_en;
    }

    public String getDistributor_cn() {
        return distributor_cn;
    }

    public void setDistributor_cn(String distributor_cn) {
        this.distributor_cn = distributor_cn;
    }

    public String getServices_scope_en() {
        return services_scope_en;
    }

    public void setServices_scope_en(String services_scope_en) {
        this.services_scope_en = services_scope_en;
    }

    public String getServices_scope_cn() {
        return services_scope_cn;
    }

    public void setServices_scope_cn(String services_scope_cn) {
        this.services_scope_cn = services_scope_cn;
    }

    public String getService_hotline() {
        return service_hotline;
    }

    public void setService_hotline(String service_hotline) {
        this.service_hotline = service_hotline;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress_cn() {
        return address_cn;
    }

    public void setAddress_cn(String address_cn) {
        this.address_cn = address_cn;
    }

    public String getAddress_en() {
        return address_en;
    }

    public void setAddress_en(String address_en) {
        this.address_en = address_en;
    }

    public String getAdded_detail_en() {
        return added_detail_en;
    }

    public void setAdded_detail_en(String added_detail_en) {
        this.added_detail_en = added_detail_en;
    }

    public String getAdded_detail_cn() {
        return added_detail_cn;
    }

    public void setAdded_detail_cn(String added_detail_cn) {
        this.added_detail_cn = added_detail_cn;
    }

    public String getTips_en() {
        return tips_en;
    }

    public void setTips_en(String tips_en) {
        this.tips_en = tips_en;
    }

    public String getTips_cn() {
        return tips_cn;
    }

    public void setTips_cn(String tips_cn) {
        this.tips_cn = tips_cn;
    }

    public String getRecords_meta_keywords_en() {
        return records_meta_keywords_en;
    }

    public void setRecords_meta_keywords_en(String records_meta_keywords_en) {
        this.records_meta_keywords_en = records_meta_keywords_en;
    }

    public String getRecords_meta_keywords_cn() {
        return records_meta_keywords_cn;
    }

    public void setRecords_meta_keywords_cn(String records_meta_keywords_cn) {
        this.records_meta_keywords_cn = records_meta_keywords_cn;
    }

    public String getRecords_meta_desc_en() {
        return records_meta_desc_en;
    }

    public void setRecords_meta_desc_en(String records_meta_desc_en) {
        this.records_meta_desc_en = records_meta_desc_en;
    }

    public String getRecords_meta_desc_cn() {
        return records_meta_desc_cn;
    }

    public void setRecords_meta_desc_cn(String records_meta_desc_cn) {
        this.records_meta_desc_cn = records_meta_desc_cn;
    }

    public String getCompanies_meta_keywords_en() {
        return companies_meta_keywords_en;
    }

    public void setCompanies_meta_keywords_en(String companies_meta_keywords_en) {
        this.companies_meta_keywords_en = companies_meta_keywords_en;
    }

    public String getCompanies_meta_keywords_cn() {
        return companies_meta_keywords_cn;
    }

    public void setCompanies_meta_keywords_cn(String companies_meta_keywords_cn) {
        this.companies_meta_keywords_cn = companies_meta_keywords_cn;
    }

    public String getCompanies_meta_desc_en() {
        return companies_meta_desc_en;
    }

    public void setCompanies_meta_desc_en(String companies_meta_desc_en) {
        this.companies_meta_desc_en = companies_meta_desc_en;
    }

    public String getCompanies_meta_desc_cn() {
        return companies_meta_desc_cn;
    }

    public void setCompanies_meta_desc_cn(String companies_meta_desc_cn) {
        this.companies_meta_desc_cn = companies_meta_desc_cn;
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
    public ArrayList<LinearLayout> formatToLayouts(Context context){
        ArrayList<LinearLayout> layoutList = new ArrayList<>();
        if(!this.service_hotline.isEmpty()){
            layoutList.add(generateItemLayout(context, R.drawable.phone_icon, this.service_hotline));
        }
        if(!this.email.isEmpty()){
            layoutList.add(generateItemLayout(context, R.drawable.email_icon, this.email));
        }
        if(!this.address_cn.isEmpty()){
            layoutList.add(generateItemLayout(context, R.drawable.address_icon, this.address_cn));
        }
        if(!this.added_detail_cn.isEmpty()){
            layoutList.add(generateItemLayout(context, R.drawable.details_icon, this.added_detail_cn));
        }
        if(!this.tips_cn.isEmpty()){
            layoutList.add(generateItemLayout(context, R.drawable.tips_icon, this.tips_cn));
        }
        return layoutList;
    }
    private LinearLayout generateItemLayout(Context context, int iconDrawableID, String text){
        float scale = context.getResources().getDisplayMetrics().density;
        // Setup linear layout
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // Width
                LinearLayout.LayoutParams.WRAP_CONTENT  // Height
        );
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(0, (int) (10 * scale + 0.5f), 0, 0);

        // Setup Icon image
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(iconDrawableID);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        imgParams.width = (int) (16 * scale + 0.5f);
        imgParams.gravity = Gravity.CENTER_VERTICAL;
        imgParams.rightMargin = (int) (10 * scale + 0.5f);
        imgParams.leftMargin = (int) (10 * scale + 0.5f);
        imageView.setLayoutParams(imgParams);
        imageView.setAdjustViewBounds(true);
        linearLayout.addView(imageView);

        // Setup TextView
        TextView textView = new TextView(context);
        textView.setText(text);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textView.setLayoutParams(textParams);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setMinimumHeight((int) (16 * scale + 0.5f));
        linearLayout.addView(textView);
        Linkify.addLinks(textView, Linkify.ALL);
        return linearLayout;
    }

    public String getShareString(){
        return "Contact information for " + getCompany_name_cn() + ": \n" + formatToString();
    }
    public String getLine1Text() {
        return getCompany_name_cn();
    }
    public String getLine2Text() {
        return formatToString();
    }
}
