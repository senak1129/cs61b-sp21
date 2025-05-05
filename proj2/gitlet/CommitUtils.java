package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.GitletContents.*;
import static gitlet.Utils.*;
import static gitlet.Utils.serialize;

public class CommitUtils {

    /**
     * 创建一个空的提交对象，仅包含一个提交消息。
     *
     * @param message 提交消息
     * @return 一个空的提交对象
     */
    public static Commit makeEmptyCommit(String message) {
        Commit c = new Commit();
        c.setMessage(message);
        c.setDate(new Date(0));
        c.setFirstParentCommitId(null);
        c.setSecondParentCommitId(null);
        return c;
    }

    /**
     * 创建一个新的提交对象，包含一个提交消息，并将其与上一个提交关联。
     *
     * @param message 提交消息
     * @return 一个新的提交对象
     */
    public static Commit makeCommit(String message) {
        Commit c = new Commit();
        c.setMessage(message);
        c.setDate(new Date(0));
        c.setFirstParentCommitId(getLastCommitId());
        c.setSecondParentCommitId(null);
        c.setFileVersion(IndexUtils.IndexMap);
        return c;
    }

    /**
     * 获取当前分支的最后一次提交的 ID。
     *
     * @return 最后一次提交的 ID
     */
    public static String getLastCommitId() {
        return readContentsAsString(join(BRANCH_DIR, Repository.HEAD));
    }

    /**
     * 将一个提交对象保存到 commits_dir 目录中。
     *
     * @param commit 提交对象
     */
    public static void saveCommit(Commit commit) {
        String commitId = getCommitId(commit);
        File commitFile = join(COMMITS_DIR, commitId);
        writeObject(commitFile, commit);
    }

    /**
     * 在 objects_dir 目录下创建与当前提交相关的文件对象。
     * 如果当前提交包含文件，而上一个提交没有，或者文件内容发生了变化，将文件内容保存到文件系统。
     *
     * @param LastCommit 上一个提交对象
     * @param NowCommit 当前提交对象
     */
    public static void createFileObject(Commit LastCommit, Commit NowCommit) {
        HashMap<String, String> LastFileVersion = LastCommit.getFileVersion();
        HashMap<String, String> NowFileVersion = NowCommit.getFileVersion();
        for (String FileName : NowFileVersion.keySet()) {
            if (!LastFileVersion.containsKey(FileName)) { // 如果上一个提交没有此文件
                String FileSha1 = NowFileVersion.get(FileName);
                String FileSha1Content = IndexUtils.StagedMap.get(FileSha1);
                writeContents(join(OBJECTS_DIR, FileSha1), FileSha1Content);
            } else if (!LastFileVersion.get(FileName).equals(NowFileVersion.get(FileName))) { // 文件版本发生变化
                String LastFileSha1 = LastFileVersion.get(FileName);
                String NowFileSha1 = NowFileVersion.get(FileName);
                String FileSha1Content = IndexUtils.StagedMap.get(NowFileSha1);
                writeContents(join(OBJECTS_DIR, NowFileSha1), FileSha1Content);
            }
        }
    }

