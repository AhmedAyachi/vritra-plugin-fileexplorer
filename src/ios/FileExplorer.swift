import Foundation;
import UniformTypeIdentifiers;
import AVFAudio;
import PhotosUI;
import QuickLook;


class FileExplorer:VritraPlugin,UIDocumentPickerDelegate,UIDocumentInteractionControllerDelegate,UINavigationControllerDelegate,UIImagePickerControllerDelegate,PHPickerViewControllerDelegate {

    var props:[AnyHashable:Any]=[:];
    lazy var entries:[[String:Any?]]=[];
    var multiple=true;
    var pickCommand:CDVInvokedUrlCommand?=nil;
    static var audioplayers:[String:AVAudioPlayer]=[:];
    var previewurl:URL?=nil;

    @objc(pick:)
    func pick(command:CDVInvokedUrlCommand){
        if let props=command.arguments[0] as? [AnyHashable:Any] {
            DispatchQueue.main.async(execute:{[self] in
                self.pickCommand=command;
                self.props=props;
                self.multiple=props["multiple"] as? Bool ?? true;
                let type=props["type"] as? String ?? "*/*";
                let isMediaMode=FileExplorer.isMediaMode(type);
                isMediaMode ? self.presentMediaPicker():self.presentDocumentsPicker();
            });
        }
    }
    
