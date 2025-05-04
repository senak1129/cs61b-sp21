package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.GitletContents.*;
import static gitlet.Utils.*;
import static gitlet.Utils.serialize;

public class CommitUtils {

    public static Commit MakeEmptyCommit(String message){
        Commit c = new Commit();
        c.SetMessage(message);
        c.SetDate(new Date(0));
        c.SetFirstParentCommitId(null);
        c.SetSecondParentCommitId(null);
        return c;
    }

    public static Commit MakeCommit(String message){
        Commit c = new Commit();
        c.SetMessage(message);
        c.SetDate(new Date(0));
        c.SetFirstParentCommitId(GetLastCommitId());
        c.SetSecondParentCommitId(null);
        c.SetFileVersion(IndexUtils.IndexMap);
        return c;
    }

    public static String GetLastCommitId(){
        return readContentsAsString(join(BRANCH_DIR,Repository.HEAD));
    }

    public static void SaveCommit(Commit commit){
        String commitId = GetCommitId(commit);
        File commitFile = join(COMMITS_DIR, commitId);
        writeObject(commitFile, commit);
    }

    public static void CreateFileObject(Commit LastCommit, Commit NowCommit){
        HashMap<String,String>LastFileVersion = LastCommit.GetFileVersion();
        HashMap<String,String>NowFileVersion = NowCommit.GetFileVersion();
        for(String FileName : NowFileVersion.keySet()){
            if(!LastFileVersion.containsKey(FileName)){
                String FileSha1 = NowFileVersion.get(FileName);
                String FileSha1Content = IndexUtils.StagedMap.get(FileSha1);
                writeContents(join(OBJECTS_DIR,FileSha1), FileSha1Content);
            }else if(!LastFileVersion.get(FileName).equals(NowFileVersion.get(FileName))){
                String LastFileSha1 = LastFileVersion.get(FileName);
                String NowFileSha1 = NowFileVersion.get(FileName);
                String FileSha1Content = IndexUtils.StagedMap.get(NowFileSha1);
                writeContents(join(OBJECTS_DIR,NowFileSha1), FileSha1Content);
            }
        }
    }

    public static Commit GetCommitByCommitId(String commitId) {
        if (commitId == null) {
            return null;
        }
        File commitFile = join(COMMITS_DIR, commitId);
        if (!commitFile.exists()) {
            return null;
        }
        try {
            return readObject(commitFile, Commit.class);
        } catch (IllegalArgumentException e) {
            return null;}
    }

    public static Commit findSplitPoint(String b1, String b2) {
        Commit head1 = BranchUtils.getBranchCommit(b1);
        Commit head2 = BranchUtils.getBranchCommit(b2);

        // 1. 收集 b1 所有祖先
        Set<String> seen = new HashSet<>();
        Deque<Commit> stack = new ArrayDeque<>();
        stack.push(head1);
        while (!stack.isEmpty()) {
            Commit cur = stack.pop();
            if (cur == null) continue;
            String id = GetCommitId(cur);       // 你的提交 ID 方法
            if (!seen.add(id)) continue;                  // 已访问过就跳过

            // 把所有父提交都压栈
            String p1 = cur.GetFirstParentCommitId();
            String p2 = cur.GetSecondParentCommitId();   // 可能为 null
            if (p1 != null) stack.push(Repository.GetCommitByCommitIdPrefix(p1));
            if (p2 != null) stack.push(Repository.GetCommitByCommitIdPrefix(p2));
        }

        // 2. 从 b2 做 BFS，首个命中的就是最近的公共祖先
        Deque<Commit> queue = new ArrayDeque<>();
        Set<String> visited2 = new HashSet<>();
        queue.add(head2);
        while (!queue.isEmpty()) {
            Commit cur = queue.poll();
            if (cur == null) continue;
            String id = GetCommitId(cur);
            if (!visited2.add(id)) continue;

            if (seen.contains(id)) {
                return cur;  // 找到 split point
            }

            String p1 = cur.GetFirstParentCommitId();
            String p2 = cur.GetSecondParentCommitId();
            if (p1 != null) queue.add(Repository.GetCommitByCommitIdPrefix(p1));
            if (p2 != null) queue.add(Repository.GetCommitByCommitIdPrefix(p2));
        }

        throw new RuntimeException("找不到共同祖先！");
    }

    public static boolean isSameCommit(Commit commit1, Commit commit2) {
        if(commit1 == null || commit2 == null) return false;
        return GetCommitId(commit1).equals(GetCommitId(commit2));
    }

    public static String GetCommitId(Commit commit){
        return sha1(serialize(commit));
    }

    public static boolean isConsistent(String fileName,Commit commit1,Commit commit2){
        assert fileName != null && commit1 != null && commit2 != null;
        HashMap<String,String>fileVersion1 = commit1.GetFileVersion();
        HashMap<String,String>fileVersion2 = commit2.GetFileVersion();
        boolean existInCommit1 = fileVersion1.containsKey(fileName);
        boolean existInCommit2 = fileVersion2.containsKey(fileName);
        if(!existInCommit1 && !existInCommit2) return true;
        if(!existInCommit1 || !existInCommit2) return false;
        Boolean sameContent = hasSameFileVersion(fileName,commit1,commit2);
        assert sameContent != null;
        return sameContent;
    }

    public static Boolean hasSameFileVersion(String fileName,Commit commit1,Commit commit2){
        assert fileName != null && commit1 != null && commit2 != null;
        HashMap<String,String>fileVersion1 = commit1.GetFileVersion();
        HashMap<String,String>fileVersion2 = commit2.GetFileVersion();
        if(!fileVersion1.containsKey(fileName) || !fileVersion2.containsKey(fileName)) return null;
        return fileVersion1.get(fileName).equals(fileVersion2.get(fileName));
    }
}
