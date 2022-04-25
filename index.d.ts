declare const FilePicker:FilePicker;


interface FilePicker{
    /**
    * Shows the filepicker
    */
    show(props:{
        /**
        * if true, the user will be able to pick multiple files at once
        * default: true
        */
        multiple:Boolean,
        /**
        * The mimeType of pickable files.
        * You can specify multiple mimeTypes by separating them by a comma (,).
        * @note
        * For mimeTypes lacking the character "/", the string "/*" will be used as a suffix
        * @example 
        * "image" => "image/*" | "audio,video" => "audio/*,video/*"
        * By default the filpicker allows all types of files
        * @IOS
        * Can't pick images and documents at the same time.
        * to pick images just pass "image" as type.
        * By default the filepicker will pick documents
        */
        type:String,
        onPick(entry:PickerEntry|PickerEntry[]):void,
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
    * @note
    * This method may fail on simulators
    */
    open(props:{
        path:String,
        onFail(message:String):void,
    }):void;
    /**
    * Plays the specified audio file path on background.
    * Even if the webview is closed, The audio will keep playing 
    */
    playAudio(params:{
        /**
        * AudioPlayer id, used to stop audio. 
        */
        id:String,
        path:String,
        /**
        * A value in 0...1 used to play the audio file at a certain position. 
        */
        atRatio:Number,
        onPlay(params:{
            /**
            * The audio file duration in ms 
            */
            duration:Number,
        }):void,
        onFail(message:String):void,
    }):void,
    stopAudio(params:{
        id:String,
        onStop(params:{
            /**
            * The audio file stop position in ms 
            */
            timestamp:Number, 
        }):void,
    }):void,
}

interface PickerEntry{
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
    * File's absolute path with file:/// as a prefix
    */
    absolutePath:String,
    canonicalPath:String,
    lastModified:Number,
    /**
    * File size in bytes 
    */
    size:Number,
}
