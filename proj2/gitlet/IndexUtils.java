package gitlet;

import static gitlet.Utils.*;
import static gitlet.GitletContents.*;

import java.io.IOException;
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

    public static void SaveIndex(){
        Utils.writeObject(INDEX_FILE, IndexMap);
        Utils.writeObject(STAGED_FILE, StagedMap);
    }

    //暂存区
    public static void StagedFile(String FileName){
        String FileContent = readContentsAsString(join(CWD,FileName));
        String FileSha1 = sha1(FileContent);
        IndexMap.put(FileName,FileSha1);
        StagedMap.put(FileSha1,FileContent);
    }

    //新添加 或者 修改
    public static boolean IsStaged(String FileName,Commit commit){
        assert FileName != null && commit != null;
        HashMap<String, String> fileVersionMap = commit.GetFileVersion();
        return (IndexMap.containsKey(FileName) && !fileVersionMap.containsKey(FileName))
                || (IndexMap.containsKey(FileName) && fileVersionMap.containsKey(FileName)
                && !fileVersionMap.get(FileName).equals(IndexMap.get(FileName)));
    }

    public static boolean IsTrackedByCommit(String FileName,Commit commit){
        return commit.GetFileVersion().containsKey(FileName);
    }

    public static void UnStageFile(String FileName){
        String FileSha1 = IndexMap.get(FileName);
        StagedMap.remove(FileSha1);
        IndexMap.remove(FileName);
    }
}
