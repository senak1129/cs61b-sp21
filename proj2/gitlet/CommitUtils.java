package gitlet;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static gitlet.Utils.*;
import static gitlet.GitletConstants.*;


public class CommitUtils {

    //空提交
    public static Commit makeEmptyCommit(String message){
        Commit commit = new Commit();
        commit.setMessage(message);
        commit.setCurrentTime(new Date(0));
        commit.setParentId(null);
        commit.setSecondeParentId(null);
        return commit;
    }

    //得到commit对应的id，把commit序列化在sha1即可
    public static String getCommitId(Commit commit){
        return sha1(serialize(commit));
    }

    public static Commit makeCommit(String message, String parentCommitId, HashMap<String,String> fileVersionMap){
        Commit commit = new Commit();
        commit.setMessage(message);
        commit.setParentId(parentCommitId);
        commit.setCurrentTime(new Date(0));
        commit.setSecondeParentId(null);
        commit.setFileVersionMap(fileVersionMap);
        return commit;
    }

    public static String saveCommit(Commit commit){
        String CommitID = getCommitId(commit);
        File commitFile = join(COMMITS_DIR,CommitID);
        writeObject(commitFile, commit);
        return CommitID;
    }

    //找到commits文件夹中对应id的文件即可
    public static Commit readCommit(String CommitId){
        if(CommitId == null){
            return null;
        }
        //把commits文件夹的CommitId作为对象读入并返回
        return readObject(join(COMMITS_DIR,CommitId),Commit.class);
    }

    //在objects文件夹里为新提交增加或修改的文件创建[id:content]的文件

    //提取旧提交的map 和 新提交的map
    //遍历新提交 若有一个文件名旧提交也有 但是内容不一样(id) 于是需要新建
    //如果没有也需要新建
    public static void createFileObjects(Commit oldCommit, Commit newCommit, HashMap<String,String> stagedFiles){
        HashMap<String,String> oldFileVersionMap = oldCommit.getFileVersionMap();
        HashMap<String,String> newFileVersionMap = newCommit.getFileVersionMap();
        for (String fileName : newFileVersionMap.keySet()) {
            if(oldFileVersionMap.containsKey(fileName)){
                if(!oldFileVersionMap.get(fileName).equals(newFileVersionMap.get(fileName))){
                    //newFileVersionMap.get(fileName)->文件id
                    //stagedFiles.get(id)->内容
                    //交给writeGitletObjectsFile:使他对于内容创建新的sha1并创建或覆盖文件[id,content]
                    FileUtils.writeGitletObjectsFile(stagedFiles.get(newFileVersionMap.get(fileName)));
                }
            }else{
                FileUtils.writeGitletObjectsFile(stagedFiles.get(newFileVersionMap.get(fileName)));
            }
        }
    }

    public static List<Commit> commitTraceBack(Commit currentCommit){
        List<Commit> commitList = new LinkedList<>();
        //指针
        Commit commitPtr = currentCommit;
        while (commitPtr != null) {
            commitList.add(commitPtr);
            //父提交
            commitPtr = readCommit(commitPtr.getParentId());
        }
        return commitList;
    }

    //fileName是否被commit跟踪
    public static boolean isTrackedByCommit(Commit commit,String fileName){
        assert commit != null && fileName != null;
        return commit.getFileVersionMap().containsKey(fileName);
    }

}
