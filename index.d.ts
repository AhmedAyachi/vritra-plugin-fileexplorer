declare const FilePicker:FilePicker;


interface FilePicker{
    show(props:{
        multiple:Boolean,
        type:String,
        onPick(entry:Entry):void,
        onPick(entries:Entry[]):void,
    }):void,
}

interface Entry{
    path:String,
    absolutePath:String,
    canonicalPath:String,
    location:String,
    name:String,
    lastModified:Number,
    size:Number,
}
