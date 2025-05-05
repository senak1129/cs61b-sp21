package gitlet;
import java.io.File;
import java.util.List;

import static gitlet.Utils.*;
import static gitlet.GitletContents.*;
public class BranchUtils {


    public static void saveBranchCommit(String branchName, String commitId) {
        writeContents(join(BRANCH_DIR, branchName), commitId);
    }

    public static void setHEAD(String branchName) {
        Repository.HEAD = branchName;
        writeContents(HEAD_FILE, branchName);
    }

    public static boolean branchExists(String branchName) {
        List<String> branchNameList = Repository.getAllBranches();
        return branchNameList.contains(branchName);
    }

    public static Commit getBranchCommit(String branchName) {
        File F = join(BRANCH_DIR, branchName);
        if (F.exists()) {
            return Repository.GetCommitByCommitIdPrefix(readContentsAsString(F));
        } else {
            return null;
        }
    }

    public static String getBranchCommitId(String branchName) {
        File F = join(BRANCH_DIR, branchName);
        if (F.exists()) {
            return readContentsAsString(F);
        } else {
            return null;
        }
    }
}

