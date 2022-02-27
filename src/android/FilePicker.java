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
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.widget.Toast;


public class FilePicker extends CordovaPlugin{

    static protected Context context;
    static protected Resources resources;
    static protected String packagename;
    protected static final JSONObject callbacks=new JSONObject();
    final int ref=new Random().nextInt(999);
    JSONObject props=null; 
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

    private void show(JSONObject props,CallbackContext callback) throws JSONException{
        callbacks.put(Integer.toString(ref),callback);
        this.props=props;
        if(this.cordova.hasPermission(permission.READ_EXTERNAL_STORAGE)){
            final int[] results={PackageManager.PERMISSION_GRANTED};
            this.onRequestPermissionsResult(ref,null,results);
        }
        else{
            this.cordova.requestPermission(this,ref,permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int ref,String[] permissions,int[] results) throws JSONException{
        if(results[0]==PackageManager.PERMISSION_GRANTED){
            final Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType(props.optString("type","*/*"));
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            final Boolean multiple=props.optBoolean("multiple",true);
            this.multiple=multiple;
            if(multiple){
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);     
            }
            this.cordova.startActivityForResult(this,Intent.createChooser(intent,"FilePicker"),ref);
        }
        else{
            callbacks.remove(Integer.toString(ref));
            Toast.makeText(context,"FilePicker Permission Denied",Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    public void onActivityResult(int ref,int resultCode,Intent intent){
        final String key=Integer.toString(ref);
        final CallbackContext callback=(CallbackContext)callbacks.opt(key);
        if(callback!=null){
            callbacks.remove(key);
            if((resultCode==cordova.getActivity().RESULT_OK)&&(intent!=null)){
                try{
                    if(multiple){
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

    protected JSONObject getFileProps(Uri uri) throws Exception{
        final JSONObject props=new JSONObject();
        final ContentResolver resolver=context.getContentResolver();
        final File file=new File(uri.getPath());
        props.put("type",resolver.getType(uri));
        props.put("absolutePath",file.getAbsolutePath());
        props.put("lastModified",file.lastModified());
        props.put("canonicalPath",file.getCanonicalPath());
        props.put("location",file.getParent());
        final Cursor cursor=resolver.query(uri,null,null,null,null);
        int nameIndex=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex=cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        final String name=cursor.getString(nameIndex);
        props.put("name",name);
        props.put("size",cursor.getLong(sizeIndex));

        final String realPath=FileHelper.getRealPath(uri,this.cordova);
        if((realPath!=null)&&(realPath.length()>0)){
            props.put("path","file://"+realPath);
        }
        else{
            props.put("path",uri);
            Toast.makeText(context,"Can't access "+name+". Please select it from another location",Toast.LENGTH_SHORT).show();
        }
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