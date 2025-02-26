const exec=require("cordova/exec");


module.exports={
    pick:(props)=>{
        const {onPick}=props;
        exec(onPick,null,"FileExplorer","pick",[props]);
    },
    useFileType:(path,callback)=>{
        exec(callback,null,"FileExplorer","useFileType",[path]);
    },
    canOpenFile:(path,callback)=>{
        exec(callback,null,"FileExplorer","canOpenFile",[path]);
    },
    open:(props)=>{
        const {onFail}=props;
        exec(null,onFail,"FileExplorer","open",[props]);
    },
    playAudio:(props)=>{
        const {onPlay,onFail}=props;
        exec(onPlay,onFail,"FileExplorer","playAudio",[props]);
    },
    stopAudio:(props)=>{
        const {onStop}=props;
        exec(onStop,null,"FileExplorer","stopAudio",[props]);
    },
};
