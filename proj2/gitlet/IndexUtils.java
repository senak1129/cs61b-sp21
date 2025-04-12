package gitlet;

import static gitlet.Utils.*;

import java.io.File;
import java.util.HashMap;
import static gitlet.GitletConstants.*;

public class IndexUtils {
    //FileName Sha1
    public static HashMap<String,String> indexMap;

    //sha1 contest
    public static HashMap<String,String> stagedFileContents;

    static{
        if(Repository.isInitialized()){
            indexMap = new HashMap<>();
            stagedFileContents = new HashMap<>();
        }
    }

    public static void saveIndex(){
        //把indexmap写入index
        Utils.writeObject(INDEX_FILE, indexMap);
        //把stagedFileContents 写入 STAGED_FILE
        Utils.writeObject(STAGED_FILE, stagedFileContents);
    }

    public static void stageFile(String fileName){

        //按字符串读取CWD目录下的fileName
        String fileContents = readContentsAsString(join(CWD,fileName));
        //把内容转换成Sha1
        String fileSHA1 = sha1(fileContents);

        //添加进去map
        indexMap.put(fileName, fileSHA1);
        stagedFileContents.put(fileSHA1, fileContents);
    }

   /* public static HashMap<String,String> readIndex(){
        return hashMapRead(INDEX_FILE);
    }

    public static HashMap<String,String> readStagedContents(){
        return hashMapRead(STAGED_FILE);
    }

    public static HashMap<String,String> hashMapRead(File file){
        if(file.length() == 0){
            return new HashMap<>();
        }

        HashMap<String,String> hashMap = Utils.readObject(file, HashMap.class);

        return hashMap != null ? hashMap : new HashMap<>();
    }*/

}
