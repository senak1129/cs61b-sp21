package gitlet;

import java.io.File;
import java.io.IOException;
import static gitlet.BranchUtils.*;

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


    public static final String MASTER_BRANCH_NAME = "master";
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");

    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    public static final File INDEX_FILE = join(GITLET_DIR, "index");

    public static final File STAGED_FILE = join(GITLET_DIR, "stage_files");

    public static boolean isInitialized (){
        return GITLET_DIR.exists();
    }

    static{
        if(isInitialized()){
            HEAD = new String(readContents(HEAD_FILE));
        }
    }

    public static void init(){
        if(isInitialized()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        if(!GITLET_DIR.exists()){
            System.out.println("Fail to create .gitlet folder in ehis work directory.");
            return;
        }

        try{
            INDEX_FILE.createNewFile();
            HEAD_FILE.createNewFile();
            STAGED_FILE.createNewFile();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        COMMITS_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        BRANCHES_DIR.mkdir();
        Commit initialCommit = CommitUtils.makeEmptyCommit("initial commit");
        String initialCommitID = CommitUtils.saveCommit(initialCommit);

        BranchUtils.saveCommitId(MASTER_BRANCH_NAME,initialCommitID);

        setHEAD(MASTER_BRANCH_NAME);
    }

    public static void setHEAD(String branchName){
        //assert BrancheUtils.branchExxists(BranchName)
        HEAD = branchName;
        writeContents(HEAD_FILE, branchName);
    }

}
