package gitlet;

import java.io.File;

import static gitlet.Utils.join;

public class GitletConstants {

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
}
