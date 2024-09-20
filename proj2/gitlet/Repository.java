package gitlet;

import javax.swing.plaf.PanelUI;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */
    /*
     *   .gitlet
     *      |--objects
     *      |     |--commit and blob (文件名是commitId和blobId)
     *      |--refs
     *      |    |--heads
     *      |         |--master ->(记录当前分支最新的commitId)
     *      |--HEAD ->(记录currBranch)
     *      |--stage
     */
    // .gitlet directory
    public static final File OBJECT_DIR = join(GITLET_DIR, "object");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File ADDSTAGE_FILE = join(GITLET_DIR, "add_stage");
    public static final File REMOVESTAGE_FILE = join(GITLET_DIR, "remove_stage");

    public static Commit currCommit;
    public static Stage addStage;
    public static Stage removeStage;
    public static String currBranch;

    public static void init(){
        if(GITLET_DIR.exists()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdirs();
        OBJECT_DIR.mkdirs();
        REFS_DIR.mkdirs();
        HEADS_DIR.mkdirs();
        initCommit();
        initHEAD();
        initHeads();
    }
    private static void initCommit(){
        Commit initCommit = new Commit();
        currCommit = initCommit;
        initCommit.save();
    }
    private static void initHEAD(){
        writeContents(HEAD_FILE, "master");
    }
    private static void initHeads(){
        File HEADS_FILE = join(HEADS_DIR, "master");
        writeContents(HEADS_FILE, currCommit.getId());
    }
    public static void checkIfInitialized(){
        if(!GITLET_DIR.exists()){
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
    public static void add(String fileName){
        File file = getFileFromCWD(fileName);
        if(!file.exists()){
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob blob = new Blob(file);
        storeBlob(blob);
    }
    //判断是绝对路径还是相对路径
    private static File getFileFromCWD(String fileName) {
        return Paths.get(fileName).isAbsolute() ? new File(fileName) : join(CWD, fileName);
    }

    private static void storeBlob(Blob blob){
        currCommit = readCurrCommit();
        addStage = readStage(ADDSTAGE_FILE);
        removeStage = readStage(REMOVESTAGE_FILE);
        //如果currcommit里面没有,并且addstage里面没有
        if(!currCommit.getPathToBlobID().containsValue(blob.getId())
                && addStage.isNewBlob(blob)){
            //removestage里有
            if(!removeStage.isNewBlob(blob)){
                removeStage.delete(blob);
                removeStage.saveStage(REMOVESTAGE_FILE);
            }
            blob.save();
            if(addStage.isFilePathExists(blob.getFilePath())){
                addStage.delete(blob.getFilePath());
            }
            addStage.add(blob);
            addStage.saveStage(ADDSTAGE_FILE);

        }
    }

    private static Commit readCurrCommit(){
        String currCommitId = readCurrCommitId();
        File CURR_COMMIT_FILE = join(OBJECT_DIR, currCommitId);
        return readObject(CURR_COMMIT_FILE, Commit.class);
    }

    private static String readCurrCommitId(){
        String currBranch = readCurrBranch();
        return readContentsAsString(join(HEADS_DIR, currBranch));
    }

    private static String readCurrBranch(){
        return readContentsAsString(HEAD_FILE);
    }

    private static Stage readStage(File stageFile){
        if(!stageFile.exists()){
            return new Stage();
        }
        return readObject(stageFile, Stage.class);
    }

    public static void commit(String arg) {
        if (arg.equals("")){
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = newCommit(arg);
        currCommit = newCommit;
        saveNewCommit(newCommit);
    }

    private static void saveNewCommit(Commit newCommit) {
        newCommit.save();
        addStage.clear();
        removeStage.clear();
        addStage.saveStage(ADDSTAGE_FILE);
        removeStage.saveStage(REMOVESTAGE_FILE);
        String currBranch = readCurrBranch();
        writeContents(join(HEADS_DIR, currBranch), currCommit.getId());
    }

    private static Commit newCommit(String arg) {
        currCommit = readCurrCommit();
        Map<String, String> addBlobMap = findBlobMap(ADDSTAGE_FILE);
        Map<String, String> removeBlobMap = findBlobMap(REMOVESTAGE_FILE);
        Map<String, String> currCommitMap = currCommit.getPathToBlobID();
        Map<String, String> commitBlobMap = generateNewMap(currCommitMap, addBlobMap, removeBlobMap);
        List<String> parents = new ArrayList<>();
        parents.add(currCommit.getId());
        return new Commit(arg, commitBlobMap, parents);
    }

    private static Map<String, String> generateNewMap(Map<String, String> currCommitMap, Map<String, String> addBlobMap, Map<String, String> removeBlobMap) {
        if(addBlobMap.isEmpty() && removeBlobMap.isEmpty()){
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        // 1. 加入 addBlobMap 的所有键值对到 currCommitMap 中
        for (Map.Entry<String, String> entry : addBlobMap.entrySet()) {
            currCommitMap.put(entry.getKey(), entry.getValue());
        }

        // 2. 从 currCommitMap 中移除 removeBlobMap 的所有键
        for (String key : removeBlobMap.keySet()) {
            currCommitMap.remove(key);
        }

        return currCommitMap;
    }

    private static Map<String, String> findBlobMap(File stageFile) {
        return readStage(stageFile).getPathToBlobID();
    }
}
