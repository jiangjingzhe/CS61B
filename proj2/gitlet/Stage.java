package gitlet;

import java.io.Serializable;
import java.util.*;
import java.io.File;

import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

public class Stage implements Serializable {
    //filePath -> BlobId
    //filePath是绝对路径
    private Map<String, String> pathToBlobID = new HashMap<>();

    public boolean isNewBlob(Blob blob){
        if(!pathToBlobID.containsValue(blob.getId()))
            return true;
        else
            return false;
    }

    public boolean isFilePathExists(String path){
        if(pathToBlobID.containsKey(path))
            return true;
        else
            return false;
    }

    public void delete(Blob blob){
        pathToBlobID.remove(blob.getFilePath());
    }

    public void delete(String path){
        pathToBlobID.remove(path);
    }

    public void add(Blob blob){
        pathToBlobID.put(blob.getFilePath(), blob.getId());
    }

    public void saveStage(File stageFile){
        writeObject(stageFile, this);
    }

    public void clear(){
        pathToBlobID.clear();
    }

    public List<Blob> getBlobList() {
        List<Blob> blobList = new ArrayList<>();
        for (String id : pathToBlobID.values()) {
            blobList.add(getBlobById(id));
        }
        return blobList;
    }

    public Set<String> getBlobIdSet(){
        Set<String> blobIdSet = new HashSet<>();
        for(String id : this.pathToBlobID.values()){
            blobIdSet.add(id);
        }
        return blobIdSet;
    }

    public static Blob getBlobById(String id) {
        return readObject(join(OBJECT_DIR, id), Blob.class);
    }

    public Map<String, String> getPathToBlobID() {
        return pathToBlobID;
    }

    public boolean exists(String fileName){
        return this.pathToBlobID.containsKey(fileName);
    }
    public Blob getBlobByPath(String path){
        return getBlobById(pathToBlobID.get(path));
    }

    public boolean isEmpty(){
        return pathToBlobID.isEmpty();
    }
}
