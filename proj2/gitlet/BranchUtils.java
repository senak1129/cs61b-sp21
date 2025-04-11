package gitlet;

import java.io.File;
import java.util.Date;
import static gitlet.Utils.*;
import static gitlet.Repository.*;

public class BranchUtils {
    public static void saveCommitId(String branchName,String commitId){
        writeContents(join(Repository.COMMITS_DIR,branchName),commitId);
    }
}
