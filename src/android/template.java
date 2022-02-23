package com.ahmedayachi.template;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.content.res.Resources;


public class Template extends CordovaPlugin{

    static protected Context context;
    static protected Resources resources;
    static protected String packagename;

    @Override
    public void initialize(CordovaInterface cordova,CordovaWebView webview){
        Template.context=cordova.getContext();
        Template.resources=Template.context.getResources();
        Template.packagename=Template.context.getPackageName();
    }
    @Override
    public boolean execute(String action,JSONArray args,CallbackContext callbackContext) throws JSONException{
        if(action.equals("coolAlert")) {
            String message=args.getString(0);
            this.coolAlert(message,callbackContext);
            return true;
        }
        return false;
    }

    private void coolAlert(String message,CallbackContext callbackContext){
        callbackContext.success(message);
    }

    static protected int getResourceId(String type,String name){
        return resources.getIdentifier(name,type,Template.packagename);
    }
    
}