package com.ahmedayachi.template;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;


public class Template extends CordovaPlugin {

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
    
}