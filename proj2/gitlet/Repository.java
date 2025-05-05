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

    /**
     * 初始化 Gitlet 仓库。
     *
     * 如果当前目录下已经存在 Gitlet 仓库，则会提示用户，并返回。否则，会在当前目录创建必要的 Gitlet 文件夹和文件，并初始化一个空的提交（initial commit）。
     */
    public static void init() {
        // 如果 gitlet 文件夹已经存在
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        // 创建所需的目录
        GITLET_DIR.mkdirs();
        BRANCH_DIR.mkdirs();
        OBJECTS_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        try {
            // 创建文件
            HEAD_FILE.createNewFile();
            INDEX_FILE.createNewFile();
            STAGED_FILE.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 创建一个空的提交作为初始化提交
        Commit emptyCommit = makeEmptyCommit("initial commit");
        saveCommit(emptyCommit);
        String commitId = getCommitId(emptyCommit);
        // 保存 "master" 分支的提交
        BranchUtils.saveBranchCommit("master", commitId);
        // 设置 HEAD 为 "master" 分支
        BranchUtils.setHEAD("master");
    }

    /**
     * 将文件添加到暂存区（Index）中。
     *
     * 该方法会检查文件是否存在。如果文件已经存在于暂存区，并且内容没有变化，则不会进行任何操作。
     * 如果文件内容有变动或者是新文件，将该文件添加到暂存区并保存索引。
     *
     * @param fileName 要添加的文件名
     */
    public static void add(String fileName) {
        // 检查文件是否存在
        if (!join(CWD, fileName).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        // 检查文件是否已经被暂存，且内容没有变化
        if (IndexMap.containsKey(fileName)) {
            String fileNameSha1 = IndexMap.get(fileName);
            String fileNameSha2 = sha1(readContentsAsString(join(CWD, fileName)));
            if (fileNameSha1.equals(fileNameSha2)) {
                return; // 文件内容没有变化
            }
        }
        // 将文件写入暂存区（Index）
        IndexUtils.stagedFile(fileName);
        // 保存索引和暂存区
        IndexUtils.saveIndex();
    }

    /**
     * 提交当前暂存区的变更并创建一个新的提交。
     *
     * 1. 首先确保提交信息不为空。
     * 2. 确保这次提交与上次提交有所不同（即 IndexMap 与上次提交的文件版本不同）。
     * 3. 使用 `makeCommit` 方法创建一个新的提交，将 IndexMap 作为文件版本。
     * 4. 将提交保存到 `commits` 文件夹，并为每个文件版本在 `objects` 文件夹中创建对象。
     * 5. 清空暂存区，并将更新的索引写入文件。
     * 6. 更新当前分支的提交 ID。
     *
     * @param commitMessage 提交信息
     */
    public static void commit(String commitMessage) {
        // 确保提交信息不为空
        if (commitMessage.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        // 获取上次提交的 ID 和 Commit 对象
        String lastCommitId = CommitUtils.getLastCommitId();
        Commit lastCommit = CommitUtils.getCommitByCommitId(lastCommitId);
        HashMap<String, String> fileVersion = lastCommit.getFileVersion();

        // 如果暂存区与上次提交的文件版本相同，说明没有更改
        if (IndexMap.equals(fileVersion)) {
            System.out.println("No changes added to the commit.");
            return;
        }

        // 创建当前的提交对象，文件版本为 IndexMap
        Commit nowCommit = makeCommit(commitMessage);

        // 在 commits 文件夹中保存提交
        saveCommit(nowCommit);

        // 在 objects 文件夹中保存文件版本对象
        CommitUtils.createFileObject(lastCommit, nowCommit);

        // 清空暂存区
        StagedMap.clear();

        // 保存索引文件
        IndexUtils.saveIndex();

        // 获取当前提交的 ID
        String newCommitId = getCommitId(nowCommit);

        // 更新当前分支的提交 ID
        BranchUtils.saveBranchCommit(HEAD, newCommitId);
    }

    /**
     * 删除指定文件。
     *
     * 删除文件有以下几种情况：
     * 1. 如果文件未被暂存且未被提交，则输出提示信息并返回。
     * 2. 如果文件已被暂存但尚未提交，移除文件从暂存区并保存更改。
     * 3. 如果文件曾经被提交，但用户手动删除该文件，也会从暂存区移除，并在工作区删除该文件。
     *
     * @param fileName 要删除的文件名
     */
    public static void rm(String fileName) {
        boolean isStaged = IndexUtils.isStaged(fileName, GetLastCommit());
        boolean isTrackedByLastCommit = CommitUtils.isTrackedByCommit(fileName, GetLastCommit());

        // 如果文件既未暂存，也未提交，则无理由删除
        if (!isStaged && !isTrackedByLastCommit) {
            System.out.println("No reason to remove the file.");
            return;
        }

        // 移除文件从暂存区
        IndexUtils.unStageFile(fileName);

        // 保存更改
        IndexUtils.saveIndex();

        // 如果文件是被上一个提交跟踪的，还需要删除工作区中的该文件
        if (isTrackedByLastCommit) {
            restrictedDelete(join(CWD, fileName));
        }
    }

    /**
     * 打印当前分支的提交历史记录。
     *
     * 从当前分支的最新提交开始，依次遍历所有父提交，并打印每个提交的提交信息、提交日期及提交 ID。
     */
    public static void log() {
        Commit lastCommit = getCommitByCommitId(getLastCommitId());
        while (lastCommit != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
            System.out.println("===");
            System.out.println("commit " + getCommitId(lastCommit));
            System.out.println("Date: " + sdf.format(lastCommit.getDate()));
            System.out.println(lastCommit.getMessage());
            System.out.println();
            lastCommit = getCommitByCommitId(lastCommit.getFirstParentCommitId());
        }
    }

    /**
     * 打印仓库中的所有提交历史。
     *
     * 遍历所有提交，并打印每个提交的提交信息、提交日期及提交 ID。
     */
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

    /**
     * 根据提交信息查找提交 ID。
     *
     * 遍历仓库中的所有提交，并查找提交信息与给定信息相匹配的提交。如果找到，输出该提交的 ID。
     * 如果没有找到，则输出提示信息。
     *
     * @param message 提交信息
     */
    public static void find(String message) {
        List<String> commitIdList = plainFilenamesIn(COMMITS_DIR);
        if (commitIdList == null || commitIdList.isEmpty()) {
            return;
        }
        boolean found = false;
        for (String commitId : commitIdList) {
            Commit commit = CommitUtils.getCommitByCommitId(commitId);
            if (commit.getMessage().equals(message)) {
                System.out.println(commitId);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * 获取指定提交中的文件内容。
     *
     * 根据提交对象和文件名获取指定文件的内容。
     *
     * @param commit 提交对象
     * @param fileName 文件名
     * @return 文件内容
     */
    public static String GetFileContent(Commit commit, String fileName) {
        String fileSha1 = commit.getFileVersion().get(fileName);
        return readContentsAsString(join(OBJECTS_DIR, fileSha1));
    }

    /**
     * 检出分支或指定提交中的文件。
     *
     * 如果给定的是分支名称，则会切换到该分支。检查工作区是否有未提交的文件，如果有，则输出提示信息。
     * 如果给定的是提交 ID，则会恢复提交中的文件内容。
     *
     * @param args 参数数组
     */
    public static void checkout(String... args) {
        Commit commit = null;
        if (args.length > 2) {
            String fileName;
            if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                }
                fileName = args[2];
                commit = getLastCommit();
            } else {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                }
                fileName = args[3];
                commit = GetCommitByCommitIdPrefix(args[1]);
                if (commit == null) {
                    System.out.println("No commit with that id exists.");
                    return;
                }
            }
            checkoutFile(commit, fileName);
        } else {
            commit = GetLastCommit();
            checkoutBranch(commit, args[1]);
        }
    }

    /**
     * 切换到指定分支。
     *
     * 切换分支之前，检查工作区是否有未提交的文件。如果没有问题，恢复该分支的内容并将 HEAD 指向该分支。
     *
     * @param commit 当前提交对象
     * @param branchName 分支名称
     */
    public static void checkoutBranch(Commit commit, String branchName) {
        if (!BranchUtils.branchExists(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branchName.equals(HEAD)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        List<String> cwdFileNames = plainFilenamesIn(CWD);
        assert cwdFileNames != null;
        for (String fileName : cwdFileNames) {
            if (!CommitUtils.isTrackedByCommit(fileName, commit)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        // 恢复分支提交的内容到工作区
        Commit newBranchCommit = CommitUtils.getCommitByCommitId(BranchUtils.getBranchCommitId(branchName));
        restoreCommit(newBranchCommit);

        // 设置 HEAD 为新分支
        BranchUtils.setHEAD(branchName);
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


    /**
     * 获取当前分支的最后一次提交。
     *
     * 通过获取当前分支的提交 ID，调用 `CommitUtils` 来获取对应的 `Commit` 对象。
     *
     * @return 当前分支的最后一次提交对象
     */
    public static Commit GetLastCommit() {
        return CommitUtils.getCommitByCommitId(getLastCommitId());
    }

    /**
     * 显示当前 Git 仓库的状态。
     *
     * 该方法依次输出以下信息：
     * 1. 所有分支，标记当前分支。
     * 2. 已暂存的文件（Staged Files）。
     * 3. 被移除的文件（Removed Files）。
     * 4. 尚未暂存但已修改的文件（Modifications Not Staged For Commit）。
     * 5. 未跟踪的文件（Untracked Files）。
     */
    public static void status() {
        System.out.println("=== Branches ===");
        List<String> branchList = getAllBranches();
        for (String branchName : branchList) {
            if (branchName.equals(HEAD)) {
                System.out.println("*" + branchName);  // 当前分支前加上星号
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        List<String> stagedFiles = getStagedFiles(GetLastCommit());
        stagedFiles.forEach(System.out::println);  // 打印已暂存的文件
        System.out.println();

        System.out.println("=== Removed Files ===");
        List<String> removedFiles = getRemovedFiles(GetLastCommit());
        removedFiles.forEach(System.out::println);  // 打印已移除的文件
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /**
     * 获取当前所有分支的列表。
     *
     * 该方法从 `BRANCH_DIR` 目录中读取所有分支的文件，并按字母顺序排序。
     *
     * @return 当前仓库的所有分支名称列表
     */
    public static List<String> getAllBranches() {
        List<String> branchList = plainFilenamesIn(BRANCH_DIR);
        branchList.sort(String::compareTo);  // 按字母顺序排序
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


    /**
     * 创建一个新的分支。
     *
     * 首先检查指定的分支名是否已存在。如果分支名已存在，则输出错误信息并返回。否则，创建该分支，并将当前分支的提交 ID 保存在新分支的文件中。
     *
     * @param branchName 要创建的新分支的名称
     */
    public static void makeNewBranch(String branchName) {
        List<String> branchList = getAllBranches();
        if (branchList.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        writeContents(join(BRANCH_DIR, branchName), getLastCommitId());
    }

    /**
     * 删除指定的分支。
     *
     * 首先检查指定的分支是否存在。如果分支不存在，则输出错误信息并返回。如果分支是当前分支（HEAD），则无法删除该分支。
     *
     * @param branchName 要删除的分支的名称
     */
    public static void removeBranch(String branchName) {
        List<String> branchList = getAllBranches();
        if (!branchList.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(HEAD)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        join(BRANCH_DIR, branchName).delete();
    }

    /**
     * 重置当前分支到指定的提交。
     *
     * 根据给定的提交 ID 前缀查找提交。如果提交存在，则将工作区恢复到该提交的状态，并更新当前分支的提交 ID。
     *
     * @param commitIdPrefix 提交 ID 前缀
     */
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
     * 检出指定提交中的文件。
     *
     * 根据提交对象和文件名，恢复文件到当前工作区。如果文件在该提交中不存在，则输出错误信息。
     *
     * @param commit 提交对象
     * @param fileName 要恢复的文件名
     */
    public static void checkoutFile(Commit commit, String fileName) {
        if (!CommitUtils.isTrackedByCommit(fileName, commit)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String fileSHA1 = commit.getFileVersion().get(fileName);
        String fileContent = FileUtils.getFileContent(fileSHA1);
        FileUtils.writeCWDFile(fileName, fileContent);
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


    /**
     * 合并指定分支到当前分支。
     * 处理 fast-forward、已经合并、冲突等情况。
     *
     * 合并操作会对工作目录进行修改，因此在执行前必须满足以下条件：
     * - 工作区没有未提交的变更（包括 staged 和 removed 文件）
     * - 指定分支存在，且不是当前分支
     *
     * 合并可能导致冲突文件被写入特殊格式，用户需手动解决后提交。
     *
     * @param branchName 要合并进当前分支的目标分支名
     */
    public static void merge(String branchName) {
        // ========== 前置检查阶段 ==========

        // 检查目标分支是否存在
        if (!BranchUtils.branchExists(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        // 检查是否尝试合并自身
        if (HEAD.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        Commit currentCommit = CommitUtils.getLastCommit();

        // 如果暂存区非空，则拒绝合并
        List<String> stagedFileNames = getStagedFiles(currentCommit);
        List<String> removedFileNames = getRemovedFiles(currentCommit);
        if (!stagedFileNames.isEmpty() || !removedFileNames.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }

        // 获取目标分支的 commit 和分裂点 commit
        Commit branchCommit = BranchUtils.getBranchCommit(branchName);
        Commit splitPoint = CommitUtils.findSplitPoint(HEAD, branchName);

        // ========== 特殊情况处理 ==========

        // 情况 1：目标分支是当前分支的祖先（无需合并）
        if (splitPoint == null || CommitUtils.isSameCommit(branchCommit, splitPoint)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        // 情况 2：当前分支是目标分支的祖先（直接快进）
        if (CommitUtils.isSameCommit(currentCommit, splitPoint)) {
            String savedHEAD = HEAD;
            String[] ns = new String[] { "checkout", branchName };
            checkout(ns); // 切换到目标分支
            HEAD = savedHEAD;

            // 将当前分支快进到目标分支最新 commit
            BranchUtils.saveBranchCommit(HEAD, BranchUtils.getBranchCommitId(branchName));
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        // ========== 一般合并操作 ==========

        Set<String> splitPointFiles = splitPoint.getFileVersion().keySet();
        Set<String> currentCommitFiles = currentCommit.getFileVersion().keySet();
        Set<String> branchCommitFiles = branchCommit.getFileVersion().keySet();

        // 计算所有涉及的文件（并集）
        Set<String> allRelevantFiles = new HashSet<>(splitPointFiles);
        allRelevantFiles.addAll(currentCommitFiles);
        allRelevantFiles.addAll(branchCommitFiles);

        boolean conflictFlag = false;

        for (String fileName : allRelevantFiles) {
            boolean splitCurrentConsistent = CommitUtils.isConsistent(fileName, splitPoint, currentCommit);
            boolean splitBranchConsistent = CommitUtils.isConsistent(fileName, splitPoint, branchCommit);
            boolean branchCurrentConsistent = CommitUtils.isConsistent(fileName, currentCommit, branchCommit);

            // 情况：分支未修改，当前修改，无需处理
            if ((splitBranchConsistent && !splitCurrentConsistent) || branchCurrentConsistent) {
                continue;
            }

            // 情况：分支修改，当前未修改 -> 使用分支文件覆盖或删除
            if (!splitBranchConsistent && splitCurrentConsistent) {
                if (!branchCommitFiles.contains(fileName)) {
                    // 分支删除了文件，我们也应删除
                    if (FileUtils.isOverwritingOrDeletingCWDUntracked(fileName, currentCommit)) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    } else {
                        rm(fileName);
                    }
                } else {
                    // 分支修改了文件，我们也更新为分支版本
                    if (FileUtils.isOverwritingOrDeletingCWDUntracked(fileName, currentCommit)) {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    } else {
                        checkoutFile(branchCommit, fileName);
                        add(fileName);
                    }
                }
                continue;
            }

            // 情况：产生冲突
            if (!splitBranchConsistent && !splitCurrentConsistent && !branchCurrentConsistent) {
                conflictFlag = true;
                StringBuilder conflictedContents = new StringBuilder("<<<<<<< HEAD\n");

                String currentCommitContent = currentCommitFiles.contains(fileName)
                        ? GetFileContent(currentCommit, fileName)
                        : "";
                String branchCommitContent = branchCommitFiles.contains(fileName)
                        ? GetFileContent(branchCommit, fileName)
                        : "";

                conflictedContents.append(currentCommitContent);
                conflictedContents.append("=======\n");
                conflictedContents.append(branchCommitContent);
                conflictedContents.append(">>>>>>>\n");

                if (FileUtils.isOverwritingOrDeletingCWDUntracked(fileName, currentCommit)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    return;
                } else {
                    FileUtils.writeCWDFile(fileName, conflictedContents.toString());
                    add(fileName);
                }
            }
        }

        // ========== 创建合并提交 ==========

        // 创建 merge commit（内容已添加到暂存区）
        commit("Merged " + branchName + " into " + HEAD + ".");

        // 设置 merge commit 的第二父指针
        Commit mergeCommit = CommitUtils.getCommitByCommitId(getLastCommitId());
        mergeCommit.setSecondParentCommitId(BranchUtils.getBranchCommitId(branchName));
        CommitUtils.saveCommit(mergeCommit);

        // 更新当前分支指向 merge commit
        BranchUtils.saveBranchCommit(HEAD, CommitUtils.getCommitId(mergeCommit));

        // 若有冲突，提示用户
        if (conflictFlag) {
            System.out.println("Encountered a merge conflict.");
        }
    }
}
