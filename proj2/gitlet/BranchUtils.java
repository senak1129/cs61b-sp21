package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static gitlet.Utils.*;
import static gitlet.Repository.*;
import static gitlet.GitletConstants.*;

public class BranchUtils {
    //为名为branchName的分支文件写入commitId表示名为branchName的文件上一次提交是commitId
    public static void saveCommitId(String branchName,String commitId){
        writeContents(join(BRANCHES_DIR,branchName),commitId);
    }

    //得到名为branchName分支的上一次提交
    public static String getCommitId(String branchName){
        return readContentsAsString(join(BRANCHES_DIR,branchName));
    }

/*    public static List<String> getAllBranchNames(){

    }*/

    public static boolean branchExists(String branchName){
/*        if(branchName.contains("/")){
            return getRemoteBranchFile()
        }*/
        List<String>branchNameList = getAllBranchNames();
        return branchNameList.contains(branchName);
    }

    public static List<String> getAllBranchNames(){
        List<String> branchNameList = plainFilenamesIn(BRANCHES_DIR);
        return branchNameList;
    }
}
