

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
            const extension=path.substring(path.indexOf("."));
            type=extensions.find(({values})=>values.includes(extension.toLowerCase())).type;
            if(type){
                type+=`/${extension}`;
            }
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

const extensions=[
    {type:"image",values:["jpg","jpeg","png","gif"]},
    {type:"audio",values:["mp3","wav","aac","mpeg"]},
    {type:"video",values:["mp4","mov","flv"]},
];

