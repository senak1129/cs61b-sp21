package gitlet;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import static gitlet.GitletContents.*;
import static gitlet.Utils.*;
import static gitlet.Utils.serialize;

public class CommitUtils {

    public static Commit MakeEmptyCommit(String message){
        Commit c = new Commit();
        c.SetMessage(message);
        c.SetDate(new Date(0));
        c.SetFirstParentCommitId(null);
        c.SetSecondParentCommitId(null);
        return c;
    }

    public static Commit MakeCommit(String message){
        Commit c = new Commit();
        c.SetMessage(message);
        c.SetDate(new Date(0));
        c.SetFirstParentCommitId(GetLastCommitId());
        c.SetSecondParentCommitId(null);
        c.SetFileVersion(IndexUtils.IndexMap);
        return c;
    }

    public static String GetLastCommitId(){
        return readContentsAsString(join(BRANCH_DIR,Repository.HEAD));
    }

    public static void SaveCommit(Commit commit){
        String commitId = GetCommitId(commit);
        File commitFile = join(COMMITS_DIR, commitId);
        writeObject(commitFile, commit);
    }

    public static void CreateFileObject(Commit LastCommit, Commit NowCommit){
        HashMap<String,String>LastFileVersion = LastCommit.GetFileVersion();
        HashMap<String,String>NowFileVersion = NowCommit.GetFileVersion();
        for(String FileName : NowFileVersion.keySet()){
            if(!LastFileVersion.containsKey(FileName)){
                String FileSha1 = NowFileVersion.get(FileName);
                String FileSha1Content = IndexUtils.StagedMap.get(FileSha1);
                writeContents(join(OBJECTS_DIR,FileSha1), FileSha1Content);
            }else if(!LastFileVersion.get(FileName).equals(NowFileVersion.get(FileName))){
                String LastFileSha1 = LastFileVersion.get(FileName);
                String NowFileSha1 = NowFileVersion.get(FileName);
                String FileSha1Content = IndexUtils.StagedMap.get(NowFileSha1);
                writeContents(join(OBJECTS_DIR,LastFileSha1), FileSha1Content);
            }
        }
    }

    public static Commit GetCommitByCommitId(String CommitId){
        if(CommitId == null){
            return null;
        }
        return readObject(join(COMMITS_DIR, CommitId), Commit.class);
    }

    public static String GetCommitId(Commit commit){
        return sha1(serialize(commit));
    }


}
