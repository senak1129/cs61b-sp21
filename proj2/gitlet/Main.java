package gitlet;
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.init();
                break;
            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.add(args[1]);
                break;
            case "commit":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.rm(args[1]);
                break;
            case "log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.log();
                break;
            case "global-log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.globalLog();
                break;
            case "find":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.find(args[1]);
                break;
            case "status":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.status();
                break;
            case "checkout":
                if (args.length < 2 || args.length > 4) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.checkout(args);
                break;
            case "branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.makeNewBranch(args[1]);
                break;
            case "rm-branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.reset(args[1]);
                break;
            case "merge":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!Repository.IsInitial()){
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }
}
