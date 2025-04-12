package gitlet;

import static gitlet.Utils.*;
import java.util.HashMap;
import static gitlet.GitletConstants.*;

public class IndexUtils {
    public static HashMap<String,String> indexMap;
    public static HashMap<String,String> stagedFileContents;

    public static void saveIndex(){
        Utils.writeContents(INDEX_FILE, indexMap);
        Utils.writeContents(STAGED_FILE, stagedFileContents);
    }

    public static void stageFile(String fileName){
        String fileContents = readContentsAsString(join(CWD,fileName));
        String fileSHA1 = sha1(fileContents);
        indexMap.put(fileSHA1, fileContents);
        stagedFileContents.put(fileSHA1, fileContents);
    }

}
