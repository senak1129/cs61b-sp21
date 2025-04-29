package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {

    private String message;

    private Date date;

    private String FirstParentCommitId;

    private String SecondParentCommitId;

    private HashMap<String,String>FileVersion;


    public Commit(){
        FileVersion = new HashMap<>();
    }

    public void SetMessage(String message){
        this.message = message;
    }

    public void SetDate(Date date){
        this.date = date;
    }

    public void SetFirstParentCommitId(String FirstParentCommitId){
        this.FirstParentCommitId = FirstParentCommitId;
    }

    public void SetSecondParentCommitId(String SecondParentCommitId){
        this.SecondParentCommitId = SecondParentCommitId;
    }

    public void SetFileVersion(HashMap<String,String> FileVersion){
        this.FileVersion = FileVersion;
    }


    public String GetMessage(){
        return this.message;
    }

    public Date GetDate(){
        return this.date;
    }

    public String GetFirstParentCommitId(){
        return this.FirstParentCommitId;
    }

    public String GetSecondParentCommitId(){
        return this.SecondParentCommitId;
    }

    public HashMap<String,String> GetFileVersion(){
        return this.FileVersion;
    }

}
