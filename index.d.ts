declare const FilePicker:FilePicker;


interface FilePicker{
    /**
    * Shows the filepicker
    */
    show(options:{
        /**
        * if true, the user will be able to pick multiple files at once.
        * default: true.
        * @IOS
        * For ios<14, if multiple is true, the user needs to cancel the picker manually
        * after selecting files.
        */
        multiple:Boolean,
        /**
        * The mimeType of pickable files.
        * Use comma-seperated mimeTypes for multiple file types.
        * @note
        * For mimeTypes lacking the character "/", the string "/*" will be used as a suffix
        * @example 
        * "image" => "image/*" | "audio,video" => "audio/*,video/*"
        * By default the filpicker allows all types of files
        * @IOS
        * Can't pick images/videos and documents at the same time.
        * to pick images/videos all mimetypes should be image/video type,
        * otherwise a document picker is shown.
        * Type extensions are ignored, values are actually: "image"|"video"|"image,video".
        * All three values will have the same effect
        * because specifying media extensions is not yet supported. 
        * By default the filepicker will pick documents.
        */
        type:String,
        /**
         * 
         * @param entry if multiple is true, entry is an array else an object
         */
        onPick(entry:FilePickerEntry|FilePickerEntry[]):void,
    }):void;
    /**
    * Gets the mimeType of the path's target file.
    * The path does not need to point at an existing file.
    * It just uses the path file extension to get the corresponding mimeType.
    * @example
    * path:".jpeg" => type:"image/jpeg"
    */
    useFileType(path:String,callback:(type:String)=>void):void;
    /**
    * Opens the path's target file in the system default app.
    * @see This method may fail on simulators
    */
    open(options:{
        path:String,
        onFail(message:String):void,
    }):void;
    /**
    * Plays the specified audio file path on background.
    * @see Even if the webview is closed, The audio will keep playing 
    */
    playAudio(options:{
        /**
        * AudioPlayer id, used to stop audio. 
        */
        id:String,
        path:String,
        /**
        * A value in 0..1 used to play the audio file at a certain position. 
        */
        atRatio:Number,
        onPlay(options:{
            /**
            * The audio file duration in ms 
            */
            duration:Number,
        }):void,
        onFail(message:String):void,
    }):void,
    stopAudio(options:{
        id:String,
        onStop(options:{
            /**
            * The audio file stop position in ms 
            */
            timestamp:Number, 
        }):void,
    }):void,
}

interface FilePickerEntry{
    /**
    * File name with extension 
    */
    name:String,
    /**
    * File's absolute path 
    */
    path:String,
    /**
    * File's parent directory absolute path
    */
    location:String,
    /**
    * File's path property with file:/// as a prefix
    */
    fullpath:String,
    lastModified:Number,
    /**
    * File size in bytes 
    */
    size:Number,
}
