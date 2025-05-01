package gitlet;

import java.io.File;

import static gitlet.Utils.join;

public class GitletContents {

    public static final File CWD = new File(System.getProperty("user.dir"));

    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File BRANCH_DIR = join(GITLET_DIR, "branches");

    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    public static final File STAGED_FILE = join(GITLET_DIR, "staged");

    public static final File INDEX_FILE = join(GITLET_DIR, "index");

    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

}
