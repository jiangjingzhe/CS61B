package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.CWD;
import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.join;
import static gitlet.Utils.writeObject;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    //filePath -> BlobId
    //filePath是绝对路径
    private Map<String, String> pathToBlobID = new HashMap<>();
    private List<String> parents;//parents commit id 是一个链表，如果List.size()==2说明是Merge之后的
    private Date currTime;
    private String id;
    private File commitSavaFile;
    private String timeStamp;

    public String getMessage() {
        return message;
    }

    public Map<String, String> getPathToBlobID() {
        return pathToBlobID;
    }

    public List<String> getParents() {
        return parents;
    }

    public Date getCurrTime() {
        return currTime;
    }

    public String getId() {
        return id;
    }

    public File getCommitSavaFile() {
        return commitSavaFile;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    /* TODO: fill in the rest of this class. */
    public Commit(String message, Map<String, String> pathToBlobID, List<String> parents){
        this.message = message;
        this.pathToBlobID = pathToBlobID;
        this.parents = parents;
        this.currTime = new Date();
        this.timeStamp = dateToTimeStamp(this.currTime);
        this.id = generateID();
        this.commitSavaFile = generateFile();
    }

    public Commit(){
        this.message = "initial commit";
        this.pathToBlobID = new HashMap<>();
        this.parents = new ArrayList<>();
        this.currTime = new Date(0);
        this.timeStamp = dateToTimeStamp(this.currTime);
        this.id = generateID();
        this.commitSavaFile = generateFile();
    }

    private File generateFile() {
        return join(OBJECT_DIR, id);
    }

    private String generateID() {
        return Utils.sha1(timeStamp, message, parents.toString(), pathToBlobID.toString());
    }

    private String dateToTimeStamp(Date currTime) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(currTime);
    }

    public void save(){
        writeObject(commitSavaFile, this);
    }

    public boolean exists(String fileName){
        return this.pathToBlobID.containsKey(fileName);
    }

    public static Set<String> getBlobIdSet(Commit commit){
        Set<String> blobIdSet = new HashSet<>();
        for(String id : commit.pathToBlobID.values()){
            blobIdSet.add(id);
        }
        return blobIdSet;
    }

}
