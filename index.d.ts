declare const FilePicker:FilePicker;


interface FilePicker{
    show(props:{
        multiple:Boolean,
        type:String,
        onPick(entry:Entry|Entry[]):void,
    }):void,
}

interface Entry{
    name:String,
    path:String,
    extension:String,
    location:String,
    absolutePath:String,
    canonicalPath:String,
    lastModified:Number,
    size:Number,
}
