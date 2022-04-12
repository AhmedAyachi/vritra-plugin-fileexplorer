

module.exports={
    show:(props)=>{
        const filepicker=document.createElement("input"),{multiple=true}=props;
        filepicker.type="file";
        filepicker.multiple=multiple;
        filepicker.onchange=()=>{
            const {onPick}=props,{files}=filepicker;
            onPick&&onPick(multiple?[...files]:files[0]);
        }
        filepicker.click();
    },
    useFileType:(path,callback)=>{
        let type=null;
        if((typeof(path)==="string")&&path.length){
            const extension=path.substring(path.lastIndexOf(".")+1);
            type=extensions[extension];
        }
        callback&&callback(type);
    },
    open:(props)=>{
        const {file}=props;
        window.open(file,"_blank","fullscreen=true");
    },
    playAudio:(props)=>{
        //const {onPlay,onFail}=props;
        console.log("browser not supported");
    },
    stopAudio:(props)=>{
        console.log("browser not supported");
    },
}

const extensions={
    "html":"text/html",
    "htm":"text/html",
    "shtml":"text/html",
    "css":"text/css",
    "xml":"text/xml",
    "gif":"image/gif",
    "jpeg":"image/jpeg",
    "jpg":"image/jpeg",
    "js":"application/javascript",
    "atom":"application/atom+xml",
    "rss":"application/rss+xml",
    "mml":"text/mathml",
    "txt":"text/plain",
    "jad":"text/vnd.sun.j2me.app-descriptor",
    "wml":"text/vnd.wap.wml",
    "htc":"text/x-component",
    "png":"image/png",
    "tif":"image/tiff",
    "tiff":"image/tiff",
    "wbmp":"image/vnd.wap.wbmp",
    "ico":"image/x-icon",
    "jng":"image/x-jng",
    "bmp":"image/x-ms-bmp",
    "svg":"image/svg+xml",
    "svgz":"image/svg+xml",
    "webp":"image/webp",
    "ex_":"application/octet-stream",
    "dl_":"application/octet-stream",
    "saf":"application/octet-stream",
    "ini":"application/octet-stream",
    "dst":"application/octet-stream",
    "woff":"application/font-woff",
    "jar":"application/java-archive",
    "war":"application/java-archive",
    "ear":"application/java-archive",
    "json":"application/json",
    "hqx":"application/mac-binhex40",
    "doc":"application/msword",
    "pdf":"application/pdf",
    "ps":"application/postscript",
    "eps":"application/postscript",
    "ai":"application/postscript",
    "rtf":"application/rtf",
    "m3u8":"application/vnd.apple.mpegurl",
    "xls":"application/vnd.ms-excel",
    "eot":"application/vnd.ms-fontobject",
    "ppt":"application/vnd.ms-powerpoint",
    "wmlc":"application/vnd.wap.wmlc",
    "kml":"application/vnd.google-earth.kml+xml",
    "kmz":"application/vnd.google-earth.kmz",
    "7z":"application/x-7z-compressed",
    "cco":"application/x-cocoa",
    "jardiff":"application/x-java-archive-diff",
    "jnlp":"application/x-java-jnlp-file",
    "run":"application/x-makeself",
    "pl":"application/x-perl",
    "pm":"application/x-perl",
    "prc":"application/x-pilot",
    "pdb":"application/x-pilot",
    "rar":"application/x-rar-compressed",
    "rpm":"application/x-redhat-package-manager",
    "sea":"application/x-sea",
    "swf":"application/x-shockwave-flash",
    "sit":"application/x-stuffit",
    "tcl":"application/x-tcl",
    "tk":"application/x-tcl",
    "der":"application/x-x509-ca-cert",
    "pem":"application/x-x509-ca-cert",
    "crt":"application/x-x509-ca-cert",
    "xpi":"application/x-xpinstall",
    "xhtml":"application/xhtml+xml",
    "xspf":"application/xspf+xml",
    "zip":"application/zip",
    "epub":"application/epub+zip",
    "docx":"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "xlsx":"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "pptx":"application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "mid":"audio/midi",
    "midi":"audio/midi",
    "kar":"audio/midi",
    "mp3":"audio/mpeg",
    "ogg":"audio/ogg",
    "m4a":"audio/x-m4a",
    "ra":"audio/x-realaudio",
    "3gpp":"video/3gpp",
    "3gp":"video/3gpp",
    "ts":"video/mp2t",
    "mp4":"video/mp4",
    "mpeg":"video/mpeg",
    "mpg":"video/mpeg",
    "mov":"video/quicktime",
    "webm":"video/webm",
    "flv":"video/x-flv",
    "m4v":"video/x-m4v",
    "mng":"video/x-mng",
    "asx":"video/x-ms-asf",
    "asf":"video/x-ms-asf",
    "wmv":"video/x-ms-wmv",
    "avi":"video/x-msvideo",
};

