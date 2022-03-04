declare const FilePicker:FilePicker;


interface FilePicker{
    show(props:{
        multiple:Boolean,
        type:String,
        onPick(entry:PickerEntry|PickerEntry[]):void,
    }):void;
    useFileType(path:String,callback:(type:String)=>void):void;
    open(props:{
        path:String,
        onFail(error:String):void,
    }):void;
    playAudio(params:{
        id:String|Number,
        path:String,
        atRatio:Number,
        onPlay(params:{
            duration:Number,
        }):void,
        onFail(message:String):void,
    }):void,
    stopAudio(params:{
        id:String|Number,
        onStop(params:{
            timestamp:Number, 
        }):void,
    }):void,
}

interface PickerEntry{
    name:String,
    path:String,
    location:String,
    absolutePath:String,
    canonicalPath:String,
    lastModified:Number,
    size:Number,
}
