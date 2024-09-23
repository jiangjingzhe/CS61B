package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static gitlet.Utils.readContents;
import static gitlet.Utils.sha1;
import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;

public class Blob implements Serializable {
    private String id;

    private byte[] bytes;

    private File fileName;

    private String filePath;

    private File blobSaveFileName;

    public String getId() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public File getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public File getBlobSaveFileName() {
        return blobSaveFileName;
    }

    public Blob(File fileName){
        this.fileName = fileName;
        this.bytes = readFile();
        this.filePath = fileName.getPath();
        this.id = generateID();
        this.blobSaveFileName = generateBlobSaveFileName();
    }

    private File generateBlobSaveFileName() {
        return join(OBJECT_DIR, id);
    }

    private String generateID() {
        return sha1(filePath, bytes);
    }

    private byte[] readFile() {
        return readContents(fileName);
    }
    public void save(){
        writeObject(blobSaveFileName, this);
    }

    public String getContentAsString(){
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
