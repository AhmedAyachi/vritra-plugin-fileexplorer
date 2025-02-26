declare const FileExplorer:FileExplorer;


interface FileExplorer {
    /**
    * Shows a filepicker
    */
    pick<multiple extends boolean=true>(options:{
        /**
        * if true, the user will be able to pick multiple files at once.
        * @default true
        * @iOS
        * For iOS<14, if multiple is true, the user needs to cancel the picker manually
        * after selecting files.
        */
        multiple:multiple,
        /**
        * The mimeType of pickable files.
        * Use comma-seperated mimeTypes for multiple file types.
        * @note
        * For mimeTypes lacking the character "/", the string "/*" will be used as a suffix
        * @example 
        * "image" => "image/*" | "audio,video" => "audio/*,video/*"
        * By default the filpicker allows all types of files
        * @iOS
        * Can't pick images/videos and documents at the same time.
        * to pick images/videos all mimetypes should be image/video type,
        * otherwise a document picker is shown.
        * Type extensions are ignored, values are actually: "image"|"video"|"image,video".
        * All three values will have the same effect
        * because specifying media extensions is not yet supported. 
        * By default the FileExplorer will pick documents.
        */
        type:string,
        /**
         * 
         * @param value if multiple is true, entry is an array else an object
         */
        onPick(value:multiple extends true?FileExplorerEntry[]:FileExplorerEntry):void,
    }):void;
    /**
    * Gets the mimeType of the path's target file.
    * The path does not need to point at an existing file.
    * It just uses the path file extension to get the corresponding mimeType.
    * @example
    * path:".jpeg" => type:"image/jpeg"
    */
    useFileType(path:string,callback:(type:string)=>void):void;
    /**
     * Checks if the device can open a file
     * @param path 
     * @param callback 
     * @param fallback 
     */
    canOpenFile(
        path:string,
        callback:(result:{isOpenable:boolean})=>void,
        fallback:(error:Error)=>void,
    ):void;
    /**
    * Opens a local file in the system default app.
    * @notice This method may fail on simulators
    */
    open(options:{
        path:string,
        onFail(error:Error):void,
    }):void;
    /**
    * Plays the specified local audio file path on background.
    * @notice Even if the webview is closed, The audio will keep playing 
    */
    playAudio(options:{
        /**
        * AudioPlayer id, used to stop audio. 
        */
        id:string,
        path:string,
        /**
        * A fraction used to specify from which start point should play the audio.
        * @example 
        * 0 => from the begining
        * 0.5 => from the middle
        * @default 0
        */
        atRatio:number,
        onPlay(options:{
            /**
            * The audio file duration in ms 
            */
            duration:number,
        }):void,
        onFail(error:Error):void,
    }):void,
    stopAudio(options:{
        id:string,
        onStop(options:{
            /**
            * The audio file stop position in ms 
            */
            timestamp:number, 
        }):void,
    }):void,
}

interface FileExplorerEntry {
    /**
    * File name with extension 
    */
    name:string,
    /**
    * File's absolute path 
    */
    path:string,
    /**
    * File's parent directory absolute path
    */
    location:string,
    /**
    * File's path property with file:/// as a prefix
    */
    fullpath:string,
    lastModified:number,
    /**
    * File size in bytes 
    */
    size:number,
}
