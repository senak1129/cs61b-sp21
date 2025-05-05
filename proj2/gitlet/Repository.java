package gitlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.IndexUtils.*;
import static gitlet.Utils.*;
import static gitlet.CommitUtils.*;
import static gitlet.GitletContents.*;
public class Repository {
    public static String HEAD;

    static {
        if (IsInitial()) {
            HEAD = readContentsAsString(HEAD_FILE);
        }
    }

    public static boolean IsInitial() {
        return GITLET_DIR.exists();
    }

    public static void init() {
        //如果gitlet文件夹存在
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        //创建
        GITLET_DIR.mkdirs();
        BRANCH_DIR.mkdirs();
        OBJECTS_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        try {
            HEAD_FILE.createNewFile();
            INDEX_FILE.createNewFile();
            STAGED_FILE.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Commit EmptyCommit = makeEmptyCommit("initial commit");
        saveCommit(EmptyCommit);
        String CommitId = getCommitId(EmptyCommit);
        BranchUtils.saveBranchCommit("master", CommitId);
        BranchUtils.setHEAD("master");

    }

    public static void add(String fileName) {
        if (!join(CWD, fileName).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        if (IndexMap.containsKey(fileName)) {
            String fileNameSha1 = IndexMap.get(fileName);
            String fileNameSha2 = sha1(readContentsAsString(join(CWD, fileName)));
            if (fileNameSha1.equals(fileNameSha2)) {
                return;
            }
        }
        //写入两张map
        IndexUtils.stagedFile(fileName);
        //写入两个文件
        IndexUtils.saveIndex();
    }

    /*
    1:首先确保commitMessage不为空
    2:确保这次提交和上一次提交是有差异的(!IndexMap.equals(fileVersion)
    3:使用makeCommit创建新提交，IndexMap作为其fileVersion，为了保存这个commit，在commits文件夹创建一个文件[id,commit]，在objects文件夹创建每个文件版本的对象[contentSHA1:content]
    4:清空暂存区，并为stagedfile和index写入
    5:为目前分支写入最新一次提交
     */
    public static void commit(String commitMessage) {
        if (commitMessage.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        String lastCommitId = CommitUtils.getLastCommitId();
        Commit lastCommit = CommitUtils.getCommitByCommitId(lastCommitId);
        HashMap<String, String> fileVersion = lastCommit.getFileVersion();

        if (IndexMap.equals(fileVersion)) {
            System.out.println("No changes added to the commit.");
            return;
        }

        //创建新的commit fileversion = IndexMap
        Commit nowCommit = makeCommit(commitMessage);

        //在commits文件夹创建
        saveCommit(nowCommit);
        //在objects文件夹创建
        CommitUtils.createFileObject(lastCommit, nowCommit);
        //清空暂存区
        StagedMap.clear();
        //为两文件写入
        IndexUtils.saveIndex();

        String newCommitId = getCommitId(nowCommit);
        //为目前分支写入最新一次提交
        BranchUtils.saveBranchCommit(HEAD, newCommitId);
    }

    /*
    1:false false:文件没有被添加（add）也没有被提交（commit）。可能是刚创建的新文件，也可能是已被移除索引的文件。
    2:true false:文件已被 add 暂存，但尚未被提交。也可能是修改后被再次 add 的新文件。
    3:false true:文件曾在某次提交中存在，但当前没有被 add（可能是用户手动删除了）。
    4:true true:文件曾经提交过，且现在在暂存区中也存在（通常是修改后又 add 了）。
     */
    public static void rm(String fileName) {
        boolean isStaged = IndexUtils.isStaged(fileName,GetLastCommit());
        boolean isTrackedByLastCommit = CommitUtils.isTrackedByCommit(fileName,GetLastCommit());
        if (!isStaged && !isTrackedByLastCommit) {
            System.out.println("No reason to remove the file.");
            return;
        }
        //为两张map清除fileName
        IndexUtils.unStageFile(fileName);
        //保存进两个文件
        IndexUtils.saveIndex();
        //如果是被上一个文件跟踪还需要删除这个文件
        if (isTrackedByLastCommit) {
            restrictedDelete(join(CWD, fileName));
        }
    }

    public static void log() {
        Commit LastCommit = getCommitByCommitId(getLastCommitId());
        while (LastCommit != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
            System.out.println("===");
            System.out.println("commit " + getCommitId(LastCommit));
            System.out.println("Date: " + sdf.format(LastCommit.getDate()));
            System.out.println(LastCommit.getMessage());
            System.out.println();
            LastCommit = getCommitByCommitId(LastCommit.getFirstParentCommitId());
        }
    }

    public static void globalLog() {
        List<String> commitIdList = plainFilenamesIn(COMMITS_DIR);
        if (commitIdList == null || commitIdList.isEmpty()) {
            return;
        }
        for (String commitId : commitIdList) {
            Commit commit = CommitUtils.getCommitByCommitId(commitId);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
            System.out.println("===");
            System.out.println("commit " + commitId);
            System.out.println("Date: " + sdf.format(commit.getDate()));
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    public static void find(String message) {
        List<String> commitIdList = plainFilenamesIn(COMMITS_DIR);
        if (commitIdList == null || commitIdList.isEmpty()) {
            return;
        }
        boolean found = false;
        for (String commitId : commitIdList) {
            Commit commit = CommitUtils.getCommitByCommitId(commitId);
            if(commit.getMessage().equals(message)) {
                System.out.println(commitId);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static String GetFileContent(Commit commit, String FileName) {
        String FileSha1 = commit.getFileVersion().get(FileName);
        return readContentsAsString(join(OBJECTS_DIR, FileSha1));
    }

    /**
     * checkout 命令实现：
     * 1. checkout -- [filename]              → 将当前分支最新提交中的某文件恢复到工作目录
     * 2. checkout [commitId] -- [filename]   → 将指定提交中的某文件恢复到工作目录
     * 3. checkout [branchName]               → 切换分支并恢复工作区文件
     */
    public static void checkout(String... args) {
        // ----------------------------
        // 情况 1：checkout -- [filename]
        // ----------------------------
        if (args.length == 3 && args[1].equals("--")) {
            String FileName = args[2];
            Commit LastCommit = getCommitByCommitId(getLastCommitId());

            // 如果该文件不在上次提交中，报错
            if (!LastCommit.getFileVersion().containsKey(FileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }

            // 恢复文件内容到当前工作目录
            String content = GetFileContent(LastCommit, FileName);
            Utils.writeContents(join(CWD, FileName), content);

            // ----------------------------
            // 情况 2：checkout [commitId] -- [filename]
            // ----------------------------
        } else if (args.length == 4 && args[2].equals("--")) {
            String CommitId = args[1];
            String FileName = args[3];

            // 根据前缀查找对应的提交
            Commit commit = GetCommitByCommitIdPrefix(CommitId);

            if (commit == null) {
                System.out.println("No commit with that id exists.");
                return;
            }

            if (!commit.getFileVersion().containsKey(FileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }

            // 恢复指定文件内容到当前工作目录
            String content = GetFileContent(commit, FileName);
            Utils.writeContents(join(CWD, FileName), content);

            // ----------------------------
            // 情况 3：checkout [branchName]
            // ----------------------------
        } else if (args.length == 2) {
            String BranchName = args[1];

            // 已在该分支上，报错
            if (BranchName.equals(HEAD)) {
                System.out.println("No need to checkout the current branch.");
                return;
            }

            List<String> BranchList = getAllBranches();

            // 分支不存在，报错
            if (!BranchList.contains(BranchName)) {
                System.out.println("No such branch exists.");
                return;
            } else {
                List<String> CWDFileNames = plainFilenamesIn(CWD);
                assert CWDFileNames != null;

                // 遍历当前目录，是否有未被跟踪的文件，防止覆盖用户数据
                for (String FileName : CWDFileNames) {
                    if (!CommitUtils.isTrackedByCommit(FileName, GetLastCommit())) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }
                }

                // 切换分支：恢复该分支的最新提交状态，并更新 HEAD
                restoreCommit(GetBranchLastCommit(BranchName));
                BranchUtils.setHEAD(BranchName);
            }

            // ----------------------------
            // 参数格式不正确
            // ----------------------------
        } else {
            System.out.println("Incorrect operands.");
        }
    }


    public static void ckout(Commit commit, String fileName) {
        if(!isTrackedByCommit(fileName, commit)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String fileSHA1 = commit.getFileVersion().get(fileName);
        String fileConent = GetFileContent(commit, fileName);
        writeContents(join(CWD,fileName), fileSHA1);
    }

    public static Commit GetCommitByCommitIdPrefix(String commitIdPrefix) {
        List<String> commitIdList = plainFilenamesIn(COMMITS_DIR);
        if (commitIdList == null) {
            return null;
        }
        int queryCount = 0;
        String resultCommitId = null;
        for (String commitId : commitIdList) {
            if (commitId.startsWith(commitIdPrefix)) {
                queryCount++;
                resultCommitId = commitId;
            }
        }
        if (queryCount > 1) {
            throw new RuntimeException("this prefix is ambiguous, you must use longer prefix");
        }
        return getCommitByCommitId(resultCommitId);
    }

    /**
     * 将指定的 commit 恢复为当前工作目录状态。
     * 主要用于分支切换或 reset 操作。
     *
     * 会覆盖当前工作目录内容，因此必须确保没有未追踪文件。
     *
     * @param commit 要恢复的提交快照
     */
    public static void restoreCommit(Commit commit) {
        // 获取当前 HEAD 所指的提交（当前版本）
        Commit currentCommit = Repository.GetLastCommit();

        // 遍历目标提交中的所有文件
        for (String fileName : commit.getFileVersion().keySet()) {
            // 如果该文件在当前版本中不存在，且存在于 CWD ⇒ 表示是未被追踪的文件
            if (FileUtils.isOverwritingOrDeletingCWDUntracked(fileName, currentCommit)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return; // 阻止覆盖，避免数据丢失
            }
        }

        // 将目标提交中的所有文件恢复到当前目录
        FileUtils.restoreCommitFile(commit);

        // 用该提交中的文件状态替换 Index（表示当前暂存区和提交内容一致）
        IndexMap = commit.getFileVersion();

        // 清空已暂存修改（即清空“准备提交”的内容）
        StagedMap.clear();

        // 保存 Index（更新 index 文件）
        IndexUtils.saveIndex();
    }


    public static Commit GetBranchLastCommit(String BranchName) {
        String CommitId = readContentsAsString(join(BRANCH_DIR,BranchName));
        return CommitUtils.getCommitByCommitId(CommitId);
    }

    public static Commit GetLastCommit() {
        return CommitUtils.getCommitByCommitId(getLastCommitId());
    }

    public static void status(){
        System.out.println("=== Branches ===");
        List<String> BranchList = getAllBranches();
        for(String BranchName:BranchList) {
            if(BranchName.equals(HEAD)) {
                System.out.println("*"+BranchName);
            }else{
                System.out.println(BranchName);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        List<String> StagedFile = getStagedFiles(GetLastCommit());
        StagedFile.forEach(System.out::println);
        System.out.println();

        System.out.println("=== Removed Files ===");
        List<String> RemovedFile = getRemovedFiles(GetLastCommit());
        RemovedFile.forEach(System.out::println);
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static List<String> getAllBranches() {
        List<String> branchList = plainFilenamesIn(BRANCH_DIR);
        branchList.sort(String::compareTo);
        return branchList;
    }

    /**
     * 获取已暂存的文件列表（即将纳入下一次提交的文件）。
     * 比较当前 Index 与传入的 commit 中的文件状态差异。
     *
     * @param commit 当前对比的 Commit（通常是上一次提交）
     * @return 所有已暂存（新增或修改）的文件名列表（按字典序排序）
     */
    public static List<String> getStagedFiles(Commit commit) {
        List<String> stagedFiles = new LinkedList<>();

        // 获取上一次提交中记录的文件版本（文件名 -> 内容哈希）
        HashMap<String, String> fileverison = commit.getFileVersion();

        // 遍历当前 Index 中的所有文件（即被 add 的文件）
        for (String FileName : IndexMap.keySet()) {

            // 情况 1：Index 中有，但 Commit 中没有 —— 新文件
            if (!fileverison.containsKey(FileName)) {
                stagedFiles.add(FileName); // 属于新增，加入结果列表

            } else {
                // 情况 2：Index 和 Commit 中都有，但内容哈希不同 —— 被修改的文件
                String ContentSha1 = fileverison.get(FileName);
                if (!IndexMap.get(FileName).equals(ContentSha1)) {
                    stagedFiles.add(FileName); // 属于修改后暂存，加入结果列表
                }
            }
        }

        // 对结果列表按文件名字典序排序，便于输出和版本一致性
        stagedFiles.sort(String::compareTo);

        return stagedFiles;
    }


    public static void makeNewBranch(String BranchName) {
        List<String> BranchList = getAllBranches();
        if(BranchList.contains(BranchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        writeContents(join(BRANCH_DIR,BranchName), getLastCommitId());
    }

    public static void removeBranch(String branchName) {
        List<String> BranchList = getAllBranches();
        if(!BranchList.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if(branchName.equals(HEAD)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        join(BRANCH_DIR,branchName).delete();
    }

    //说白了就是不能有未跟踪文件 比如新建一个文件 什么都没干  因为等会要把commit的内容覆盖给工作区
    public static void reset(String commitIdPrefix) {
        Commit commit = GetCommitByCommitIdPrefix(commitIdPrefix);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        String commitId = CommitUtils.getCommitId(commit);
        restoreCommit(commit);
        BranchUtils.saveBranchCommit(HEAD, commitId);
    }


    /**
     * 获取被移除的文件列表（即：上次提交中存在，但现在不在暂存区 Index 中的文件）。
     *
     * @param commit 当前对比的 Commit（通常是上一次提交）
     * @return 被删除的文件名列表（按字典序排序）
     */
    public static List<String> getRemovedFiles(Commit commit) {
        List<String> RemovedFiles = new LinkedList<>();

        // 获取上一次提交中记录的文件版本（文件名 -> 内容哈希）
        HashMap<String, String> fileverison = commit.getFileVersion();

        // 遍历上次提交中所有文件
        for (String FileName : fileverison.keySet()) {

            // 如果某个文件不在当前 Index 中，说明它已被删除或未再被 add
            if (!IndexMap.containsKey(FileName)) {
                RemovedFiles.add(FileName);  // 加入“被移除”文件列表
            }
        }

        // 对结果按文件名字典序排序，便于一致输出
        RemovedFiles.sort(String::compareTo);

        return RemovedFiles;
    }


    public static void merge(String branchName) {
        if(!BranchUtils.branchExists(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        if(HEAD.equals(branchName)) {
            System.out.println("Cannot merge the current branch.");
            return;
        }

        Commit lastCommit = GetLastCommit();
        List<String> stagedFileNames = getStagedFiles(lastCommit);
        List<String> removedFileNames = getRemovedFiles(lastCommit);
        if (!stagedFileNames.isEmpty() || !removedFileNames.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }

        Commit branchCommit = BranchUtils.getBranchCommit(branchName);
        Commit splitCommit = CommitUtils.findSplitPoint(HEAD, branchName);
        if (splitCommit == null || CommitUtils.isSameCommit(branchCommit, splitCommit)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return; //给定分支已经完全被包含在当前分支里
        }

        //           master     other
        //commit1 <- commit2 <- commit3
        //此时让master文件等于commit3的id checkout commit3即可
        if (CommitUtils.isSameCommit(lastCommit, splitCommit)) {
            String savedHEAD = HEAD;
            checkout(branchName);
            HEAD = savedHEAD;
            // fast-forward master pointer
            BranchUtils.saveBranchCommit(HEAD,BranchUtils.getBranchCommitId(branchName));
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        Set<String> splitPointFiles = splitCommit.getFileVersion().keySet();
        Set<String> lastCommitFiles = lastCommit.getFileVersion().keySet();
        Set<String> branchCommitFiles = branchCommit.getFileVersion().keySet();

        Set<String>allFiles = new HashSet<>();
        allFiles.addAll(splitPointFiles);
        allFiles.addAll(lastCommitFiles);
        allFiles.addAll(branchCommitFiles);

        boolean isConflict = false;
        for(String fileName : allFiles) {
            boolean splitCurrentConsistent = isConsistent(fileName,splitCommit,lastCommit);
            boolean splitBranchConsistent = isConsistent(fileName,splitCommit,branchCommit);
            boolean branchCurrentConsistent = isConsistent(fileName,lastCommit,branchCommit);

            //merge no conflicts
            if((splitBranchConsistent && ! splitCurrentConsistent) || branchCurrentConsistent) {
                continue;
            }

            if(!splitBranchConsistent && splitCurrentConsistent) {
                if(!branchCommitFiles.contains(fileName)) {
                    if(FileUtils.isOverwritingOrDeletingCWDUntracked(fileName,lastCommit)){
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }else{
                        rm(fileName);
                    }
                }else{
                    if(FileUtils.isOverwritingOrDeletingCWDUntracked(fileName,lastCommit)){
                        System.out.println("There is an untracked file in the way; delete it first.");
                        return;
                    }else{
                        ckout(branchCommit,fileName);
                        add(fileName);
                    }
                }
                continue;
            }
        }

        commit("Merged " + branchName + "into " + HEAD + ".");
        Commit mergeCommit = GetLastCommit();
        mergeCommit.setSecondParentCommitId(BranchUtils.getBranchCommitId(branchName));
        saveCommit(mergeCommit);

        BranchUtils.saveBranchCommit(HEAD, getCommitId(mergeCommit));
        if(isConflict) {
            System.out.println("Encountered a merge conflict.");
        }

    }


}
