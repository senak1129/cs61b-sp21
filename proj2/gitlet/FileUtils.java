package gitlet;
import java.util.List;

import static gitlet.GitletConstants.*;
import static gitlet.Utils.*;

public class FileUtils {
    //在objects创建一个[id,content]的文件
    public static String writeGitletObjectsFile(String content){
        String fileObjectId = sha1(content);
        writeContents(join(OBJECTS_DIR,fileObjectId),content);
        return fileObjectId;
    }


    //得到fileSHA1对应的内容
    public static String getFileContent(String fileSHA1){
        //objects文件夹内[sha1->内容]
        return readContentsAsString(join(OBJECTS_DIR,fileSHA1));
    }

    public static boolean hasSameSHA1(String fileName, String targetSHA1) {
        return getFileContentSHA1(fileName).equals(targetSHA1);
    }

    public static String getFileContentSHA1(String fileName) {
        return sha1(readContents(join(CWD,fileName)));
    }

    //通过提交信息与fileName得到上一个提交名为filename的文件的内容
    public static String getFileContent(String fileName,Commit commit){
        assert commit != null && fileName != null;
        //getFileversion.get(filename)得到sha1调用上面的方法
        return getFileContent(commit.getFileVersionMap().get(fileName));
    }

    //在CWD目录下写一个或覆盖一个filename的文件 名filename 内容content
    public static void writeCWDFile(String fileName, String content){
        writeContents(join(CWD,fileName),content);
    }

    //CWD目录下的文件是改重写删除嘛
    public static boolean isOverwritingOrDeletingCWDUntracked(String fileName, Commit commit){
        List<String> CWDFileNames = plainFilenamesIn(CWD);
        assert CWDFileNames != null && commit != null;
        //名为fileName的文件没有被跟踪并且确实存在
        return !CommitUtils.isTrackedByCommit(commit,fileName)&&CWDFileNames.contains(fileName);
    }
}
