package gitlet;

import java.io.File;
import java.util.Date;
import static gitlet.Utils.*;
import static gitlet.Repository.*;
import static gitlet.GitletConstants.*;

public class BranchUtils {
    public static void saveCommitId(String branchName,String commitId){
        writeContents(join(BRANCHES_DIR,branchName),commitId);
    }
}
