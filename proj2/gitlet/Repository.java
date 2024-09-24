package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

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
     *      |--REMOTES_DIR
     */
    // .gitlet directory
    public static File CWD;
    /** The .gitlet directory. */
    public static File GITLET_DIR;
    public static File OBJECT_DIR;
    public static File REFS_DIR;
    public static File HEADS_DIR;
    public static File HEAD_FILE;
    public static File ADDSTAGE_FILE;
    public static File REMOVESTAGE_FILE;
    public static File REMOTES_DIR;
    public static File CONFIG;

    public static Commit currCommit;
    public static Stage addStage;
    public static Stage removeStage;
    public static String currBranch;

    public static void configDIR(File WORK_DIR){
        CWD = WORK_DIR;
        GITLET_DIR = join(CWD, ".gitlet");
        OBJECT_DIR = join(GITLET_DIR, "object");
        REFS_DIR = join(GITLET_DIR, "refs");
        HEADS_DIR = join(REFS_DIR, "heads");
        HEAD_FILE = join(GITLET_DIR, "HEAD");
        ADDSTAGE_FILE = join(GITLET_DIR, "add_stage");
        REMOVESTAGE_FILE = join(GITLET_DIR, "remove_stage");
        REMOTES_DIR = join(GITLET_DIR, "remotes");
        CONFIG = join(GITLET_DIR, "config");
    }

    public static void init(){
        if(GITLET_DIR.exists()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdirs();
        OBJECT_DIR.mkdirs();
        REFS_DIR.mkdirs();
        HEADS_DIR.mkdirs();
        REMOTES_DIR.mkdirs();
        initCommit();
        initHEAD();
        initHeads();
        writeContents(CONFIG, "");
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
        currCommit = readCurrCommit();
        addStage = readStage(ADDSTAGE_FILE);
        removeStage = readStage(REMOVESTAGE_FILE);
        if(removeStage.exists(file.getPath())){
            removeStage.delete(blob);
            removeStage.saveStage(REMOVESTAGE_FILE);
        }
        //如果文件版本和commit中的不一样
        if(!currCommit.getPathToBlobID().containsValue(blob.getId())){
            blob.save();
            if(addStage.isFilePathExists(blob.getFilePath())){
                addStage.delete(blob.getFilePath());
            }
            addStage.add(blob);
            addStage.saveStage(ADDSTAGE_FILE);
        }

    }
    //判断是绝对路径还是相对路径,返回绝对路径。
    private static File getFileFromCWD(String fileName) {
        return Paths.get(fileName).isAbsolute() ? new File(fileName) : join(CWD, fileName);
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
        clearAllStage();
        String currBranch = readCurrBranch();
        writeContents(join(HEADS_DIR, currBranch), newCommit.getId());
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
        } else if (currCommit.exists(filePath)) {
            String removeBlobId = currCommit.getPathToBlobID().get(filePath);
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
                if(commitId.equals(id.substring(0, commitId.length())))
                    return readObject(join(OBJECT_DIR, id), Commit.class);
            }
            return null;
        }
    }

    private static void printMergeCommit(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getId());
        String parent1 = commit.getParents().get(0);
        String parent2 = commit.getParents().get(1);
        System.out.println("Merge: " + parent1.substring(0, 7) + " " + parent2.substring(0, 7));
        System.out.println("Date: " + commit.getTimeStamp());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    private static void printCommit(Commit commit){
        System.out.println("===");
        System.out.println("commit " + commit.getId());
        System.out.println("Date: " + commit.getTimeStamp());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    public static void checkoutBranch(String branchName) {
        File branchFile = getBranchFile(branchName);
        /* * checkout [branch name] */
        currBranch = readCurrBranch();
        if(branchName.equals(currBranch)){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        if(!branchFile.exists()){
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        currCommit = readCurrCommit();
        Commit newCommit = readCommitByBranchName(branchName);
        changeCommit(newCommit);
        currCommit = newCommit;
        changeBranch(branchName);
    }

    private static void changeBranch(String branchName) {
        writeContents(HEAD_FILE, branchName);
    }

    private static void changeCommit(Commit newCommit) {
        Set<String> currBlobId = getBlobIdSet(currCommit);
        Set<String> newBlobId = getBlobIdSet(newCommit);

        // 1. 找到 currBlobId 独有的元素
        Set<String> onlyInCurr = new HashSet<>(currBlobId);
        onlyInCurr.removeAll(newBlobId);  // 移除 newBlobSet 中的元素，剩下的就是 currBlobId 独有的

        // 2. 找到 newBlobId 独有的元素
        Set<String> onlyInNew = new HashSet<>(newBlobId);
        onlyInNew.removeAll(currBlobId);  // 移除 currBlobSet 中的元素，剩下的就是 newBlobId 独有的

        Set<String> currBlobName = new HashSet<>();
        for(String id : onlyInCurr){
            Blob blob = getBlobById(id);
            currBlobName.add(blob.getFileName().getName());
        }
        for(String id : onlyInNew){
            Blob blob = getBlobById(id);
            if(!currBlobName.contains(blob.getFileName().getName()) &&
                    blob.getFileName().exists()){
                Blob b = new Blob(blob.getFileName());
                if(!b.getId().equals(id)){
                    unTreackedError();
                }
            }
        }
        deleteFile(onlyInCurr);
        writeFile(onlyInNew);
        clearAllStage();
    }

    private static void clearAllStage() {
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

    private static void writeFile(Set<String> onlyInNew){
        for(String id : onlyInNew){
            Blob blob = getBlobById(id);
            writeBlobToCWD(blob);
        }
    }

    private static void writeBlobToCWD(Blob blob) {
        writeContents(blob.getFileName(), new String(blob.getBytes(), StandardCharsets.UTF_8));
    }

    private static Commit readCommitByBranchName(String branchName) {
        File branchFile = getBranchFile(branchName);
        String newCommitId = readContentsAsString(branchFile);
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
        if(commit.exists(file.getPath())){
            String blobId = commit.getPathToBlobID().get(file.getPath());
            Blob blob = getBlobById(blobId);
            writeBlobToCWD(blob);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    public static void status() {
        printBranches();
        addStage = readStage(ADDSTAGE_FILE);
        removeStage = readStage(REMOVESTAGE_FILE);
        Set<String> addStagePath = addStage.getPathToBlobID().keySet();
        Set<String> removeStagePath = removeStage.getPathToBlobID().keySet();

        System.out.println("=== Staged Files ===");
        printStageFile(addStagePath);
        System.out.println("=== Removed Files ===");
        printStageFile(removeStagePath);

        currCommit = readCurrCommit();
        printUnstagedFile();
        printUntrackedFile();
    }

    private static void printUntrackedFile() {
        System.out.println("=== Untracked Files ===");
        List<String> untracked = getUntrackedFiles();
        for(String name : untracked){
            System.out.println(name);
        }
        System.out.println();
    }
    private static List<String> getUntrackedFiles(){
        addStage = readStage(ADDSTAGE_FILE);
        removeStage = readStage(REMOVESTAGE_FILE);
        List<String> untracked = new ArrayList<>();
        List<String> allFiles = plainFilenamesIn(CWD);
        for(String fileName : allFiles){
            File currFile = getFileFromCWD(fileName);
            if(!addStage.exists(currFile.getPath())){
                if(!currCommit.exists(currFile.getPath())){
                    untracked.add(currFile.getName());
                }else {
                    if (removeStage.exists(currFile.getPath())){
                        untracked.add(currFile.getName());
                    }
                }
            }
        }
        Collections.sort(untracked);
        return untracked;
    }

    private static void printUnstagedFile() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> unstaged = new ArrayList<>();
        List<String> allFiles = plainFilenamesIn(CWD);
        for(String fileName : allFiles){
            File currFile = getFileFromCWD(fileName);
            Blob currBlob = new Blob(currFile);
            if(currCommit.exists(currFile.getPath()) &&
                !currCommit.getPathToBlobID().containsValue(currBlob.getId()) &&
                    ((!addStage.getPathToBlobID().containsValue(currBlob.getId()) && addStage.exists(currFile.getPath()) )  ||
                !addStage.exists(currFile.getPath()))){
                unstaged.add(fileName + " (modified)");
            }
        }
        for(String addFile : addStage.getPathToBlobID().keySet()){
            File file = new File(addFile);
            if(!allFiles.contains(file.getName())){
                unstaged.add(file.getName() + " (deleted)");
            }
        }
        for(String commitFile : currCommit.getPathToBlobID().keySet()){
            File file = new File(commitFile);
            if(!allFiles.contains(file.getName()) && !removeStage.exists(commitFile)){
                if(!unstaged.contains(file.getName() + " (deleted)")){
                    unstaged.add(file.getName() + " (deleted)");
                }
            }
        }
        Collections.sort(unstaged);
        for(String name : unstaged){
            System.out.println(name);
        }
        System.out.println();
    }

    private static void printStageFile(Set<String> filePath) {
        List<String> fileName = new ArrayList<>();
        for(String path : filePath){
            File file = new File(path);
            fileName.add(file.getName());
        }
        Collections.sort(fileName);
        for(String name : fileName){
            System.out.println(name);
        }
        System.out.println();
    }

    private static void printBranches() {
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        currBranch = readCurrBranch();
        System.out.println("=== Branches ===");
        System.out.println("*"  + currBranch);
        for(String branch : branchList){
            if(branch.equals(currBranch))
                continue;
            else
                System.out.println(branch);
        }
        System.out.println();
    }

    public static void global_log() {
        List<String> commitList = plainFilenamesIn(OBJECT_DIR);
        Commit commit;
        for (String id : commitList) {
            try {
                commit = readCommitById(id);
                if (commit.getParents().size() == 2) {
                    printMergeCommit(commit);
                } else {
                    printCommit(commit);
                }
            } catch (Exception ignore) {
            }
        }
    }

    public static void find(String findMessage) {
        List<String> commitList = plainFilenamesIn(OBJECT_DIR);
        List<String> idList = new ArrayList<String>();
        Commit commit;
        for (String id : commitList) {
            try {
                commit = readCommitById(id);
                if (findMessage.equals(commit.getMessage())) {
                    idList.add(id);
                }
            } catch (Exception ignore) {
            }
        }
        for(String id : idList){
            System.out.println(id);
        }
        if(idList.isEmpty()){
            System.out.println("Found no commit with that message.");
        }
    }

    public static void branch(String branchName) {
        List<String> allBranch = plainFilenamesIn(HEADS_DIR);
        if(allBranch.contains(branchName)){
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File newBranchFile = getBranchFile(branchName);
        currCommit = readCurrCommit();
        writeContents(newBranchFile, currCommit.getId());
    }

    public static void rm_branch(String branchName) {
        currBranch = readCurrBranch();
        if (currBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        checkIfBranchExists(branchName);
        File fileName = getBranchFile(branchName);
        fileName.delete();
    }

    private static void checkIfBranchExists(String branchName) {
        File branchFile = getBranchFile(branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    public static void reset(String commitId) {
        Commit newCommit = readCommitById(commitId);
        if (newCommit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        currCommit = readCurrCommit();
        changeCommit(newCommit);
        currBranch = readCurrBranch();
        File branchFile = join(HEADS_DIR, currBranch);
        writeContents(branchFile, commitId);
    }

    public static void merge(String mergeBranch) {
        currBranch = readCurrBranch();
        currCommit = readCurrCommit();
        addStage = readStage(ADDSTAGE_FILE);
        removeStage = readStage(REMOVESTAGE_FILE);
        if(!addStage.isEmpty() || !removeStage.isEmpty()){
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        checkIfBranchExists(mergeBranch);
        if (currBranch.equals(mergeBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        Commit mergeCommit = readCommitByBranchName(mergeBranch);
        Commit splitPoint = findSplitPoint(currCommit, mergeCommit);
        if(splitPoint == null){
            System.out.println("Cannot find splitPoint.");
            System.exit(0);
        }
        //
        if(splitPoint.getId().equals(mergeCommit.getId())){
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if(splitPoint.getId().equals(currCommit.getId())){
            System.out.println("Current branch fast-forwarded.");
            checkoutBranch(mergeBranch);
            return;
        }
        String message = "Merged " + mergeBranch + " into " + currBranch + ".";
        List<String> parents = new ArrayList<>(List.of(currCommit.getId(), mergeCommit.getId()));
        Map<String, String> mergedCommitMap = mergeToNewCommit(splitPoint, currCommit, mergeCommit);
        Commit newCommit = new Commit(message, mergedCommitMap, parents);

        saveNewCommit(newCommit);
    }

    private static Map<String, String> mergeToNewCommit(Commit splitPoint, Commit currCommit, Commit mergeCommit) {
        Set<String> allFiles = getAllFiles(splitPoint, currCommit, mergeCommit);
        Set<String> deleteId = new HashSet<>(); //blobId
        Set<String> writeId = new HashSet<>(); //blobId
        Set<String> stopTrack = new HashSet<>();
        Map<String, String> newTrack = new HashMap<>();
        Set<String> conflict = new HashSet<>(); //path
        Map<String, String> newMap = new HashMap<>(currCommit.getPathToBlobID());

        for(String filePath : allFiles){
            String spId = splitPoint.getPathToBlobID().getOrDefault(filePath, "");
            String currId = currCommit.getPathToBlobID().getOrDefault(filePath, "");
            String merId = mergeCommit.getPathToBlobID().getOrDefault(filePath, "");
            //case 2,3,7
            if(currId.equals(merId) || spId.equals(merId)){
                continue;
            }
            //case 1,6
            if(spId.equals(currId)){
                //case 6
                if(merId.equals("")){
                    deleteId.add(spId);
                    stopTrack.add(filePath);
                    //System.out.println(filePath+"//stoptrack");
                }else {
                    writeId.add(merId);
                    newTrack.put(filePath, merId);
                    //System.out.println(filePath+"//newtrack");
                }
            }else {
                conflict.add(filePath);
            }
        }

        List<String> untrackedFiles = getUntrackedFiles();
        for(String fileName : untrackedFiles){
            File file = join(CWD, fileName);
            for(String id : deleteId){
                Blob blob = getBlobById(id);
                if(blob.getFileName().getName().equals(fileName)){
                    unTreackedError();
                }
            }
            for(String id : writeId){
                Blob blob = getBlobById(id);
                if(blob.getFileName().getName().equals(fileName)){
                    unTreackedError();
                }
            }
            if(conflict.contains(file.getPath())){
                unTreackedError();
            }
        }
        deleteFile(deleteId);
        writeFile(writeId);
        for(String i : stopTrack){
            newMap.remove(i);
        }
        for(String i : newTrack.keySet()){
            newMap.put(i, newTrack.get(i));
        }
        for(String filePath : conflict){
            //System.out.println(filePath+"conflict????????????");
            String currId = currCommit.getPathToBlobID().getOrDefault(filePath, "");
            String merId = mergeCommit.getPathToBlobID().getOrDefault(filePath, "");
            String newContent = getContentAsStringFromBlobId(currId);
            String mergContent = getContentAsStringFromBlobId(merId);
            String content = getConflictFile(newContent.split("\n"), mergContent.split("\n"));
            File file = new File(filePath);
            //更新文件内容
            writeContents(file, content);
            //更新blobId
            Blob blob = new Blob(file);
            newMap.put(filePath, blob.getId());
            System.out.println("Encountered a merge conflict.");
        }
        return newMap;
    }
    private static void unTreackedError(){
        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
        System.exit(0);
    }
    private static void printfile(Commit c){
        System.out.println(c.getMessage()+"///");
        for(String i : c.getPathToBlobID().keySet()){
            Blob blob = getBlobById(c.getPathToBlobID().get(i));
            System.out.print(blob.getFileName().getName()+" ");
        }
        System.out.println();
    }

    private static String getConflictFile(String[] head, String[] other) {
        StringBuffer sb = new StringBuffer();
        int len1 = head.length, len2 = other.length;
        int i = 0, j = 0;
        while (i < len1 && j < len2) {
            if (head[i].equals(other[j])) {
                sb.append(head[i]);
            } else {
                sb.append(getConflictContent(head[i], other[j]));
            }
            i++;
            j++;
        }
        // head.len > other.len
        while (i < len1) {
            sb.append(getConflictContent(head[i], ""));
            i++;
        }
        // head.len < other.len
        while (j < len1) {
            sb.append(getConflictContent("", other[j]));
            j++;
        }
        return sb.toString();
    }

    private static String getConflictContent(String head, String other) {
        StringBuffer sb = new StringBuffer();
        sb.append("<<<<<<< HEAD\n");
        // contents of file in current branch
        sb.append(head.equals("") ? head : head + "\n");
        sb.append("=======\n");
        // contents of file in given branch
        sb.append(other.equals("") ? other : other + "\n");
        sb.append(">>>>>>>\n");
        return sb.toString();
    }

    private static String getContentAsStringFromBlobId(String blobId) {
        if (blobId.equals("")) {
            return "";
        }
        return getBlobById(blobId).getContentAsString();
    }

    private static Set<String> getAllFiles(Commit a, Commit b, Commit c){
        Set<String> set = new HashSet<>();
        set.addAll(a.getPathToBlobID().keySet());
        set.addAll(b.getPathToBlobID().keySet());
        set.addAll(c.getPathToBlobID().keySet());
        return set;
    }
    private static Commit findSplitPoint(Commit commit1, Commit commit2) {
        Set<String> ancestors = bfsFromCommit(commit1);
        Queue<Commit> queue = new LinkedList<>();
        queue.add(commit2);
        while (!queue.isEmpty()){
            Commit com = queue.poll();
            if(ancestors.contains(com.getId())){
                return com;
            }
            if(!com.getParents().isEmpty()){
                for(String parent : com.getParents()){
                    queue.add(readCommitById(parent));
                }
            }
        }
        return null;
    }

    private static Set<String> bfsFromCommit(Commit commit) {
        Set<String> ancestors = new HashSet<>();
        Queue<Commit> queue = new LinkedList<>();
        queue.add(commit);
        while(!queue.isEmpty()){
            Commit com = queue.poll();
            if(!ancestors.contains(com.getId()) && !com.getParents().isEmpty()){
                for(String parent : com.getParents()){
                    queue.add(readCommitById(parent));
                }
            }
            ancestors.add(com.getId());
        }
        return ancestors;
    }

    public static void addRemote(String remoteName, String remotePath) {
        File remote = join(HEADS_DIR, remoteName);
        if (remote.exists()) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
        remote.mkdir();

        // java.io.File.separator
        if (File.separator.equals("\\")) {
            remotePath = remotePath.replaceAll("/", "\\\\\\\\");
        }

        /*
        [remote "origin"]
	        url = ..\\remotegit\\.git
	        fetch = +refs/heads/*:refs/remotes/origin/*
         */
        String content = readContentsAsString(CONFIG);
        content += "[remote \"" + remoteName + "\"]\n";
        content += remotePath + "\n";

        writeContents(CONFIG, content);
    }

    public static void rmRemote(String remoteName) {
        File remote = join(HEADS_DIR, remoteName);
        if (!remote.exists()) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }

        remote.delete();

        String[] contents = readContentsAsString(CONFIG).split("\n");
        String target = "[remote \"" + remoteName + "\"]";;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < contents.length;) {
            if (contents[i].equals(target)) {
                i += 2;
            } else {
                sb.append(contents[i]);
            }
        }
        writeContents(CONFIG, sb.toString());
    }

    public static void push(String remoteName, String remoteBranchName) {
        File remotePath = getRemotePath(remoteName);

        currCommit = readCurrCommit();
        Set<String> history = bfsFromCommit(currCommit);
        //change to remote CWD
        configDIR(new File(remotePath.getParent()));
        Commit remoteHead = readCurrCommit();
        Set<String> remoteHistory = bfsFromCommit(remoteHead);

        if (!history.contains(remoteHead.getId())) {
            System.out.println("Please pull down remote changes before pushing.");
            configDIR(new File(System.getProperty("user.dir")));
            System.exit(0);
        }

        // If the Gitlet system on the remote machine exists
        // but does not have the input branch,
        // then simply add the branch to the remote Gitlet.
        File remoteBranch = getBranchFile(remoteBranchName);
        if (!remoteBranch.exists()) {
            branch(remoteBranchName);
        }
        configDIR(new File(System.getProperty("user.dir")));
        currCommit = readCurrCommit();
        configDIR(new File(remotePath.getParent()));
        writeContents(remoteBranch, currCommit.getId());

        // append the future commits to the remote branch.
        history.removeAll(remoteHistory);
        for (String commitId : history) {
            if (commitId.equals(remoteHead.getId())) {
                continue;
            }
            configDIR(new File(System.getProperty("user.dir")));

            Commit commit = readCommitById(commitId);

            configDIR(new File(remotePath.getParent()));

            File remoteCommit = join(OBJECT_DIR, commitId);
            writeObject(remoteCommit, commit);

            if (!commit.getPathToBlobID().isEmpty()) {
                for (Map.Entry<String, String> item: commit.getPathToBlobID().entrySet()) {
                    configDIR(new File(System.getProperty("user.dir")));
                    String blobId = item.getValue();
                    Blob blob = getBlobById(blobId);
                    configDIR(new File(remotePath.getParent()));
                    File remoteBlob = join(OBJECT_DIR, blobId);
                    writeObject(remoteBlob, blob);
                }
            }
        }

        // Then, the remote should reset to the front of the appended commits
        // (so its head will be the same as the local head).
        configDIR(new File(System.getProperty("user.dir")));
        currCommit = readCurrCommit();
        configDIR(new File(remotePath.getParent()));
        reset(currCommit.getId());
        configDIR(new File(System.getProperty("user.dir")));
    }
    private static File getRemotePath(String remoteName) {
        String path = "";
        String[] contents = readContentsAsString(CONFIG).split("\n");
        for (int i = 0; i < contents.length;) {
            if (contents[i].contains(remoteName)) {
                path = contents[i + 1];
                break;
            } else {
                i += 2;
            }
        }

        File file = null;
        try {
            file = new File(path).getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (path.equals("") || !file.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        return file;
    }

    public static void fetch(String remoteName, String remoteBranchName) {
        File remotePath = getRemotePath(remoteName);
        configDIR(new File(remotePath.getParent()));
        File remoteBranchFile = getBranchFile(remoteBranchName);
        if (remoteBranchFile == null || !remoteBranchFile.exists()) {
            System.out.println("That remote does not have that branch.");
            configDIR(new File(System.getProperty("user.dir")));
            System.exit(0);
        }

        Commit remoteBranchCommit = readCommitByBranchName(remoteBranchName);

        // This branch is created in the local repository if it did not previously exist.
        // just update remotes/[remote]/[remote branch] file to new Commit id.
        configDIR(new File(System.getProperty("user.dir")));
        File branch = join(HEADS_DIR, remoteName, remoteBranchName);
        writeContents(branch, remoteBranchCommit.getId());

        // fetch down all commits and blobs
        configDIR(new File(remotePath.getParent()));
        Set<String> history = bfsFromCommit(remoteBranchCommit);

        for (String commitId : history) {
            configDIR(new File(remotePath.getParent()));
            Commit commit = readCommitById(commitId);
            configDIR(new File(System.getProperty("user.dir")));
            File commitFile = join(OBJECT_DIR, commit.getId());
            if (commitFile.exists()) {
                continue;
            }
            writeObject(commitFile, commit);

            if (commit.getPathToBlobID().isEmpty()) {
                continue;
            }
            for (Map.Entry<String, String> item: commit.getPathToBlobID().entrySet()) {
                String blobId = item.getValue();
                configDIR(new File(remotePath.getParent()));
                Blob blob = getBlobById(blobId);
                configDIR(new File(System.getProperty("user.dir")));
                File blobFile = join(OBJECT_DIR, blobId);
                writeObject(blobFile, blob);
            }
        }
        configDIR(new File(System.getProperty("user.dir")));
    }
    private static File getBranchFile(String branchName) {
        File file = null;
        String[] branches = branchName.split("/");
        if (branches.length == 1) {
            file = join(HEADS_DIR, branchName);
        } else if (branches.length == 2) {
            file = join(HEADS_DIR, branches[0], branches[1]);
        }
        return file;
    }
    public static void pull(String remoteName, String remoteBranchName) {
        fetch(remoteName, remoteBranchName);
        merge(remoteName + "/" + remoteBranchName);
    }
}
