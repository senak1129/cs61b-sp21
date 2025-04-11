package gitlet;

import java.io.File;
import java.util.Date;
import static gitlet.Utils.*;


public class CommitUtils {
    public static Commit makeEmptyCommit(String message){
        Commit commit = new Commit();
        commit.setMessage(message);
        commit.setCurrentTime(new Date(0));
        commit.setParentId(null);
        commit.setSecondeParentId(null);
        return commit;
    }

    public static String getCommitId(Commit commit){
        return sha1(serialize(commit));
    }

    public static String saveCommit(Commit commit){
        String CommitID = getCommitId(commit);
        File commitFile = join(Repository.COMMITS_DIR,CommitID);
        writeContents(commitFile, commit);
        return CommitID;
    }

}
