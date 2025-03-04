package com.vritra.fileexplorer;

import com.vritra.common.*;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.net.URLConnection;
import android.content.Context;
import android.content.Intent;
import android.content.ClipData;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.Manifest.permission;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.os.Environment;
import android.app.Activity;
import androidx.core.content.FileProvider;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.os.Build;
import android.net.Uri;


public class FileExplorer extends VritraPlugin {

    protected static final JSONObject mediaplayers=new JSONObject();
    protected static final JSONObject callbacks=new JSONObject();
    final int ref=new Random().nextInt(999);
    JSONObject props=null; 
    private Boolean multiple=true;

    @Override
    public boolean execute(String action,JSONArray args,CallbackContext callbackContext) throws JSONException {
        if(action.equals("pick")){
            JSONObject props=args.getJSONObject(0);
            this.pick(props,callbackContext);
            return true;
        }
        else if(action.equals("useFileType")){
            String path=args.getString(0);
            this.useFileType(path,callbackContext);
            return true;
        }
        else if(action.equals("canOpenFile")){
            String path=args.getString(0);
            this.canOpenFile(path,callbackContext);
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

    private void pick(JSONObject props,CallbackContext callback) throws JSONException {
        callbacks.put(Integer.toString(ref),callback);
        this.props=props;
        if(cordova.hasPermission(permission.READ_EXTERNAL_STORAGE)) this.openFilePicker(ref);
        else cordova.requestPermission(this,ref,permission.READ_EXTERNAL_STORAGE);
    }
    public void onRequestPermissionResult(int ref,String[] permissions,int[] results) throws JSONException {
        if((Build.VERSION.SDK_INT>Build.VERSION_CODES.Q)||(results[0]==PackageManager.PERMISSION_GRANTED)){
            this.openFilePicker(ref);
        }
        else{
            final CallbackContext callback=(CallbackContext)callbacks.remove(Integer.toString(ref));
            callback.error(new VritraError("FileExplorer Permission Denied"));
        }
    };
    public void openFilePicker(int ref){
        final Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        String type=props.optString("type","*/*");
        if(!type.contains("/")) type+="/*";
        intent.setType(type);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        final Boolean multiple=props.optBoolean("multiple",true);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,multiple); 
        this.multiple=multiple;
        cordova.startActivityForResult(this,Intent.createChooser(intent,"FileExplorer"),ref);
    }
    
    @Override
    public void onActivityResult(int ref,int resultCode,Intent intent){
        final String key=Integer.toString(ref);
        final CallbackContext callback=(CallbackContext)callbacks.opt(key);
        if(callback!=null){
            callbacks.remove(key);
            try{
                if((resultCode==cordova.getActivity().RESULT_OK)&&(intent!=null)){
                    if(multiple){
                        final ClipData data=intent.getClipData();
                        final JSONArray entries=new JSONArray();
                        if(data!=null){
                            final int length=data.getItemCount();
                            for(int i=0;i<length;i++){
                                final Uri uri=data.getItemAt(i).getUri();
                                final JSONObject props=FileExplorer.getFileProps(uri);
                                if(props!=null){
                                    entries.put(props);
                                }
                            }
                        }
                        else{
                            final Uri uri=intent.getData();
                            final JSONObject props=FileExplorer.getFileProps(uri);
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
                        final JSONObject props=FileExplorer.getFileProps(uri);
                        callback.success(props);
                    }
                }
                else throw new Exception("failed to get activity result");
            }
            catch(Exception exception){
                callback.error(new VritraError(exception));
            }
        }
    }

    private void useFileType(String path,CallbackContext callback){
        final String type=URLConnection.guessContentTypeFromName(path);
        callback.success(type);
    }

    private Intent canOpenFile(String path,CallbackContext callback){
        try{
            final Intent intent=new Intent(Intent.ACTION_VIEW);
            final File file=new File(parsePath(path));
            final Uri uri=FileProvider.getUriForFile(context,packageName+".provider",file);
            intent.setData(uri);
            final Activity activity=cordova.getActivity();
            final JSONObject result=new JSONObject();
            result.put("isOpenable",intent.resolveActivity(activity.getPackageManager())!=null);
            if(callback!=null) callback.success(result);
            return intent;
        }
        catch(Exception exception){
            if(callback!=null) callback.error(new VritraError(exception));
            return null;
        }
    }

    private void open(JSONObject props,CallbackContext callback){
        final String path=props.optString("path",null);
        try{
            if(path!=null){
                final Intent intent=this.canOpenFile(path,null);
                if(intent!=null){
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    cordova.getActivity().startActivity(intent);
                }
                else throw new Exception("No app to open file");
            }
            else throw new Exception("Path property is falsy");
        }
        catch(Exception exception){
            callback.error(new VritraError(exception));
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
                else throw new Exception("Path property is required");
            }
            else throw new Exception("Id property is required");
        }
        catch(Exception exception){
            callback.error(new VritraError(exception));
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
            else throw new Exception("Id property is required");
        }
        catch(Exception exception){
            callback.error(new VritraError(exception));
        }
    }

    static protected JSONObject getFileProps(Uri uri) throws Exception {
        JSONObject props=null;
        final Cursor cursor=context.getContentResolver().query(uri,null,null,null,null);
        File file=null;
        String filename=null;
        if(cursor.moveToFirst()){
            props=new JSONObject();
            int nameIndex=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex=cursor.getColumnIndex(OpenableColumns.SIZE);
            filename=cursor.getString(nameIndex);
            final Long filesize=cursor.getLong(sizeIndex);
            cursor.close();
            file=FileFinder.getUriFile(context,uri);
            if(file==null) file=FileFinder.getExternalStoragePublicFile(filename);
            if(file!=null){
                props.put("name",filename);
                props.put("size",filesize);
                FileExplorer.assignFileToJSONObject(file,props);
            }
        }
        else cursor.close();
        if(file==null){
            props=null;
            String message="Couldn't access "+(filename==null?"file":filename)+".";
            if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.R){
                message+="Please try selecting it from another location";
            }
            throw new Exception(message);
        }
        else return props;
    }


    static void assignFileToJSONObject(File file,JSONObject object) throws Exception {
        final String name=file.getName();
        object.put("name",name);
        object.put("path",file.getPath());
        object.put("location",file.getParent());
        object.put("fullpath","file://"+file.getAbsolutePath());
        object.put("lastModified",file.lastModified());
    }

    static String parsePath(String path){
        if(path.startsWith("file://")){
            path=path.replace("file://","");
        }
        return path;
    }
}
