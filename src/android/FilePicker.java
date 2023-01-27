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
import java.io.IOException;
import java.net.URLConnection;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.os.Environment;
import android.widget.Toast;
import android.app.Activity;
import androidx.core.content.FileProvider;
import android.media.MediaPlayer;
import android.media.AudioManager;


public class FilePicker extends CordovaPlugin{

    static protected Context context;
    static protected Resources resources;
    static protected String packagename;
    static protected CordovaInterface cordova;
    protected static final JSONObject mediaplayers=new JSONObject();
    protected static final JSONObject callbacks=new JSONObject();
    final int ref=new Random().nextInt(999);
    JSONObject props=null; 
    private Boolean multiple=true;

    @Override
    public void initialize(CordovaInterface cordova,CordovaWebView webview){
        FilePicker.cordova=cordova;
        FilePicker.context=cordova.getContext();
        FilePicker.resources=FilePicker.context.getResources();
        FilePicker.packagename=FilePicker.context.getPackageName();
    }
    @Override
    public boolean execute(String action,JSONArray args,CallbackContext callbackContext) throws JSONException{
        if(action.equals("show")){
            JSONObject props=args.getJSONObject(0);
            this.show(props,callbackContext);
            return true;
        }
        else if(action.equals("useFileType")){
            String path=args.getString(0);
            this.useFileType(path,callbackContext);
            return true;
        }
        else if(action.equals("open")){
            JSONObject props=args.getJSONObject(0);
            this.open(props,callbackContext);
            return true;
        } 
        else if(action.equals("playAudio")){
            JSONObject props=args.getJSONObject(0);
            this.playAudio(props,callbackContext);
            return true;
        }
        else if(action.equals("stopAudio")){
            JSONObject props=args.getJSONObject(0);
            this.stopAudio(props,callbackContext);
            return true;
        }
        return false;
    }

    private void show(JSONObject props,CallbackContext callback) throws JSONException{
        callbacks.put(Integer.toString(ref),callback);
        this.props=props;
        if(cordova.hasPermission(permission.READ_EXTERNAL_STORAGE)){
            final int[] results={PackageManager.PERMISSION_GRANTED};
            this.onRequestPermissionsResult(ref,null,results);
        }
        else{
            cordova.requestPermission(this,ref,permission.READ_EXTERNAL_STORAGE);
        }
    }

