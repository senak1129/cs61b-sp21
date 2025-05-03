package gitlet;

import static gitlet.GitletContents.CWD;
import static gitlet.Utils.join;
import static gitlet.Utils.restrictedDelete;

public class FileUtils {
    public static void RestoreCommitFile(Commit TargetCommit) {
        //将目标commit的文件写入工作目录(覆盖)
        for(String FileName : TargetCommit.GetFileVersion().keySet()) {
            String Contents = Repository.GetFileContent(TargetCommit, FileName);
            Utils.writeContents(join(CWD,FileName), Contents);
        }
        //删除当前分支有但目标分支没有的文件
        for(String FileName : Repository.GetLastCommit().GetFileVersion().keySet()) {
            if(!TargetCommit.GetFileVersion().containsKey(FileName)) {
                restrictedDelete(join(CWD,FileName));
            }
        }
    }
}
