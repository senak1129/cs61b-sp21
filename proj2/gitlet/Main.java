package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.add(args[1]);
                break;
            case "commit":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.rm(args[1]);
                break;
            case "log":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.log();
                break;
            case "global-log":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.globalLog();
                break;
            case "find":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.find(args[1]);
                break;
            case "status":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.status();
                break;
            case "checkout":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.checkout(args);
                break;
            case "branch":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.MakeNewBranch(args[1]);
                break;
            case "rm-branch":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.RemoveBranch(args[1]);
                break;
            case "reset":
                if(!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.Reset(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }
}