    public void onRequestPermissionsResult(int ref,String[] permissions,int[] results) throws JSONException{
        if(results[0]==PackageManager.PERMISSION_GRANTED){
            final Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(props.optString("type","*/*"));
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            final Boolean multiple=props.optBoolean("multiple",true);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,multiple); 
            this.multiple=multiple;
            cordova.startActivityForResult(this,Intent.createChooser(intent,"FilePicker"),ref);
        }
        else{
            callbacks.remove(Integer.toString(ref));
            Toast.makeText(context,"FilePicker Permission Denied",Toast.LENGTH_SHORT).show();
        }
    }

    public void onRequestPermissionResult(int ref,String[] permissions,int[] results) throws JSONException{
        this.onRequestPermissionsResult(ref,permissions,results);
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
                                final JSONObject props=FilePicker.getFileProps(uri);
                                if(props!=null){
                                    entries.put(props);
                                }
                            }
                        }
                        else{
                            final Uri uri=intent.getData();
                            final JSONObject props=FilePicker.getFileProps(uri);
                            if(props!=null){
                                entries.put(props);
                            }
                        }
                        if(entries.length()>0){
                            callback.success(entries);
                        }
                    }
                    else{
                        final Uri uri=intent.getData();
                        final JSONObject props=FilePicker.getFileProps(uri);
                        callback.success(props);
                    }
                }
                catch(Exception exception){}
            }
            callback.error("");
        }
    }

    private void useFileType(String path,CallbackContext callback){
        final String type=URLConnection.guessContentTypeFromName(path);
        callback.success(type);
    }

    private void open(JSONObject props,CallbackContext callback){
        final String path=props.optString("path",null);
        try{
            if(path!=null){
                final Intent intent=new Intent(Intent.ACTION_VIEW);
                final File file=new File(parsePath(path));
                final Uri uri=FileProvider.getUriForFile(context,packagename+".provider",file);
                intent.setData(uri);
                final Activity activity=cordova.getActivity();
                if(intent.resolveActivity(activity.getPackageManager())==null){
                    Toast.makeText(context,"No app to open file",Toast.LENGTH_SHORT).show();
                }
                else{
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    activity.startActivity(intent);
                }
            }
            else{
                throw new Exception("Path property is falsy");
            }
        }
        catch(Exception exception){
            callback.error(exception.getMessage());
        }
    }

    private void playAudio(JSONObject props,CallbackContext callback){
        try{
            final String id=props.optString("id",null);
            if(id!=null){
                final String path=props.optString("path",null);
                if(path!=null){
                    final MediaPlayer mediaplayer=new MediaPlayer();
                    mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaplayer.setDataSource(path);
                    mediaplayer.prepare();
                    final int duration=mediaplayer.getDuration();
                    final double atRatio=props.optDouble("atRatio",0);
                    if((atRatio>0)&&(atRatio<1)){
                        mediaplayer.seekTo((long)(atRatio*duration),MediaPlayer.SEEK_CLOSEST);
                    }
                    mediaplayer.start();
                    mediaplayers.put(id,mediaplayer);
                    final JSONObject params=new JSONObject();
                    params.put("duration",duration);
                    callback.success(params);
                }
                else{
                    throw new Exception("Path property is required");
                }
            }
            else{
                throw new Exception("Id property is required");
            }
        }
        catch(Exception exception){
            callback.error(exception.getMessage());
        }
    }

    private void stopAudio(JSONObject props,CallbackContext callback){
        try{
            final String id=props.optString("id",null);
            if(id!=null){
                final MediaPlayer mediaplayer=(MediaPlayer)mediaplayers.opt(id);
                if(mediaplayer!=null){
                    final JSONObject params=new JSONObject();
                    params.put("timestamp",mediaplayer.getTimestamp().getAnchorMediaTimeUs()/1000);
                    mediaplayer.stop();
                    mediaplayer.release();
                    mediaplayers.remove(id);
                    callback.success(params);
                }
            }
            else{
                throw new Exception("Id property is required");
            }
        }
        catch(Exception exception){
            callback.error(exception.getMessage());
        }
    }

    static protected JSONObject getFileProps(Uri uri) throws Exception{
        JSONObject props=new JSONObject();
        try{
            final ContentResolver resolver=context.getContentResolver();
            final Cursor cursor=resolver.query(uri,null,null,null,null);
            int nameIndex=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex=cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            props.put("name",cursor.getString(nameIndex));
            props.put("size",cursor.getLong(sizeIndex));
            final String realPath=FileHelper.getRealPath(uri,cordova);
            if((realPath!=null)&&(realPath.length()>0)){
                final File file=new File(realPath);
                FilePicker.setFileProps(file,props);
            }
            else{
                throw new Exception();
            }
        }
        catch(Exception exception){
            final String name=props.optString("name");
            final File location=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            final File file=new File(location,name);
            if(file.exists()){
                FilePicker.setFileProps(file,props);
            }
            else{
                props=null;
                Toast.makeText(context,"Can't access "+name+". Please try selecting it from another location",Toast.LENGTH_SHORT).show();
            }
        }
        
        return props;
    }


    static void setFileProps(File file,JSONObject props) throws Exception{
        final String name=file.getName();
        props.put("name",name);
        props.put("path",file.getPath());
        props.put("location",file.getParent());
        props.put("fullpath","file://"+file.getAbsolutePath());
        props.put("lastModified",file.lastModified());
    }

    /* static Boolean isMediaFile(String path){
        Boolean isMedia=false;
        final String type=URLConnection.guessContentTypeFromName(path);
        final String[] mediaTypes={"image","audio","video"};
        int i=0,length=mediaTypes.length;
        while((!isMedia)&&(i<length)){
            isMedia=type.startsWith(mediaTypes[i]);
            i++;
        }
        return isMedia;
    } */

    static String parsePath(String path){
        if(path.startsWith("file://")){
            path=path.replace("file://","");
        }
        return path;
    }

    static protected int getResourceId(String type,String name){
        return resources.getIdentifier(name,type,FilePicker.packagename);
    }
}