    /**
     * 根据提交 ID 获取对应的提交对象。
     *
     * @param commitId 提交 ID
     * @return 如果提交存在，返回对应的提交对象；如果提交不存在，返回 null
     */
    public static Commit getCommitByCommitId(String commitId) {
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
            return null;
        }
    }

    /**
     * 查找两个分支的最近共同祖先提交（split point）。
     *
     * @param b1 分支 1 的名字
     * @param b2 分支 2 的名字
     * @return 最近的共同祖先提交对象
     * @throws RuntimeException 如果找不到共同祖先
     */
    public static Commit findSplitPoint(String b1, String b2) {
        Commit head1 = BranchUtils.getBranchCommit(b1);
        Commit head2 = BranchUtils.getBranchCommit(b2);

        // 1. 收集 b1 所有祖先
        Set<String> seen = new HashSet<>();
        Deque<Commit> stack = new ArrayDeque<>();
        stack.push(head1);
        while (!stack.isEmpty()) {
            Commit cur = stack.pop();
            if (cur == null) {
                continue;
            }
            String id = getCommitId(cur); // 获取提交 ID
            if (!seen.add(id)) {
                continue; // 如果已访问过，跳过
            }

            // 压栈父提交
            String p1 = cur.getFirstParentCommitId();
            String p2 = cur.getSecondParentCommitId(); // 可能为 null
            if (p1 != null) {
                stack.push(Repository.GetCommitByCommitIdPrefix(p1));
            }
            if (p2 != null) {
                stack.push(Repository.GetCommitByCommitIdPrefix(p2));
            }
        }

        // 2. 从 b2 做 BFS，首个命中的就是最近的公共祖先
        Deque<Commit> queue = new ArrayDeque<>();
        Set<String> visited2 = new HashSet<>();
        queue.add(head2);
        while (!queue.isEmpty()) {
            Commit cur = queue.poll();
            if (cur == null) continue;
            String id = getCommitId(cur);
            if (!visited2.add(id)) continue;

            if (seen.contains(id)) {
                return cur; // 找到 split point
            }

            String p1 = cur.getFirstParentCommitId();
            String p2 = cur.getSecondParentCommitId();
            if (p1 != null) {
                queue.add(Repository.GetCommitByCommitIdPrefix(p1));
            }
            if (p2 != null) {
                queue.add(Repository.GetCommitByCommitIdPrefix(p2));
            }
        }

        throw new RuntimeException("找不到共同祖先！");
    }

    /**
     * 判断两个提交是否相同（通过比较提交 ID）。
     *
     * @param commit1 提交对象 1
     * @param commit2 提交对象 2
     * @return 如果两个提交相同，返回 true；否则返回 false
     */
    public static boolean isSameCommit(Commit commit1, Commit commit2) {
        if (commit1 == null || commit2 == null) return false;
        return getCommitId(commit1).equals(getCommitId(commit2));
    }

    /**
     * 获取提交对象的唯一 ID。
     *
     * @param commit 提交对象
     * @return 提交对象的 ID
     */
    public static String getCommitId(Commit commit) {
        return sha1(serialize(commit)); // 返回提交对象的 SHA-1 哈希值
    }

    /**
     * 判断两个提交中是否包含相同版本的文件。
     *
     * @param fileName 文件名
     * @param commit1 提交对象 1
     * @param commit2 提交对象 2
     * @return 如果两个提交中都有相同版本的文件，返回 true；否则返回 false
     */
    public static boolean isConsistent(String fileName, Commit commit1, Commit commit2) {
        assert fileName != null && commit1 != null && commit2 != null;

        HashMap<String, String> fileVersion1 = commit1.getFileVersion();
        HashMap<String, String> fileVersion2 = commit2.getFileVersion();

        boolean existInCommit1 = fileVersion1.containsKey(fileName);
        boolean existInCommit2 = fileVersion2.containsKey(fileName);

        // 如果两个提交中都没有这个文件，视为一致
        if (!existInCommit1 && !existInCommit2) {
            return true;
        }

        // 如果只有一个提交中有这个文件，视为不一致
        if (!existInCommit1 || !existInCommit2) {
            return false;
        }

        // 两个提交中都存在该文件，比较内容哈希是否一致
        Boolean sameContent = hasSameFileVersion(fileName, commit1, commit2);
        assert sameContent != null;

        return sameContent;
    }

    /**
     * 判断两个提交中指定文件的版本是否相同。
     *
     * @param fileName 文件名
     * @param commit1 提交对象 1
     * @param commit2 提交对象 2
     * @return 如果两个提交中该文件版本相同，返回 true；否则返回 false
     */
    public static Boolean hasSameFileVersion(String fileName, Commit commit1, Commit commit2) {
        assert fileName != null && commit1 != null && commit2 != null;
        HashMap<String, String> fileVersion1 = commit1.getFileVersion();
        HashMap<String, String> fileVersion2 = commit2.getFileVersion();
        if (!fileVersion1.containsKey(fileName) || !fileVersion2.containsKey(fileName)) {
            return null; // 如果其中一个提交不包含该文件，返回 null
        }
        return fileVersion1.get(fileName).equals(fileVersion2.get(fileName));
    }

    /**
     * 判断一个文件是否被当前提交跟踪。
     *
     * @param fileName 文件名
     * @param commit 提交对象
     * @return 如果文件在当前提交的文件版本中，返回 true；否则返回 false
     */
    public static boolean isTrackedByCommit(String fileName, Commit commit) {
        return commit.getFileVersion().containsKey(fileName);
    }

    /**
     * 获取当前分支的最后一个提交对象。
     *
     * @return 当前分支的最后一个提交对象，如果没有提交则返回 null
     */
    public static Commit getLastCommit() {
        String lastCommitId = getLastCommitId();
        if (lastCommitId == null) return null;
        return getCommitByCommitId(lastCommitId);
    }

}
