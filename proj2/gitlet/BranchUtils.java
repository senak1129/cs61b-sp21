package gitlet;
import static gitlet.Utils.*;
import static gitlet.GitletContents.*;
public class BranchUtils {

    public static void SaveBranchCommit(String BranchName, String CommitId) {
        writeContents(join(BRANCH_DIR, BranchName), CommitId);
    }

    public static void SetHEAD(String BranchName){
        Repository.HEAD = BranchName;
        writeContents(HEAD_FILE, BranchName);
    }
}
