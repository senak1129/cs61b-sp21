package gitlet;
import java.io.File;
import java.util.List;

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

    public static boolean branchExists(String BranchName) {
        List<String> branchNameList = Repository.GetAllBranches();
        return branchNameList.contains(BranchName);
    }

    public static Commit getBranchCommit(String BranchName){
        File F = join(BRANCH_DIR, BranchName);
        if(F.exists()){
            return Repository.GetCommitByCommitIdPrefix(readContentsAsString(F));
        }else{
            return null;
        }
    }

    public static void saveCommitId(String branchName, String CommitId) {
        Utils.writeContents(join(BRANCH_DIR, branchName), CommitId);
    }

    public static String gerBranchCommitId(String BranchName){
        File F = join(BRANCH_DIR, BranchName);
        if(F.exists()){
            return readContentsAsString(F);
        }else{
            return null;
        }
    }
}

