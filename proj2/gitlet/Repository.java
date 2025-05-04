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

    public static void add(String FileName) {
        if (!join(CWD, FileName).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        if (IndexMap.containsKey(FileName)) {
            String FileNameSha1 = IndexMap.get(FileName);
            String FileNameSha2 = sha1(readContentsAsString(join(CWD, FileName)));
            if (FileNameSha1.equals(FileNameSha2)) {
                return;
            }
        }
        IndexUtils.stagedFile(FileName);
        IndexUtils.saveIndex();
    }

    public static void commit(String CommitMessage) {
        if (CommitMessage.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        String LastCommitId = CommitUtils.getLastCommitId();
        Commit LastCommit = CommitUtils.getCommitByCommitId(LastCommitId);
        HashMap<String, String> LastFileVersion = LastCommit.getFileVersion();

        if (IndexMap.equals(LastFileVersion)) {
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit NowCommit = makeCommit(CommitMessage);
        saveCommit(NowCommit);
        Utils.writeObject(STAGED_FILE, StagedMap);
        CommitUtils.createFileObject(LastCommit, NowCommit);
        StagedMap.clear();
        String NewCommitId = getCommitId(NowCommit);
        BranchUtils.saveBranchCommit(HEAD, NewCommitId);
    }

    public static void rm(String FileName) {
        boolean IsTrackedByLastCommit = CommitUtils.isTrackedByCommit(FileName,GetLastCommit());
        boolean IsStaged = IndexUtils.isStaged(FileName,GetLastCommit());
        if (!IsStaged && !IsTrackedByLastCommit) {
            System.out.println("No reason to remove the file.");
            return;
        }
        IndexUtils.unStageFile(FileName);
        IndexUtils.saveIndex();
        if (IsTrackedByLastCommit) {
            restrictedDelete(join(CWD, FileName));
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

    public static void checkout(String...args) {
        //checkout -- [filename]
        if(args.length == 3 && args[1].equals("--")) {
            String FileName = args[2];
            Commit LastCommit = getCommitByCommitId(getLastCommitId());
            if(!LastCommit.getFileVersion().containsKey(FileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            String content = GetFileContent(LastCommit, FileName);
            Utils.writeContents(join(CWD,FileName), content);
        }else if(args.length == 4 && args[2].equals("--")) {
            //checkout [commitId] -- [filename]
            String CommitId = args[1];
            String FileName = args[3];
            Commit commit = GetCommitByCommitIdPrefix(CommitId);
            if(commit == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
            if (!commit.getFileVersion().containsKey(FileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            String content = GetFileContent(commit, FileName);
            Utils.writeContents(join(CWD,FileName), content);
        }else if(args.length == 2){
            String BranchName = args[1];
            if(BranchName.equals(HEAD)){
                System.out.println("No need to checkout the current branch.");
                return;
            }else {
                List<String>BranchList = GetAllBranches();
                if(!BranchList.contains(BranchName)) {
                    System.out.println("No such branch exists.");
                    return;
                }else{
                    List<String> CWDFileNames = plainFilenamesIn(CWD);
                    assert CWDFileNames != null;
                    for(String FileName:CWDFileNames) {
                        if(!CommitUtils.isTrackedByCommit(FileName, GetLastCommit())) {
                            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                            return;
                        }
                    }
                    RestoreCommit(GetBranchLastCommit(BranchName));
                    BranchUtils.setHEAD(BranchName);
                }
            }
        }else{
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

    public static void RestoreCommit(Commit commit) {
        Commit currentCommit = Repository.GetLastCommit();
        // pre-check
        for (String fileName : commit.getFileVersion().keySet()) {
            if (FileUtils.isOverwritingOrDeletingCWDUntracked(fileName, currentCommit)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }

        // 1. restore files to CWD
        FileUtils.restoreCommitFile(commit);

        // 2. restore indexMap
        // note: to keep consistency, checkout branch just like the new branch's commit() just happen
        // so it will restore indexMap & .gitlet/index, but stagedFiles and its file stay empty.
        IndexMap = commit.getFileVersion();
        StagedMap.clear();
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
        List<String> BranchList = GetAllBranches();
        for(String BranchName:BranchList) {
            if(BranchName.equals(HEAD)) {
                System.out.println("*"+BranchName);
            }else{
                System.out.println(BranchName);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        List<String> StagedFile = GetStagedFiles(GetLastCommit());
        StagedFile.forEach(System.out::println);
        System.out.println();

        System.out.println("=== Removed Files ===");
        List<String> RemovedFile = GetRemovedFiles(GetLastCommit());
        RemovedFile.forEach(System.out::println);
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static List<String> GetAllBranches() {
        List<String> BranchList = plainFilenamesIn(BRANCH_DIR);
        BranchList.sort(String::compareTo);
        return BranchList;
    }

    public static List<String> GetStagedFiles(Commit commit) {
        List<String> stagedFiles = new LinkedList<>();
        HashMap<String,String> fileverison = commit.getFileVersion();
        for(String FileName : IndexMap.keySet()) {
            if(!fileverison.containsKey(FileName)) {
                stagedFiles.add(FileName);//新加
            }else{
                String ContentSha1 = fileverison.get(FileName);
                if (!IndexMap.get(FileName).equals(ContentSha1)) {
                    stagedFiles.add(FileName);
                }
            }
        }
        stagedFiles.sort(String::compareTo);
        return stagedFiles;
    }

    public static void MakeNewBranch(String BranchName) {
        List<String> BranchList = GetAllBranches();
        if(BranchList.contains(BranchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        writeContents(join(BRANCH_DIR,BranchName), getLastCommitId());
    }

    public static void RemoveBranch(String BranchName) {
        List<String> BranchList = GetAllBranches();
        if(!BranchList.contains(BranchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if(BranchName.equals(HEAD)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        join(BRANCH_DIR,BranchName).delete();
    }

    public static void Reset(String commitIdPrefix) {
        Commit commit = GetCommitByCommitIdPrefix(commitIdPrefix);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        String commitId = CommitUtils.getCommitId(commit);
        RestoreCommit(commit);
        BranchUtils.saveBranchCommit(HEAD, commitId);
    }


    public static List<String> GetRemovedFiles(Commit commit) {
        List<String> RemovedFiles = new LinkedList<>();
        HashMap<String,String> fileverison = commit.getFileVersion();
        for(String FileName : fileverison.keySet()) {
            if(!IndexMap.containsKey(FileName)) {
                RemovedFiles.add(FileName);
            }
        }
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
        List<String> stagedFileNames = GetStagedFiles(lastCommit);
        List<String> removedFileNames = GetRemovedFiles(lastCommit);
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
