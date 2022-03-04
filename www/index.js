const exec=require("cordova/exec");


module.exports={
    show:(props)=>{
        const {onPick}=props;
        exec(onPick,null,"FilePicker","show",[props]);
    },
    useFileType:(path,callback)=>{
        exec(callback,null,"FilePicker","useFileType",[path]);
    },
    open:(props)=>{
        const {onFail}=props;
        exec(null,onFail,"FilePicker","open",[props]);
    },
    playAudio:(props)=>{
        const {onPlay,onFail}=props;
        exec(onPlay,onFail,"FilePicker","playAudio",[props]);
    },
    stopAudio:(props)=>{
        const {onStop}=props;
        exec(onStop,null,"FilePicker","stopAudio",[props]);
    },
};
