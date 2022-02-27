declare const FilePicker:FilePicker;


interface FilePicker{
    show(props:{
        multiple:Boolean,
        type:String,
        onPick(entry:Entry|Entry[]):void,
    }):void,
}

interface Entry{
    path:String,
    name:String,
    type:String,
    absolutePath:String,
    canonicalPath:String,
    location:String,
    lastModified:Number,
    size:Number,
}
