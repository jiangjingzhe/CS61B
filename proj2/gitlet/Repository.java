package gitlet;

import afu.org.checkerframework.checker.oigj.qual.O;

import javax.swing.plaf.PanelUI;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static gitlet.Commit.getBlobIdSet;
import static gitlet.Commit.getBlobIdSet;
import static gitlet.Stage.getBlobById;
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
        //创建blob的时候用的绝对路径。
        Blob blob = new Blob(file);
        storeBlob(blob);
    }
    //判断是绝对路径还是相对路径,返回绝对路径。
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
        List<String> newParents = new ArrayList<>();
        newParents.add(currCommit.getId());
        return new Commit(arg, commitBlobMap, newParents);
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

    public static void rm(String fileName) {
        File file = getFileFromCWD(fileName);
        currCommit = readCurrCommit();
        addStage = readStage(ADDSTAGE_FILE);
        removeStage = readStage(REMOVESTAGE_FILE);
        String filePath = file.getPath();
        if(addStage.exists(filePath)){
            addStage.delete(filePath);
            addStage.saveStage(ADDSTAGE_FILE);
        } else if (currCommit.exists(fileName)) {
            String removeBlobId = currCommit.getPathToBlobID().get(fileName);
            Blob removeBlob = getBlobById(removeBlobId);
            removeStage.add(removeBlob);
            removeStage.saveStage(REMOVESTAGE_FILE);
            file.delete();
        }else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    public static void log() {
        Commit curr = readCurrCommit();
        while (!curr.getParents().isEmpty()){
            if(curr.getParents().size() == 2)
                printMergeCommit(curr);
            else 
                printCommit(curr);
            curr = readCommitById(curr.getParents().get(0));
        }
        printCommit(curr);
    }

    private static Commit readCommitById(String commitId) {
        if(commitId.length() == 40){
            File COMMIT_FILE = join(OBJECT_DIR, commitId);
            if(COMMIT_FILE.exists())
                return readObject(COMMIT_FILE, Commit.class);
            else
                return null;
        }else {
            List<String> objectId = plainFilenamesIn(OBJECT_DIR);
            for(String id : objectId){
                if(commitId.equals(id.substring(0, commitId.length())));
                    return readObject(join(OBJECT_DIR, id), Commit.class);
            }
            return null;
        }
    }

    private static void printMergeCommit(Commit commit) {
        System.out.println("===");
        System.out.println("commit" + commit.getId());
        String parent1 = commit.getParents().get(0);
        String parent2 = commit.getParents().get(1);
        System.out.println("Merge: " + parent1.substring(0, 7) + " " + parent2.substring(0, 7));
        System.out.println("Date:" + commit.getTimeStamp());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    private static void printCommit(Commit commit){
        System.out.println("===");
        System.out.println("commit" + commit.getId());
        System.out.println("Date:" + commit.getTimeStamp());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    public void checkoutBranch(String branchName) {
        /* * checkout [branch name] */
        currBranch = readCurrBranch();
        if(branchName.equals(currBranch)){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        List<String> allBranch = plainFilenamesIn(HEADS_DIR);
        if(!allBranch.contains(branchName)){
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        currCommit = readCurrCommit();
        Commit newCommit = readCommitByBranchName(branchName);
        changeCommit(newCommit);
        changeBranch(branchName);
    }

    private void changeBranch(String branchName) {
    }

    private void changeCommit(Commit newCommit) {
        Set<String> currBlobId = getBlobIdSet(currCommit);
        Set<String> newBlobId = getBlobIdSet(newCommit);

        // 1. 找到 currBlobId 独有的元素
        Set<String> onlyInCurr = new HashSet<>(currBlobId);
        onlyInCurr.removeAll(newBlobId);  // 移除 newBlobSet 中的元素，剩下的就是 currBlobId 独有的

        // 2. 找到 newBlobId 独有的元素
        Set<String> onlyInNew = new HashSet<>(newBlobId);
        onlyInNew.removeAll(currBlobId);  // 移除 currBlobSet 中的元素，剩下的就是 newBlobId 独有的

//        // 3. 找到两者共有的元素
//        Set<String> inBoth = new HashSet<>(currBlobId);
//        inBoth.retainAll(newBlobId);  // 仅保留同时出现在两者中的元素

        writeFile(onlyInNew, newCommit);
        deleteFile(onlyInCurr);
        clearAllStage();
    }

    private void clearAllStage() {
        addStage = readStage(ADDSTAGE_FILE);
        removeStage = readStage(REMOVESTAGE_FILE);
        addStage.clear();
        removeStage.clear();
        addStage.saveStage(ADDSTAGE_FILE);
        removeStage.saveStage(REMOVESTAGE_FILE);
    }

    private static void deleteFile(Set<String> onlyInCurr){
        for(String id : onlyInCurr){
            Blob blob = getBlobById(id);
            restrictedDelete(blob.getFileName());
        }
    }

    private static void writeFile(Set<String> onlyInNew, Commit newCommit){
        for(String id : onlyInNew){
            Blob blob = getBlobById(id);
            if(blob.getFileName().exists()){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for(String id : onlyInNew){
            Blob blob = getBlobById(id);
            writeBlobToCWD(blob);
        }
    }

    private static void writeBlobToCWD(Blob blob) {
        writeContents(blob.getFileName(), new String(blob.getBytes(), StandardCharsets.UTF_8));
    }

    private static Commit readCommitByBranchName(String branchName) {
        String newCommitId = readContentsAsString(join(HEADS_DIR, branchName));
        return readCommitById(newCommitId);
    }

    public static void checkout(String fileName) {
        /* * checkout -- [file name] */
        currCommit = readCurrCommit();
        checkout(currCommit, fileName);
    }

    public static void checkout(String commitId, String fileName) {
        /* * checkout [commit id] -- [file name] */
        Commit commit = readCommitById(commitId);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        checkout(commit, fileName);
    }

    private static void checkout(Commit commit, String fileName){
        File file = join(CWD, fileName);
        if(commit.getPathToBlobID().containsKey(file.getPath())){
            String blobId = commit.getPathToBlobID().get(file.getPath());
            Blob blob = getBlobById(blobId);
            writeBlobToCWD(blob);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }
}