    func presentDocumentsPicker(){
        var pickerVC:UIDocumentPickerViewController;
        let type:String=props["type"] as? String ?? "*/*";
        if#available(iOS 14,*){
            pickerVC=UIDocumentPickerViewController(forOpeningContentTypes:FileExplorer.getUTTypes(type),asCopy:false);
        }
        else {
            pickerVC=UIDocumentPickerViewController(documentTypes:FileExplorer.getMimeTypes(type),in:UIDocumentPickerMode.open);
        }
        pickerVC.allowsMultipleSelection=multiple;
        pickerVC.delegate=self;
        self.viewController.present(pickerVC,animated:true);
    } 

    func documentPicker(_ pickerVC:UIDocumentPickerViewController,didPickDocumentsAt:[URL]){
        let included=didPickDocumentsAt.filter({url in
            let accessable=url.startAccessingSecurityScopedResource();
            if(accessable){
                entries.append(FileExplorer.getEntryFromURL(url));
            };
            return accessable;
        });
        DispatchQueue.main.asyncAfter(deadline:.now()+30,execute:{
            included.forEach({url in
                if(url.startAccessingSecurityScopedResource()){
                    url.stopAccessingSecurityScopedResource();
                }
            });
        });
        self.onPick();
    }
        
    func documentPickerWasCancelled(_ pickerVC:UIDocumentPickerViewController){
 
    }

    func presentMediaPicker(){
        if#available(iOS 14,*){
            /* PHPhotoLibrary.requestAuthorization(for:.readWrite,handler:{[self] permission in
                if(permission==PHAuthorizationStatus.authorized||permission==PHAuthorizationStatus.limited){
                    
                }
            }); */
            var configs=PHPickerConfiguration(photoLibrary:PHPhotoLibrary.shared());
            configs.selectionLimit=multiple ? 0:1;
            let pickerVC=PHPickerViewController(configuration:configs);
            pickerVC.delegate=self;
            self.viewController.present(pickerVC,animated:true);
        }
        else{
            let pickerVC=UIImagePickerController();
            pickerVC.allowsEditing=false;
            pickerVC.mediaTypes=["public.image","public.movie"];
            pickerVC.sourceType=UIImagePickerController.SourceType.photoLibrary;
            pickerVC.delegate=self;
            self.viewController.present(pickerVC,animated:true);
        }
    }

    @available(iOS 14, *)
    func picker(_ pickerVC:PHPickerViewController,didFinishPicking medias:[PHPickerResult]){
        if(!medias.isEmpty){
            var i=0;
            let fileManager=FileManager.default;
            medias.forEach({media in
                let provider=media.itemProvider;
                provider.loadFileRepresentation(forTypeIdentifier:UTType.item.identifier,completionHandler:{[self] url,error in
                    i+=1;
                    guard let url=url else { return };
                    let path="\(fileManager.temporaryDirectory.path)/\(url.lastPathComponent)";
                    let destination=URL(fileURLWithPath:path);
                    var fileCreated:Bool=false;
                    do {
                        if(fileManager.fileExists(atPath:destination.path)){
                            _=try fileManager.replaceItemAt(destination,withItemAt:url);
                        }
                        else {
                            try fileManager.moveItem(at:url,to:destination);
                        }
                        fileCreated=true;
                    }
                    catch {
                        fileCreated=false;
                    }
                    if(fileCreated){
                        let entry=FileExplorer.getEntryFromURL(destination);
                        self.entries.append(entry);
                    }
                    if(i==medias.count){
                        self.onPick();
                    }
                });
            });
        }
        pickerVC.dismiss(animated:true);
    }

    func imagePickerController(_ pickerVC:UIImagePickerController,didFinishPickingMediaWithInfo mediainfo:[UIImagePickerController.InfoKey:Any]){
        if let value=mediainfo[.imageURL] ?? mediainfo[.mediaURL],let url=value as? URL {
            let entry=FileExplorer.getEntryFromURL(url);
            entries.append(entry);
        };
        if(!multiple){
            self.imagePickerControllerDidCancel(pickerVC);
        }
    }

    func imagePickerControllerDidCancel(_ pickerVC: UIImagePickerController){
        pickerVC.dismiss(animated:true);
        if(!entries.isEmpty){
            self.onPick();
        }
    }

    func onPick(){
        if(multiple){
            success(pickCommand!,entries);
        }
        else{
            success(pickCommand!,entries[0] as [AnyHashable:Any]);
        }
        self.reset();
    }

    private func reset(){
        self.entries=[];
        self.multiple=true;
    }

    @objc(useFileType:)
    func useFileType(command:CDVInvokedUrlCommand){
        if let path=command.arguments[0] as? NSString {
            if#available(iOS 14,*){
                let mimeType=UTType(filenameExtension:path.pathExtension)?.preferredMIMEType ?? "";
                success(command,mimeType);
            }
            else{
                success(command,path.mimeType());
            }
        }
    }

    @objc(canOpenFile:)
    func canOpenFile(command:CDVInvokedUrlCommand){
        guard var path=command.arguments[0] as? String else { return };
        if(path.hasPrefix("file:")){
            path=path.replacingOccurrences(of:"file:",with:"");
        }
        while(path.contains("//")){
            path=path.replacingOccurrences(of:"//",with:"/");
        }
        if(FileManager.default.fileExists(atPath:path)){
            let url=URL(fileURLWithPath:path);
            self.success(command,["isOpenable":UIApplication.shared.canOpenURL(url)]);
        }
        else {
            self.error(command,FileExplorer.Error("file does not exist").toObject());
        }
    }
    
    @objc(open:)
    func open(command:CDVInvokedUrlCommand){
        DispatchQueue.main.async(execute:{[self] in
            guard
                let props=command.arguments[0] as? [AnyHashable:Any],
                var path=props["path"] as? String
            else { return };
            if(path.hasPrefix("file:")){
                path=path.replacingOccurrences(of:"file:",with:"");
            }
            while(path.contains("//")){
                path=path.replacingOccurrences(of:"//",with:"/");
            }
            let url=URL(fileURLWithPath:path);
            do{
                if(FileManager.default.fileExists(atPath:url.path)){
                    _=url.startAccessingSecurityScopedResource()
                    self.previewurl=url;
                    let opener=UIDocumentInteractionController(url:url);
                    opener.delegate=self;
                    if(!opener.presentPreview(animated:true)){
                        url.stopAccessingSecurityScopedResource();
                        self.previewurl=nil;
                        throw FileExplorer.Error("Can't open \(url.lastPathComponent)");
                    }
                }
                else{
                    throw FileExplorer.Error("Can't open \(url.lastPathComponent)");
                }
            }
            catch{
                let alert=UIAlertController(title:"",message:error.localizedDescription,preferredStyle:.actionSheet);
                DispatchQueue.main.asyncAfter(deadline:DispatchTime.now()+2){
                    alert.dismiss(animated:true);
                }
                self.viewController.present(alert,animated:true);
                self.error(command,"Can't open url");
            }
        });
    }

    func documentInteractionControllerViewControllerForPreview(_ controller:UIDocumentInteractionController)->UIViewController{
        return self.viewController;
    }

    func documentInteractionControllerDidEndPreview(_ controller:UIDocumentInteractionController){
        if let url=self.previewurl {
            url.stopAccessingSecurityScopedResource();
        }
    }

    @objc(playAudio:)
    func playAudio(command:CDVInvokedUrlCommand){
        DispatchQueue.main.async(execute:{[self] in
            do{
                if let props=command.arguments[0] as? [AnyHashable:Any] {
                    let id=props["id"] as? String ?? "";
                    if !(id.isEmpty){
                        var path=props["path"] as? String ?? "";
                        if(!path.isEmpty){
                            if(path.starts(with:"file:///")){path.removeFirst(8)};
                            let url=URL(fileURLWithPath:path);
                            let player:AVAudioPlayer=try AVAudioPlayer(contentsOf:url);
                            let duration:TimeInterval=player.duration;
                            let atRatio=props["atRatio"] as? Double ?? 0;
                            player.currentTime=atRatio*duration;
                            if(player.play()){
                                FileExplorer.audioplayers[id]=player;
                                let params:[String:Any]=[
                                    "duration":duration*1000,
                                ];
                                success(command,params);
                            }
                            else{
                                throw FileExplorer.Error("Unable to play audio");
                            }
                        }
                    }
                    else{
                        throw FileExplorer.Error("Id property is required");
                    }
                }
            }
            catch{
                self.error(command,error.localizedDescription);
            }
        });
    }

    @objc(stopAudio:)
    func stopAudio(command:CDVInvokedUrlCommand){
        DispatchQueue.main.async(execute:{[self] in
            if let props=command.arguments[0] as? [AnyHashable:Any] {
                let id=props["id"] as? String ?? "";
                if(!id.isEmpty),let player=FileExplorer.audioplayers[id]{
                    player.stop();
                    if(!player.isPlaying){
                        let params:[String:Any]=[
                            "timestamp":player.currentTime*1000,
                        ];
                        FileExplorer.audioplayers.removeValue(forKey:id);
                        success(command,params);
                    }
                }
            }
        });
    }
    
    static func isMediaMode(_ token:String)->Bool{
        var isMediaMode=false;
        if(token != "*/*"){
            let types=FileExplorer.getMimeTypes(token);
            isMediaMode=types.allSatisfy({type in type.hasPrefix("image")||type.hasPrefix("video")});
        }
        return isMediaMode;
    }

    static func getEntryFromURL(_ url:URL)->[String:Any?]{
        let path=url.path;
        var entry:[String:Any?]=[
            "name":url.lastPathComponent,
            "path":path,
            "location":path.replacingCharacters(in:(path.lastIndex(of:"/") ?? path.startIndex)...path.index(before:path.endIndex),with:""),
            "fullpath":"file://\(url.absoluteURL.path)",
            "lastModified":0,
            "size":0,
        ];
        if let resource=try? url.resourceValues(forKeys:[.fileSizeKey,.contentModificationDateKey]){
            entry["lastModified"]=(resource.contentModificationDate?.timeIntervalSince1970 ?? 0)*1000;
            entry["size"]=resource.fileSize;
        }
        return entry;
    }

    @available(iOS 14.0,*)
    static func getUTTypes(_ type:String)->[UTType]{
        var types:[UTType]=[];
        if(type=="*/*"){
            //Set(mimeTypes.values).forEach(pushMimeType);
            types.append(UTType.item);
        }
        else{
            type.split(separator:",").forEach({pushMimeType(String($0))});
        }
        func pushMimeType(_ token:String){
            let mime=token.trimmingCharacters(in:[" "]);
            if(mime.hasSuffix("/*")||(!mime.contains("/"))){
                let base=mime.contains("/") ? mime.replacingOccurrences(of:"/*",with:""):mime;
                Set(mimeTypes.values).filter({value in value.hasPrefix(base)}).forEach(pushMimeType);
            }
            else{
                let uttype=UTType(mimeType:mime);
                if !(uttype==nil){
                    types.append(uttype!);
                }
            }
        }
        return types;
    }

    static func getMimeTypes(_ token:String)->[String]{
        var types:[String]=[];
        if(token=="*/*"){
            types=mimeTypes.values.map({value in String(value)});
        }
        else{
            token.split(separator:",").forEach({item in
                let mime=String(item).trimmingCharacters(in:[" "]);
                if(mime.hasSuffix("/*")||(!mime.contains("/"))){
                    let base=mime.contains("/") ? mime.replacingOccurrences(of:"/*",with:""):mime;
                    Set(mimeTypes.values).filter({value in value.hasPrefix(base)}).forEach({types.append($0)});
                }
                else{
                    types.append(mime);
                }
            });
        }
        return types;
    }

    /* static func getMediaTypes(_ token:String)->[String]{
        var types=FileExplorer.getMediaTypes(token);
        let length=types.count;
        for i in 0..<length {
            var type=types[i];
            type="public.\(type.split(separator:"/")[0])";
            types[i]=type;
        }
        return types;
    } */
}
