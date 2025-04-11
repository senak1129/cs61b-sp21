package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private Date currentTime;

    //sha1
    private String parentId;

    private String secondeParentId;

    private HashMap<String,String> fileVersionMap;


    /* TODO: fill in the rest of this class. */
    public Commit(){
        fileVersionMap = new HashMap<>();
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setCurrentTime(Date currentTime){
        this.currentTime = currentTime;
    }

    public void setParentId(String parentId){
        this.parentId = parentId;
    }
    public void setSecondeParentId(String secondeParentId){
        this.secondeParentId = secondeParentId;
    }

}
