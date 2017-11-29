package com.techjany.automaticcallrecorder.utils;

import android.content.Context;

/**
 * Created by sandhya on 24-Aug-17.
 */

public class StringUtils {
    public static  String prepareContacts(Context ctx,String number){
            if(!number.isEmpty()){
                String preparednumbers=number.trim();
                preparednumbers=preparednumbers.replace(" ","");
                preparednumbers=preparednumbers.replace("(","");
                preparednumbers=preparednumbers.replace(")","");
                if(preparednumbers.contains("+")){
                    preparednumbers=preparednumbers.replace(preparednumbers.substring(0,3),""); //to remove country code
                }
                preparednumbers=preparednumbers.replace("-","");
                return preparednumbers;
            }else{
                return "";
            }


    }
}
