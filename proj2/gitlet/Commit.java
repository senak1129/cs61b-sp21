package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {

    private String message;

    private Date date;

    private String firstParentCommitId;

    private String secondParentCommitId;

    private HashMap<String,String> fileVersion;


    public Commit(){
        fileVersion = new HashMap<>();
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setDate(Date date){
        this.date = date;
    }

    public void setFirstParentCommitId(String firstParentCommitId){
        this.firstParentCommitId = firstParentCommitId;
    }

    public void setSecondParentCommitId(String secondParentCommitId){
        this.secondParentCommitId = secondParentCommitId;
    }

    public void setFileVersion(HashMap<String,String> fileVersion){
        this.fileVersion = fileVersion;
    }

    public String getMessage(){
        return this.message;
    }

    public Date getDate(){
        return this.date;
    }

    public String getFirstParentCommitId(){
        return this.firstParentCommitId;
    }

    public String getSecondParentCommitId(){
        return this.secondParentCommitId;
    }

    public HashMap<String,String> getFileVersion(){
        return this.fileVersion;
    }
}
