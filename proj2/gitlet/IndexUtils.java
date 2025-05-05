package gitlet;

import static gitlet.Utils.*;
import static gitlet.GitletContents.*;

import java.util.HashMap;

public class IndexUtils {
    public static HashMap<String,String> IndexMap;

    public static HashMap<String,String> StagedMap;

    static {
        if(INDEX_FILE.length() == 0){
            IndexMap = new HashMap<>();
        }else{
            IndexMap = readObject(INDEX_FILE, HashMap.class);
        }
        if(STAGED_FILE.length() == 0){
            StagedMap = new HashMap<>();
        }else{
            StagedMap = readObject(STAGED_FILE, HashMap.class);
        }
    }


    /**
     * 将当前的索引（IndexMap）和暂存文件（StagedMap）写入到文件中。
     */
    public static void saveIndex() {
        Utils.writeObject(INDEX_FILE, IndexMap);
        Utils.writeObject(STAGED_FILE, StagedMap);
    }

    /**
     * 将指定文件的内容计算其 SHA-1 哈希值，并将该文件添加到 IndexMap 和 StagedMap 中。
     *
     * @param fileName 文件名
     */
    public static void stagedFile(String fileName) {
        String fileContent = readContentsAsString(join(CWD, fileName)); // 读取文件内容
        String fileSha1 = sha1(fileContent); // 计算文件的 SHA-1 值
        IndexMap.put(fileName, fileSha1); // 将文件名和 SHA-1 映射到 IndexMap
        StagedMap.put(fileSha1, fileContent); // 将 SHA-1 和文件内容映射到 StagedMap
    }

    /**
     * 检查文件是否已被暂存。一个文件会在以下情况下被暂存：
     * - 新文件被暂存：文件不在上一次提交中，但被 add 进了 Index。
     * - 修改后的文件被暂存：文件在提交中已有，但内容修改后又 add 了新版本。
     *
     * @param fileName 文件名
     * @param commit 提交对象
     * @return 如果文件已被暂存，返回 true；否则返回 false
     */
    public static boolean isStaged(String fileName, Commit commit) {
        assert fileName != null && commit != null;
        HashMap<String, String> fileVersionMap = commit.getFileVersion();

        return (IndexMap.containsKey(fileName) && !fileVersionMap.containsKey(fileName)) // 文件在 Index 中但未出现在提交中
                || (IndexMap.containsKey(fileName) && fileVersionMap.containsKey(fileName)
                && !fileVersionMap.get(fileName).equals(IndexMap.get(fileName))); // 文件在 Index 中且内容修改
    }

    /**
     * 取消暂存指定的文件，即从 IndexMap 和 StagedMap 中删除该文件。
     *
     * @param fileName 文件名
     */
    public static void unStageFile(String fileName) {
        String FileSha1 = IndexMap.get(fileName);
        StagedMap.remove(FileSha1); // 从 StagedMap 中移除文件内容
        IndexMap.remove(fileName); // 从 IndexMap 中移除文件
    }

}
