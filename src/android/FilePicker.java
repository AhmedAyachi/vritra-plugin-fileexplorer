package com.ahmedayachi.filepicker;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.Context;
import android.content.Intent;
import android.content.ClipData;
import android.content.res.Resources;
import java.util.Random;
import android.net.Uri;
import java.io.File;
import android.widget.Toast;


public class FilePicker extends CordovaPlugin{

    static protected Context context;
    static protected Resources resources;
    static protected String packagename;
    protected static final JSONObject callbacks=new JSONObject();
    private Boolean multiple=true;

    @Override
    public void initialize(CordovaInterface cordova,CordovaWebView webview){
        FilePicker.context=cordova.getContext();
        FilePicker.resources=FilePicker.context.getResources();
        FilePicker.packagename=FilePicker.context.getPackageName();
    }
    @Override
    public boolean execute(String action,JSONArray args,CallbackContext callbackContext) throws JSONException{
        if(action.equals("show")) {
            JSONObject props=args.getJSONObject(0);
            this.show(props,callbackContext);
            return true;
        }
        return false;
    }

    private void show(JSONObject props,CallbackContext callback){
        final Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType(props.optString("type","*/*"));
        final Boolean multiple=props.optBoolean("multiple",true);
        this.multiple=multiple;
        if(multiple){
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);     
        }
        final int ref=new Random().nextInt(999);
        try{
            callbacks.put(Integer.toString(ref),callback);
        }
        catch(Exception exception){}
        this.cordova.startActivityForResult(this,Intent.createChooser(intent,"FilePicker"),ref);
    }

    @Override
    public void onActivityResult(int ref,int resultCode,Intent intent){
        final String key=Integer.toString(ref);
        final CallbackContext callback=(CallbackContext)callbacks.opt(key);
        if(callback!=null){
            callbacks.remove(key);
            if((resultCode==cordova.getActivity().RESULT_OK)&&(intent!=null)){
                try{
                    if(this.multiple){
                        final ClipData data=intent.getClipData();
                        final JSONArray entries=new JSONArray();
                        if(data!=null){
                            final int length=data.getItemCount();
                            for(int i=0;i<length;i++){
                                final Uri uri=data.getItemAt(i).getUri();
                                entries.put(this.getFileProps(uri));
                            }
                        }
                        else{
                            final Uri uri=intent.getData();
                            entries.put(this.getFileProps(uri));
                        }
                        callback.success(entries);
                    }
                    else{
                        final Uri uri=intent.getData();
                        callback.success(this.getFileProps(uri));
                    }
                }
                catch(Exception exception){};
            }
            callback.error("");
        }
    }

    static JSONObject getFileProps(Uri uri) throws Exception{
        //Toast.makeText(context,uri.toString(),Toast.LENGTH_SHORT).show();
        final JSONObject props=new JSONObject();
        final File file=new File(uri.getPath());
        props.put("path",file.getPath());
        props.put("name",file.getName());
        props.put("absolutePath",file.getAbsolutePath());
        props.put("lastModified",file.lastModified());
        props.put("canonicalPath",file.getCanonicalPath());
        props.put("location",file.getParent());
        props.put("size",file.getTotalSpace());
        return props;
    }

    static String parsePath(String path){
        String result="";
        String[] parts=path.split(":");
        result="file://"+parts[parts.length-1];
        return result;
    };

    static protected int getResourceId(String type,String name){
        return resources.getIdentifier(name,type,FilePicker.packagename);
    }
    
}