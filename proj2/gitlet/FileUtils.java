package gitlet;

import java.util.List;

import static gitlet.GitletContents.CWD;
import static gitlet.GitletContents.OBJECTS_DIR;
import static gitlet.Utils.*;

public class FileUtils {
    /**
     * 将工作目录恢复成指定提交 `target` 的快照状态：
     * 1. 提交中存在的文件 → 写入当前工作目录
     * 2. 提交中不存在但当前目录存在的文件 → 删除
     *
     * @param target 要恢复的提交
     */
    public static void restoreCommitFile(Commit target) {
        // ① 写入：把目标提交中的所有文件内容写入当前目录
        for (String fname : target.getFileVersion().keySet()) {
            String contents = Repository.GetFileContent(target, fname);
            Utils.writeContents(Utils.join(CWD, fname), contents);
        }

        // ② 删除：将工作目录中不在该提交快照中的文件删除
        List<String> cwdFiles = plainFilenamesIn(CWD);
        for (String fname : cwdFiles) {
            if (!target.getFileVersion().containsKey(fname)) {
                // 说明该文件不属于目标提交，应删除
                Utils.restrictedDelete(Utils.join(CWD, fname));
            }
        }
    }


    /**
     * 判断某个文件是否是未被当前提交（currentCommit）追踪，但又出现在当前工作目录中的文件。
     * 这类文件如果继续执行 checkout/restore 操作，可能会被覆盖，导致数据丢失。
     *
     * @param fileName      被检查的文件名
     * @param currentCommit 当前分支的最新提交
     * @return 如果是“未被追踪但出现在 CWD 中的文件”，返回 true（即危险）
     */
    public static boolean isOverwritingOrDeletingCWDUntracked(String fileName, Commit currentCommit) {
        // 获取当前目录下所有文件名
        List<String> CWDFileNames = plainFilenamesIn(CWD);

        // 保证当前提交不为空，且获取到工作目录文件列表
        assert CWDFileNames != null && currentCommit != null;

        // 条件说明：
        // - 该文件不是当前提交追踪的（用户未 add）
        // - 但它现在存在于工作目录中 ⇒ 表示这个文件是“用户本地新增的未追踪文件”
        return !CommitUtils.isTrackedByCommit(fileName, currentCommit)
                && CWDFileNames.contains(fileName);
    }

    public static void writeCWDFile(String fileName, String content) {
        writeContents(join(CWD, fileName), content);
    }

    public static String getFileContent(String fileSHA1) {
        return readContentsAsString(join(OBJECTS_DIR, fileSHA1));
    }

}
