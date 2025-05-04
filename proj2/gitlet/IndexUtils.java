package gitlet;

import static gitlet.Utils.*;
import static gitlet.GitletContents.*;

import java.util.HashMap;

public class IndexUtils {
    public static HashMap<String,String> IndexMap;

    public static HashMap<String,String> StagedMap;

    static {
        if(INDEX_FILE.length() == 0){
            IndexMap = new HashMap<>();
        }else{
            IndexMap = readObject(INDEX_FILE, HashMap.class);
        }
        if(STAGED_FILE.length() == 0){
            StagedMap = new HashMap<>();
        }else{
            StagedMap = readObject(STAGED_FILE, HashMap.class);
        }
    }


    //为两个文件写入
    public static void saveIndex(){
        Utils.writeObject(INDEX_FILE, IndexMap);
        Utils.writeObject(STAGED_FILE, StagedMap);
    }

    //暂存区
    public static void stagedFile(String fileName){
        String FileContent = readContentsAsString(join(CWD,fileName));
        String FileSha1 = sha1(FileContent);
        IndexMap.put(fileName,FileSha1);
        StagedMap.put(FileSha1,FileContent);
    }

    //新添加 或者 修改
    public static boolean isStaged(String fileName, Commit commit){
        assert fileName != null && commit != null;
        HashMap<String, String> fileVersionMap = commit.getFileVersion();
        return (IndexMap.containsKey(fileName) && !fileVersionMap.containsKey(fileName))
                || (IndexMap.containsKey(fileName) && fileVersionMap.containsKey(fileName)
                && !fileVersionMap.get(fileName).equals(IndexMap.get(fileName)));
    }

    public static void unStageFile(String fileName){
        String FileSha1 = IndexMap.get(fileName);
        StagedMap.remove(FileSha1);
        IndexMap.remove(fileName);
    }
}
