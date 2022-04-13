import Foundation;
import UniformTypeIdentifiers;
import AVFAudio;


class Filepicker:FilePickerPlugin,UIDocumentPickerDelegate,UIDocumentInteractionControllerDelegate {

    var multiple=true;
    var showCommand:CDVInvokedUrlCommand?=nil;
    static var audioplayers:[String:AVAudioPlayer]=[:];

    @objc(show:)
    func show(command:CDVInvokedUrlCommand){
        let argument=command.arguments[0] as? [AnyHashable:Any];
        if !(argument==nil){
            let props=argument!;
            self.showCommand=command;
            var pickerVC:UIDocumentPickerViewController;
            let type:String=props["type"] as? String ?? "*/*";
            if#available(iOS 14,*){
                pickerVC=UIDocumentPickerViewController(forOpeningContentTypes:Filepicker.getUTTypes(type),asCopy:false);
            }
            else {
                
                pickerVC=UIDocumentPickerViewController(documentTypes:Filepicker.getMimeTypes(type),in:UIDocumentPickerMode.open);
            }
            self.multiple=props["multiple"] as? Bool ?? true;
            pickerVC.allowsMultipleSelection=multiple;
            pickerVC.delegate=self;
            self.viewController.present(pickerVC,animated:true);
        }
    }

    func documentPicker(_ pickerVC:UIDocumentPickerViewController,didPickDocumentsAt:[URL]){
        var entries:[[String:Any?]]=[];
        didPickDocumentsAt.forEach({url in
        let path=url.path;
        var entry:[String:Any?]=[
                "name":url.lastPathComponent,
                "path":path,
                "location":path.replacingCharacters(in:(path.lastIndex(of:"/") ?? path.startIndex)...path.index(before:path.endIndex),with:""),
                "absolutePath":"file://\(url.absoluteURL.path)",
                "canonicalPath":url.absoluteURL.path,
                "lastModified":0,
                "size":0,
            ];
            let resource=try? url.resourceValues(forKeys:[.fileSizeKey,.contentModificationDateKey]);
            if !(resource==nil){
                entry["lastModified"]=(resource!.contentModificationDate?.timeIntervalSince1970 ?? 0)*1000;
                entry["size"]=resource!.fileSize;
            }
            entries.append(entry);
        });
        multiple ? success(showCommand!,entries):success(showCommand!,entries[0]);
    }
        
    func documentPickerWasCancelled(_ pickerVC:UIDocumentPickerViewController){
        
    }

    @objc(useFileType:)
    func useFileType(command:CDVInvokedUrlCommand){
        let argument=command.arguments[0] as? String;
        if !(argument==nil){
            let path:NSString=argument as! NSString;
            if#available(iOS 14,*){
                let mimeType=UTType(filenameExtension:path.pathExtension)?.preferredMIMEType ?? "";
                success(command,mimeType);
            }
            else{
                success(command,path.mimeType());
            }
        }
    }

    @objc(open:)
    func open(command:CDVInvokedUrlCommand){
        let argument=command.arguments[0] as? String;
        if !(argument==nil){
            let path:String=argument!;
            let url:URL?=path.contains("://") ? URL(string:path):URL(fileURLWithPath:path);
            if !(url==nil){
                let file=url!;
                let app=UIApplication.shared;
                if(app.canOpenURL(file)){
                    if#available(iOS 10.0,*){
                        app.open(file);
                    }
                    else{
                        app.openURL(file);
                    }
                    /* let controller=UIDocumentInteractionController(url:file);
                    controller.delegate=self;
                    controller.presentOpenInMenu(
                        from:self.viewController.view.frame,
                        in:self.viewController.view,
                        animated:true
                    ); */
                    /* let controller=UIDocumentInteractionController(url:url);
                    controller.delegate=self;
                    controller.presentPreview(animated:true); */
                }
                else{
                    let alert=UIAlertController(title:"",message:"No app to open file",preferredStyle:.actionSheet);
                    DispatchQueue.main.asyncAfter(deadline:DispatchTime.now()+2){
                        alert.dismiss(animated:true);
                    }
                    error(command,"Can't open url");
                }
            }
        }
    }

    func documentInteractionControllerViewControllerForPreview(_ controller:UIDocumentInteractionController)->UIViewController{
        return self.viewController;
    }

    @objc(playAudio:)
    func playAudio(command:CDVInvokedUrlCommand){
        let argument=command.arguments[0] as? [AnyHashable:Any];
        do{
            if !(argument==nil){
                let props=argument!;
                let id=props["id"] as? String ?? "";
                if !(id.isEmpty){
                    let path=props["path"] as? String ?? "";
                    if(!path.isEmpty){
                        let url:URL?=path.contains("://") ? URL(string:path):URL(fileURLWithPath:path);
                        if !(url==nil){
                            let file=url!;
                            let player:AVAudioPlayer=try!AVAudioPlayer(contentsOf:file);
                            let duration:TimeInterval=player.duration;
                            let atRatio=props["atRatio"] as? Double ?? 0;
                            player.currentTime=atRatio*duration;
                            if(player.play()){
                                Filepicker.audioplayers[id]=player;
                                let params:[String:Any]=[
                                    "duration":duration*1000,
                                ];
                                success(command,params);
                            }
                            else{
                                throw "Unable to play audio";
                            }
                        }
                    }
                    else{
                        throw "Path property is required";
                    }
                }
                else{
                    throw "Id property is required";
                }
            }
        }
        catch{
            self.error(command,error.localizedDescription);
        }
    }

    @objc(stopAudio:)
    func stopAudio(command:CDVInvokedUrlCommand){
        let argument=command.arguments[0] as? [AnyHashable:Any];
        if !(argument==nil){
            let props=argument!;
            let id=props["id"] as? String ?? "";
            if(!id.isEmpty){
                let value:AVAudioPlayer?=Filepicker.audioplayers[id];
                if !(value==nil){
                    let player=value!;
                    player.stop();
                    if(!player.isPlaying){
                        let params:[String:Any]=[
                            "timestamp":player.currentTime*1000,
                        ];
                        Filepicker.audioplayers.removeValue(forKey:id);
                        success(command,params);
                    }
                }
            }
        }
    }

    @available(iOS 14.0,*)
    static func getUTTypes(_ type:String)->[UTType]{
        var types:[UTType]=[];
        if(type=="*/*"){
            Set(mimeTypes.values).forEach(pushMimeType);
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
}
