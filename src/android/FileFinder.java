package com.vritra.fileexplorer;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import java.io.File;
import java.util.List;
import java.util.Iterator;


public class FileFinder {

    public static final List<String> publicDirNames=List.of(
        Environment.DIRECTORY_ALARMS,
        Environment.DIRECTORY_AUDIOBOOKS,
        Environment.DIRECTORY_DCIM,
        Environment.DIRECTORY_DOCUMENTS,
        Environment.DIRECTORY_DOWNLOADS,
        Environment.DIRECTORY_MOVIES,
        Environment.DIRECTORY_MUSIC,
        Environment.DIRECTORY_NOTIFICATIONS,
        Environment.DIRECTORY_PICTURES,
        Environment.DIRECTORY_PODCASTS,
        Environment.DIRECTORY_RINGTONES,
        Environment.DIRECTORY_SCREENSHOTS
    );
    public static File getExternalStoragePublicFile(String name){
        File file=null;
        final Iterator<String> iterator=FileFinder.publicDirNames.iterator();
        while((file==null)&&iterator.hasNext()){
            final File location=Environment.getExternalStoragePublicDirectory(iterator.next());
            file=new File(location,name);
            if(!file.exists()) file=null;
        }
        return file;
    }

    public static File getUriFile(Context context,Uri uri){
        File file=null;
        try{
            final String realPath=FileFinder.getUriPath(context,uri);
            if((realPath!=null)&&(realPath.length()>0)){
                file=new File(realPath);
                if(!file.exists()) file=null;
            }
        }
        catch(Exception $){
            file=null;
        }
        return file;
    }

    public static String getUriPath(Context context,String uriString){
        return FileFinder.getUriPath(context,Uri.parse(uriString));
    }
    @SuppressWarnings("deprecation")
    public static String getUriPath(Context context,Uri uri){
        String realPath=null;
        final int sdkNumber=Build.VERSION.SDK_INT;
        if(sdkNumber<11) realPath=FileFinder.getPathFromURIBelowAPI11(context,uri);
        else if(sdkNumber<19) realPath=FileFinder.getPathFromURIFromAPI11To18(context,uri);
        else realPath=FileFinder.getPathFromURIAboveAPI18(context,uri);
        return realPath;
    }

    @SuppressLint("NewApi")
    public static String getPathFromURIAboveAPI18(final Context context,final Uri uri){
        if(DocumentsContract.isDocumentUri(context,uri)){//DocumentProvider
            final String docId=DocumentsContract.getDocumentId(uri);
            if(isDownloadsDocument(uri)){//DownloadsProvider
                final Uri contentUri=ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                return getDataColumn(context,contentUri,null,null);
            }
            else{
                final String[] parts=docId.split(":");
                final String type=parts[0];
                if(isExternalStorageDocument(uri)){
                    if(type.equalsIgnoreCase("primary")) return Environment.getExternalStorageDirectory()+"/"+parts[1];
                    else return null;
                }
                else if(isMediaDocument(uri)){//MediaProvider
                    Uri contentUri=null;
                    switch(type){
                        case "image":contentUri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;break;
                        case "video":contentUri=MediaStore.Video.Media.EXTERNAL_CONTENT_URI;break;
                        case "audio":contentUri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;break;
                        case "document":contentUri=MediaStore.Files.getContentUri("external");break;
                        default:break;
                    }
                    final String selection="_id=?";
                    final String[] selectionArgs=new String[]{parts[1]};
                    return getDataColumn(context,contentUri,selection,selectionArgs);
                }
                else return null;
            }
        }
        else{
            //MediaStore (and general)
            final String scheme=uri.getScheme().toLowerCase();
            switch(scheme){
                case "content"://Return the remote address
                    if(isGooglePhotosUri(uri)) return uri.getLastPathSegment();
                    else return getDataColumn(context,uri,null,null);
                case "file": return uri.getPath();
                default: return null;
            }
        }
    }

    @SuppressLint("NewApi")
    public static String getPathFromURIFromAPI11To18(Context context,Uri contentUri){
        final String[] projections={MediaStore.Images.Media.DATA};
        String result=null;
        try{
            CursorLoader cursorLoader=new CursorLoader(context,contentUri,projections,null,null,null);
            Cursor cursor=cursorLoader.loadInBackground();
            if(cursor!=null){
                final int columnIndex=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                result=cursor.getString(columnIndex);
            }
        }
        catch(Exception $){
            result=null;
        }
        return result;
    }

    public static String getPathFromURIBelowAPI11(Context context, Uri contentUri){
        final String[] projections={MediaStore.Images.Media.DATA};
        String result=null;
        try{
            Cursor cursor=context.getContentResolver().query(contentUri,projections,null,null,null);
            final int columnIndex=cursor.getColumnIndexOrThrow(projections[0]);
            cursor.moveToFirst();
            result=cursor.getString(columnIndex);
        } 
        catch(Exception $){
            result=null;
        }
        return result;
    }

    public static String getDataColumn(Context context,Uri uri,String selection,String[] selectionArgs){
        final String column="_data";
        final String[] projections={column};
        Cursor cursor=null;
        String dataColumn=null;
        try{
            cursor=context.getContentResolver().query(uri,projections,selection,selectionArgs,null);
            if((cursor!=null)&&cursor.moveToFirst()){
                dataColumn=cursor.getString(cursor.getColumnIndexOrThrow(column));
            }
        } 
        finally{
            if(cursor!=null) cursor.close();
        }
        return dataColumn;
    }

    public static boolean isExternalStorageDocument(Uri uri){
        return uri.getAuthority().equals("com.android.externalstorage.documents");
    }

    public static boolean isDownloadsDocument(Uri uri){
        return uri.getAuthority().equals("com.android.providers.downloads.documents");
    }

    public static boolean isMediaDocument(Uri uri){
        return uri.getAuthority().equals("com.android.providers.media.documents");
    }

    public static boolean isGooglePhotosUri(Uri uri){
        return uri.getAuthority().equals("com.google.android.apps.photos.content");
    }
}
