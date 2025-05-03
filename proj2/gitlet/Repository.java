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
        Commit EmptyCommit = MakeEmptyCommit("initial commit");
        SaveCommit(EmptyCommit);
        String CommitId = GetCommitId(EmptyCommit);
        BranchUtils.SaveBranchCommit("master", CommitId);
        BranchUtils.SetHEAD("master");

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
        IndexUtils.StagedFile(FileName);
        IndexUtils.SaveIndex();
    }

    public static void commit(String CommitMessage) {
        if (CommitMessage.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        String LastCommitId = CommitUtils.GetLastCommitId();
        Commit LastCommit = CommitUtils.GetCommitByCommitId(LastCommitId);
        HashMap<String, String> LastFileVersion = LastCommit.GetFileVersion();

        if (IndexMap.equals(LastFileVersion)) {
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit NowCommit = MakeCommit(CommitMessage);
        SaveCommit(NowCommit);
        Utils.writeObject(STAGED_FILE, StagedMap);
        CommitUtils.CreateFileObject(LastCommit, NowCommit);
        StagedMap.clear();
        String NewCommitId = GetCommitId(NowCommit);
        BranchUtils.SaveBranchCommit(HEAD, NewCommitId);
    }

    public static void rm(String FileName) {
        boolean IsTrackedByLastCommit = IndexUtils.IsTrackedByCommit(FileName,GetLastCommit());
        boolean IsStaged = IndexUtils.IsStaged(FileName,GetLastCommit());
        if (!IsStaged && !IsTrackedByLastCommit) {
            System.out.println("No reason to remove the file.");
            return;
        }
        IndexUtils.UnStageFile(FileName);
        IndexUtils.SaveIndex();
        if (IsTrackedByLastCommit) {
            restrictedDelete(join(CWD, FileName));
        }
    }

    public static void log() {
        Commit LastCommit = GetCommitByCommitId(GetLastCommitId());
        while (LastCommit != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
            System.out.println("===");
            System.out.println("commit " + GetCommitId(LastCommit));
            System.out.println("Date: " + sdf.format(LastCommit.GetDate()));
            System.out.println(LastCommit.GetMessage());
            System.out.println();
            LastCommit = GetCommitByCommitId(LastCommit.GetFirstParentCommitId());
        }
    }

    public static void globalLog() {
        List<String> commitIdList = plainFilenamesIn(COMMITS_DIR);
        if (commitIdList == null || commitIdList.isEmpty()) {
            return;
        }
        for (String commitId : commitIdList) {
            Commit commit = CommitUtils.GetCommitByCommitId(commitId);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
            System.out.println("===");
            System.out.println("commit " + commitId);
            System.out.println("Date: " + sdf.format(commit.GetDate()));
            System.out.println(commit.GetMessage());
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
            Commit commit = CommitUtils.GetCommitByCommitId(commitId);
            if(commit.GetMessage().equals(message)) {
                System.out.println(commitId);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static String GetFileContent(Commit commit, String FileName) {
        String FileSha1 = commit.GetFileVersion().get(FileName);
        return readContentsAsString(join(OBJECTS_DIR, FileSha1));
    }

    public static void checkout(String[] args) {
        //checkout -- [filename]
        if(args.length == 3 && args[1].equals("--")) {
            String FileName = args[2];
            Commit LastCommit = GetCommitByCommitId(GetLastCommitId());
            if(!LastCommit.GetFileVersion().containsKey(FileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            String content = GetFileContent(LastCommit, FileName);
            Utils.writeContents(join(CWD,FileName), content);
        }else if(args.length == 4 && args[2].equals("--")) {
            //checkout [commitId] -- [filename]
            String CommitId = args[1];
            String FileName = args[3];
            Commit commit = GetCommitByCommitId(CommitId);
            if(commit == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
            if (!commit.GetFileVersion().containsKey(FileName)) {
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
                        if(!IndexUtils.IsTrackedByCommit(FileName, GetLastCommit())) {
                            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                            return;
                        }
                    }
                    RestoreCommit(GetBranchLastCommit(BranchName));
                    BranchUtils.SetHEAD(BranchName);
                }
            }
        }else{
            System.out.println("Incorrect operands.");
        }
    }



    public static void RestoreCommit(Commit TargetCommit) {
        FileUtils.RestoreCommitFile(TargetCommit);
        IndexMap = new HashMap<>(TargetCommit.GetFileVersion());
        StagedMap.clear();
        IndexUtils.SaveIndex();
    }

    public static Commit GetBranchLastCommit(String BranchName) {
        String CommitId = readContentsAsString(join(BRANCH_DIR,BranchName));
        return CommitUtils.GetCommitByCommitId(CommitId);
    }

    public static Commit GetLastCommit() {
        return CommitUtils.GetCommitByCommitId(GetLastCommitId());
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
        HashMap<String,String> fileverison = commit.GetFileVersion();
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
        writeContents(join(BRANCH_DIR,BranchName),GetLastCommitId());
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

    public static void Reset(String CommitId) {
        Commit commit = GetCommitByCommitId(CommitId);
        if(commit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        RestoreCommit(commit);
        BranchUtils.SaveBranchCommit(HEAD,CommitId);
    }

    public static List<String> GetRemovedFiles(Commit commit) {
        List<String> RemovedFiles = new LinkedList<>();
        HashMap<String,String> fileverison = commit.GetFileVersion();
        for(String FileName : fileverison.keySet()) {
            if(!IndexMap.containsKey(FileName)) {
                RemovedFiles.add(FileName);
            }
        }
        RemovedFiles.sort(String::compareTo);
        return RemovedFiles;
    }
}
