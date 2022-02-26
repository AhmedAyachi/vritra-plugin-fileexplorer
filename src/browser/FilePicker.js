

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
}

