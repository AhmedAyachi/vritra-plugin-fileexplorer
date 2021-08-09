package org.apache.cordova.custom;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Custom extends CordovaPlugin{

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException{
        if (action.equals("coolAlert")) {
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
