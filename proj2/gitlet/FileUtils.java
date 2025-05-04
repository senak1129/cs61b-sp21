package gitlet;

import java.util.List;

import static gitlet.GitletContents.CWD;
import static gitlet.Utils.*;

public class FileUtils {
    public static void restoreCommitFile(Commit target) {
        // 1. 把目标提交里的文件检出到工作目录
        for (String fname : target.getFileVersion().keySet()) {
            String contents = Repository.GetFileContent(target, fname);
            Utils.writeContents(Utils.join(CWD, fname), contents);
        }
        // 2. 删除工作目录里所有“不属于目标提交”的文件
        List<String> cwdFiles = plainFilenamesIn(CWD);
        for (String fname : cwdFiles) {
            if (!target.getFileVersion().containsKey(fname)) {
                Utils.restrictedDelete(Utils.join(CWD, fname));
            }
        }
    }

    public static boolean isOverwritingOrDeletingCWDUntracked(String fileName, Commit currentCommit) {
        List<String> CWDFileNames = plainFilenamesIn(CWD);
        assert CWDFileNames != null && currentCommit != null;
        return !CommitUtils.isTrackedByCommit(fileName,currentCommit) && CWDFileNames.contains(fileName);
    }
}
