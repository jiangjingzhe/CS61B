package gitlet;

import javax.swing.plaf.PanelUI;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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
     *      |     |--commit and blob
     *      |--refs
     *      |    |--heads
     *      |         |--master
     *      |--HEAD
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
            //removestage里没有
            if(!removeStage.isNewBlob(blob)){
                removeStage.delete(blob);
                removeStage.saveAddStage();
            }
            blob.save();
            if(addStage.isFilePathExists(blob.getFilePath())){
                addStage.delete(blob.getFilePath());
            }
            addStage.add(blob);
            addStage.saveAddStage();

        }
    }

    private static Commit readCurrCommit(){
        String currCommitId = readCurrCommitId();
        File CURR_COMMIT_FILE = join(OBJECT_DIR, currCommitId);
        return readObject(CURR_COMMIT_FILE, Commit.class);
    }

    private static String readCurrCommitId(){
        String currBranch = readCurrBranch();
        File HEADS_FILE = join(HEADS_DIR, currBranch);
        return readContentsAsString(HEADS_FILE);
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
}
