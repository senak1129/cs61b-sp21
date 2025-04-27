package gitlet;

import static gitlet.Utils.*;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static gitlet.GitletConstants.*;

public class IndexUtils {
    //FileName Sha1
    public static HashMap<String,String> indexMap;

    //sha1 contest
    public static HashMap<String,String> stagedFileContents;

    static{
        if(Repository.isInitialized()){
            indexMap = readIndex();
            stagedFileContents = readStagedFile();
        }
    }

    public static HashMap<String,String> readIndex(){
        return readHashMap(INDEX_FILE);
    }

    public static HashMap<String,String> readStagedFile(){
        return readHashMap(STAGED_FILE);
    }

    //读取文件的内容作为哈希map返回
    @SuppressWarnings("unchecked")
    public static HashMap<String,String> readHashMap(File file){
        if(file.length()==0){
            return new HashMap<>();
        }
        HashMap<String,String> hashMap = readObject(file, HashMap.class);
        return hashMap == null ? new HashMap<>() : hashMap;
    }

    public static List<String> getUntrackedFiles(Commit commit) {
        List<String> CWDFileNames = plainFilenamesIn(CWD);
        List<String> result = new LinkedList<>();
        assert CWDFileNames != null;
        for (String fileName : CWDFileNames) {
            if (!isStaged(fileName, commit) && !CommitUtils.isTrackedByCommit(commit, fileName)) {
                result.add(fileName);
            }
        }
        return result;
    }

    public static List<String> getRemovedFiles(Commit commit) {
        HashMap<String, String> fileVersionMap = commit.getFileVersionMap();
        List<String> result = new LinkedList<>();
        for (String fileName : fileVersionMap.keySet()) {
            if (!indexMap.containsKey(fileName)) {
                result.add(fileName);
            }
        }
        result.sort(String::compareTo);
        return result;
    }

    public static List<StringBuffer> deletedNotStagedForCommit(Commit commit) {
        List<String> CWDFileNames = plainFilenamesIn(CWD);
        assert CWDFileNames != null;
        List<StringBuffer> result = new LinkedList<>();
        List<String> stagedFiles = getStagedFiles(commit);
        for (String fileName : stagedFiles) {
            if (!CWDFileNames.contains(fileName)) {
                result.add(new StringBuffer(fileName));
            }
        }
        HashMap<String, String> fileVersionMap = commit.getFileVersionMap();
        for (String fileName : fileVersionMap.keySet()) {
            if (!CWDFileNames.contains(fileName) && !isRemoval(fileName, commit)) {
                result.add(new StringBuffer(fileName));
            }
        }
        return result;
    }

    public static boolean isRemoval(String fileName, Commit commit) {
        assert fileName != null && commit != null;
        return commit.getFileVersionMap().containsKey(fileName) && !indexMap.containsKey(fileName);
    }

    public static void saveIndex(){
        //把indexmap写入index
        Utils.writeObject(INDEX_FILE, indexMap);
        //把stagedFileContents 写入 STAGED_FILE
        Utils.writeObject(STAGED_FILE, stagedFileContents);
    }

    public static void stageFile(String fileName){

        //按字符串读取CWD目录下的fileName
        String fileContents = readContentsAsString(join(CWD,fileName));
        //把内容转换成Sha1
        String fileSHA1 = sha1(fileContents);

        //添加进去map
        indexMap.put(fileName, fileSHA1);
        stagedFileContents.put(fileSHA1, fileContents);
    }

    //从暂存区得到文件
    //commit是上一次提交
    public static List<String> getStagedFiles(Commit commit){
        //得到上一次提交的map [name:id]
        HashMap<String,String> fileVersionMap = commit.getFileVersionMap();
        List<String> result = new LinkedList<>();
        for (String fileName: indexMap.keySet()){
            //遍历indexmap (indexmap>=fileversionmap)
            //如果名字存在 检查id是否相等(既这个文件是否后续被修改)
            if (fileVersionMap.containsKey(fileName)){
                if(!fileVersionMap.get(fileName).equals(fileVersionMap.get(fileName))){
                    result.add(fileName);
                }
            }else{
               result.add(fileName);
            }
        }
        //字典序排序
        result.sort(String::compareTo);
        return result;
    }


    //fileName是否在暂存区内
    //如果上一个提交无fileName但indexmap有filename 说明对一个新文件进行了add 在暂存区内
    //如果有 但是不等于 说明修改过 所以在暂存区
    public static boolean isStaged(String fileName,Commit commit){
        assert fileName != null && commit != null;
        HashMap<String,String> fileVersionMap = commit.getFileVersionMap();
        if (indexMap.containsKey(fileName) && !fileVersionMap.containsKey(fileName)){
            return true;
        }else return indexMap.containsKey(fileName) && fileVersionMap.containsKey(fileName) && !indexMap.get(fileName).equals(fileVersionMap.get(fileName));
    }

    //两张map去除fileName
    public static void unstageFile(String fileName){
        stagedFileContents.remove(indexMap.get(fileName));
        indexMap.remove(fileName);
    }

    public static List<StringBuffer> modifiedNotStagedForCommit(Commit commit) {
        List<String> CWDFileNames = plainFilenamesIn(CWD);
        List<StringBuffer> result = new LinkedList<>();
        assert CWDFileNames != null;
        for (String fileName : CWDFileNames) {
            boolean fileIsStaged = isStaged(fileName, commit);
            boolean fileIsTracked = CommitUtils.isTrackedByCommit(commit, fileName);
            if ((fileIsStaged && !FileUtils.hasSameSHA1(fileName, indexMap.get(fileName))) ||
                    (fileIsTracked && !FileUtils.hasSameSHA1(fileName, commit.getFileVersionMap().get(fileName)) && !fileIsStaged)) {
                result.add(new StringBuffer(fileName));
            }
        }
        return result;
    }
}
