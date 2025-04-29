package gitlet;

import java.io.File;
import java.io.IOException;
import static gitlet.IndexUtils.*;
import static gitlet.Utils.*;
import static gitlet.CommitUtils.*;
import static gitlet.GitletContents.*;
public class Repository {
    public static String HEAD;

    static {
        if(IsInitial()){
            HEAD = readContentsAsString(HEAD_FILE);
        }
    }

    public static boolean IsInitial(){
        return GITLET_DIR.exists();
    }
    public static void init(){
        //如果gitlet文件夹存在
        if(GITLET_DIR .exists()){
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
        BranchUtils.SaveBranch("master",CommitId);
        BranchUtils.SetHEAD("master");

    }

    public static void add(String FileName){
        if(!join(CWD,FileName).exists()){
            System.out.println("File does not exist.");
            return;
        }
        if (IndexMap.containsKey(FileName)) {
            String FileNameSha1 = IndexMap.get(FileName);
            String FileNameSha2 = sha1(readContentsAsString(join(CWD,FileName)));
            if(FileNameSha1.equals(FileNameSha2)){
                return;
            }
        }
        IndexUtils.StagedFile(FileName);
        IndexUtils.SaveIndex();
    }

    public static void commit(String CommitMessage){

    }


}
