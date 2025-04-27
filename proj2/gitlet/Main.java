package gitlet;
import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Must need a least one argument");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                if(args.length != 1){
                    System.err.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if(args.length != 2){
                    System.err.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "status":
                if(args.length != 1){
                    System.err.println("Incorrect operands.");
                }
                Repository.status();
                break;
            case "commit":
                if(args.length != 2){
                    System.err.println("Incorrect operands.");
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                if(args.length != 2){
                    System.err.println("Incorrect operands.");
                }
                Repository.rm(args[1]);
                break;
            case "log":
                if(args.length != 1){
                    System.err.println("Incorrect operands.");
                }
                Repository.log();
                break;
            case "global-log":
                if(args.length != 1){
                    System.err.println("Incorrect operands.");
                }
                Repository.globalLog();
            case "find":
                if(args.length != 2){
                    System.err.println("Incorrect operands.");
                }
                Repository.find(args[1]);
                break;
            case "checkout":
                if(args.length <= 1 || args.length >= 5 ){
                    System.err.println("Incorrect operands.");
                }
                Repository.checkout(args);
                break;
            case "branch":
                if(args.length != 2){
                    System.err.println("Incorrect operands.");
                }
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                if(args.length != 2){
                    System.err.println("Incorrect operands.");
                }
                break;
            case "merge":
                break;
            default:
                System.out.println("No command with that name exists.");

        }
    }
}
