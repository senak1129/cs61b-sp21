package gitlet;

import java.io.File;
import java.util.Date;
import gitlet.Commit.*;

import static gitlet.GitletContents.BRANCH_DIR;
import static gitlet.GitletContents.COMMITS_DIR;
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

    public static String GetCommitId(Commit commit){
        return sha1(serialize(commit));
    }
}
