package gitlet;
import java.io.File;
import java.util.List;

import static gitlet.Utils.*;
import static gitlet.GitletContents.*;
public class BranchUtils {
    /**
     * 保存指定分支的提交 ID。
     *
     * 将指定分支的最新提交 ID 保存到分支文件中，以便可以在以后检索该分支的提交记录。
     *
     * @param branchName 要保存的分支名称
     * @param commitId 要保存的提交 ID
     */
    public static void saveBranchCommit(String branchName, String commitId) {
        writeContents(join(BRANCH_DIR, branchName), commitId);
    }

    /**
     * 设置当前 HEAD 分支。
     *
     * 该方法会更新当前工作目录中的 HEAD 文件，指向指定的分支，表明当前检出的是该分支。
     *
     * @param branchName 要设置为当前分支的名称
     */
    public static void setHEAD(String branchName) {
        Repository.HEAD = branchName;
        writeContents(HEAD_FILE, branchName);
    }

    /**
     * 检查指定分支是否存在。
     *
     * 通过获取所有分支名并检查其中是否包含指定的分支名来判断该分支是否存在。
     *
     * @param branchName 要检查的分支名称
     * @return 如果分支存在则返回 true，否则返回 false
     */
    public static boolean branchExists(String branchName) {
        List<String> branchNameList = Repository.getAllBranches();
        return branchNameList.contains(branchName);
    }

    /**
     * 获取指定分支的最新提交对象。
     *
     * 根据指定的分支名，从分支目录中查找并读取该分支对应的提交 ID，并返回该提交对象。
     *
     * @param branchName 要获取提交的分支名称
     * @return 如果分支存在，则返回该分支的提交对象；否则返回 null
     */
    public static Commit getBranchCommit(String branchName) {
        File F = join(BRANCH_DIR, branchName);
        if (F.exists()) {
            return Repository.GetCommitByCommitIdPrefix(readContentsAsString(F));
        } else {
            return null;
        }
    }

    /**
     * 获取指定分支的提交 ID。
     *
     * 根据指定的分支名，从分支目录中读取该分支保存的提交 ID，并返回它。
     *
     * @param branchName 要获取提交 ID 的分支名称
     * @return 如果分支存在，则返回该分支的提交 ID；否则返回 null
     */
    public static String getBranchCommitId(String branchName) {
        File F = join(BRANCH_DIR, branchName);
        if (F.exists()) {
            return readContentsAsString(F);
        } else {
            return null;
        }
    }


}

