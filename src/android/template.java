package com.ahmedayachi.template;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;


public class template extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals("showDateMethod")) {
            String message=args.getString(0);
            this.showDateMethod(message,callbackContext);
            return true;
        }
        return false;
    }

    private void showDateMethod(String message,CallbackContext callbackContext){
        callbackContext.success(message);
    }
    
}