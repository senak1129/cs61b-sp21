package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static gitlet.BranchUtils.*;
import static gitlet.GitletConstants.*;
import static gitlet.IndexUtils.*;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {

    //HEAD 指针 指向现在的分支名字
    public static String HEAD;
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    //调用exists()方法检查是否存在文件夹
    public static boolean isInitialized (){
        return GITLET_DIR.exists();
    }

    //HEAD文件存的就是当前分支名字 为其指向即可
    static{
        if(isInitialized()){
            HEAD = new String(readContents(HEAD_FILE));
        }
    }

    public static void init(){
        //.gitlet仓库已经存在就返回不需要在创建
        if(isInitialized()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        /*if(!GITLET_DIR.exists()){
            System.out.println("Fail to create .gitlet folder in this work directory.");
            return;
        }*/
        //创建.gitlet文件夹
        GITLET_DIR.mkdirs();

        try{
            //创建对应的文件
            INDEX_FILE.createNewFile();
            HEAD_FILE.createNewFile();
            STAGED_FILE.createNewFile();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        COMMITS_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        BRANCHES_DIR.mkdir();

        //初始化一个空提交
        Commit initialCommit = CommitUtils.makeEmptyCommit("initial commit");
        //得到这个空提交的id(将commit序列化然后sha1的结果)
        String initialCommitID = CommitUtils.saveCommit(initialCommit);
        //在branch文件夹中 为master写入commitId
        BranchUtils.saveCommitId(MASTER_BRANCH_NAME,initialCommitID);
        //设置头指针 先指向master
        //为HEAD_file写入当前分支的名字
        setHEAD(MASTER_BRANCH_NAME);
    }

    public static void add(String fileName){

        //添加到暂存区 至少文件需要存在
        if(!join(CWD,fileName).exists()){
            System.out.println("File does not exist.");
        }
        else{
            //如果indexMap存有这个文件名
            //有两种情况 分别为文件修改过 和 没修改过
            if(indexMap.containsKey(fileName)){
                //取得该文件名对应的SHA1
                String targetSHA1 = indexMap.get(fileName);

                //把文件作为字符串读取转换成sha1 如果没修改过那么一定是一样的
                String targetSHA2 = sha1(readContentsAsString(join(CWD,fileName)));
                if(targetSHA1.equals(targetSHA2)){
                    System.out.println("File already exists.");
                    return;
                }
            }
            //保存index
            stageFile(fileName);
            IndexUtils.saveIndex();
        }
    }

    public static void commit(String commitMessage){
        //提交信心不能为空
        if(commitMessage.equals("")){
            System.out.println("Please enter a commit message.");
            return;
        }
        //得到上一次提交的Id
        String currentCommitId = getHeadCommitId();
        //上一次提交的信息
        Commit currentCommit = CommitUtils.readCommit(currentCommitId);
        //上一次提交的文件版本记录  文件名:sha1
        HashMap<String,String> fileVersionMap = currentCommit.getFileVersionMap();

        //index作为全局的静态map 记录文件名:sha1
        //若上一次提交后 本质没有进行任何提交 则两个map应该是相同的
        //indexMap只要有一次正确add,就会记录一个新的[FileName:sha1]
        //而fileVersionMap是一次正确提交后，给indexMap复制过来
        if(indexMap.equals(fileVersionMap)){
            System.out.println("No changes added to the commit.");
        }
        else{
            //创建新的commit
            Commit newCommit = CommitUtils.makeCommit(commitMessage,currentCommitId,indexMap);
            //每一次成功的commits都会在objects文件夹创建文件
            CommitUtils.createFileObjects(currentCommit,newCommit,stagedFileContents);
            //提交会清空暂存区
            stagedFileContents.clear();
            //清空文件staged-file ////////////////(maybe)更新文件index
            IndexUtils.saveIndex();
            //保存这个commit(在commits文件夹创建文件[id,commit对象])
            String newCommitID = CommitUtils.saveCommit(newCommit);
            //目前的分支记录一下最新提交
            BranchUtils.saveCommitId(HEAD,newCommitID);
        }
    }

    public static void status(){
        // print branches
        List<String> allBranchNames = BranchUtils.getAllBranchNames();
        System.out.println("=== Branches ===");
        for (String branchName : allBranchNames) {
            System.out.println((HEAD.equals(branchName) ? "*" : "") + branchName);
        }
        System.out.println();

        // print staged files
        Commit commit = CommitUtils.readCommit(getHeadCommitId());
        List<String> stagedFileNames = IndexUtils.getStagedFiles(commit);
        System.out.println("=== Staged Files ===");
        stagedFileNames.forEach(System.out::println);
        System.out.println();

        // print removed files
        List<String> removedFileNames = IndexUtils.getRemovedFiles(commit);
        System.out.println("=== Removed Files ===");
        removedFileNames.forEach(System.out::println);
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        List<StringBuffer> modifiedNotStagedForCommit = IndexUtils.modifiedNotStagedForCommit(commit);
        List<StringBuffer> deletedNotStagedForCommit = IndexUtils.deletedNotStagedForCommit(commit);
        modifiedNotStagedForCommit.forEach(s -> s.append(" (modified)"));
        deletedNotStagedForCommit.forEach(s -> s.append(" (deleted)"));
        modifiedNotStagedForCommit.addAll(deletedNotStagedForCommit);
        modifiedNotStagedForCommit.sort(StringBuffer::compareTo);
        modifiedNotStagedForCommit.forEach(System.out::println);
        System.out.println();

        // ("Untracked Files") is for files present in the working directory but neither staged for addition nor tracked.
        System.out.println("=== Untracked Files ===");
        List<String> untrackedFileNames = IndexUtils.getUntrackedFiles(commit);
        untrackedFileNames.forEach(System.out::println);
        System.out.println();
    }

    public static void rm(String fileName){
        Commit commit = CommitUtils.readCommit(getHeadCommitId());
        //是否被暂存:1.新add的文件但没有commit
        //2:add后或commit后又修改了的文件
        boolean staged = IndexUtils.isStaged(fileName,commit);
        //上一次提交是否包含这个文件
        boolean trackedByHeadCommit = CommitUtils.isTrackedByCommit(commit,fileName);
        if(!staged && !trackedByHeadCommit){
            System.out.println("No reason to remove the file.");
            return;
        }
        //从indexmap,stagemap删除关于fileName的相关内容
        //注意fileversionmap是没有删除的
        IndexUtils.unstageFile(fileName);
        //并且保存到对应的文件
        IndexUtils.saveIndex();
        if(trackedByHeadCommit){
            //若上一次提交了次文件 还应该在工作区删除这个文件
            restrictedDelete(join(CWD,fileName));
        }
    }

    public static void setHEAD(String branchName){
        //assert BrancheUtils.branchExxists(BranchName)
        HEAD = branchName;
        writeContents(HEAD_FILE, branchName);
    }

    public static void log(){
        //上一次提交
        Commit currentCommit = CommitUtils.readCommit(getHeadCommitId());

        List<Commit> commits = CommitUtils.commitTraceBack(currentCommit);
        for (Commit commit : commits){
            commit.printCommitInfo();
        }
    }

    //commits文件夹中所有提交
    public static void globalLog(){
        List<String> commitIdList = plainFilenamesIn(COMMITS_DIR);
        if (commitIdList == null || commitIdList.isEmpty()){
            return;
        }
        for (String commitID:commitIdList){
            CommitUtils.readCommit(commitID).printCommitInfo();
        }
    }

    //在所有提交中找到提交信息为message
    public static void find(String message){
        //plainFilenamesIn(Files_DIR)
        List<String> commitIdList = plainFilenamesIn(COMMITS_DIR);
        if (commitIdList == null || commitIdList.isEmpty()){
            return;
        }
        boolean found = false;
        for (String commitID:commitIdList){
            Commit commit = CommitUtils.readCommit(commitID);
            if(commit.getMessage().equals(message)){
                System.out.println(commitID);
                found = true;
            }
        }
        if(!found){
            System.out.println("Found no commit with that message.");
        }
    }

    public static void checkout(String...args){
        //从下标1

        //操作1
        //checkout -- [filename]
        //从上一个提交 把filename回到工作区
        if(args[2].equals("-")&&args[3].equals("-")){
            String fileName = args[4];
            //上一个提交
            Commit commit = CommitUtils.readCommit(getHeadCommitId());
            //此文件需要被上一个提交跟踪
            if(!CommitUtils.isTrackedByCommit(commit,fileName)){
                System.out.println("File does not exist in that commit.");
                return;
            }

            //读取文件sha1 内容
            String fileSHA1 = commit.getFileVersionMap().get(fileName);
            String fileContent = FileUtils.getFileContent(fileSHA1);
            //把文件拿到工作区(cwd) 没有这个文件就创建 有就覆盖
            FileUtils.writeCWDFile(fileName,fileContent);
        } else if (args[3].equals("-")&&args[4].equals("-")) {
            String fileName = args[5];
            //commitID是将commit序列化后sha1的结果
            String commitID = args[1];
            Commit commit = CommitUtils.readCommit(getHeadCommitId());

        }else{
            String branchName = args[1];
            Commit commit = CommitUtils.readCommit(getHeadCommitId());
            checkoutBranch(commit,branchName);

        }
    }

    //调整HEAD的指向
    public static void checkoutBranch(Commit commit,String BranchName){
        //分支名字不存在
        if(!BranchUtils.branchExists(BranchName)){
            System.out.println("No such branch exists.");
            return;
        }
        //分支名字等于目前分支
        if(BranchName.equals(HEAD)){
            System.out.println("No need to checkout the current branch.");
            return;
        }
        List<String>CWDFileNames = plainFilenamesIn(CWD);
        assert CWDFileNames != null;
        for(String fileName : CWDFileNames){
            //工作区不能有没被这个commit跟踪过的东西
            if(!CommitUtils.isTrackedByCommit(commit,fileName)){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        //获取名为BranchName分支的上一次提交的信息
        Commit newBranchCommit = CommitUtils.readCommit(BranchUtils.getCommitId(BranchName));
        //
        restoreCommit(newBranchCommit);

        setHEAD(BranchName);

    }

    public static void restoreCommit(Commit commit){
        //目前分支的上一次提交
        Commit currentCommit = CommitUtils.readCommit(getHeadCommitId());

        //遍历commit提交的文件信息  若某文件不被目前分支的上一次提交跟踪 NO
        for(String fileName : commit.getFileVersionMap().keySet()){
            if(FileUtils.isOverwritingOrDeletingCWDUntracked(fileName,currentCommit)){
                System.out.println(MERGE_MODIFY_UNTRACKED_WARNING);
                return;
            }
        }
    }

    public static void branch(String branchName){
        //判断这个分支名是否已经存在
        if(BranchUtils.branchExists(branchName)){
            System.out.println("A branch with that name already exists");
            return;
        }
        //在branch目录下创建[branchName,content]
        String content = getHeadCommitId();
        writeContents(join(BRANCHES_DIR,branchName),content);
    }

    public static void rm_branch(String branchName){
        if(!BranchUtils.branchExists(branchName)){
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if(branchName.equals(HEAD)){
            System.out.println("Cannot remove the current branch.");
            return;
        }
        join(BRANCHES_DIR,branchName).delete();
    }

    public static void reset(String CommitId){
        Commit commit = CommitUtils.readCommit(CommitId);
        if(commit==null){
            System.out.println("No commit with that id exist.");
            return;
        }
        String commitId = CommitUtils.getCommitId(commit);
        restoreCommit(commit);
        BranchUtils.saveCommitId(HEAD,commitId);
    }

    //得到当前分支得上一个提交的id
    public static String getHeadCommitId(){
        return BranchUtils.getCommitId(HEAD);
    }

}
