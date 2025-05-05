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

    //写入两张map
    public static void stagedFile(String fileName){
        String fileContent = readContentsAsString(join(CWD,fileName));
        String fileSha1 = sha1(fileContent);
        IndexMap.put(fileName,fileSha1);
        StagedMap.put(fileSha1,fileContent);
    }

    //ture:
    //新文件被暂存：文件不在上一次提交中，但被 add 进了 Index
    //修改后暂存：文件在提交中已有，但内容修改后又 add 了新版本
    //false:
    //文件不在 Index 中（即未 add）
    //文件在 Index 中，但和上次提交版本相同（add 后未修改）
    //文件不在提交和 Index 中（从未存在）
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
