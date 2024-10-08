package gitlet;

import java.io.File;

import static gitlet.Repository.configDIR;
import static gitlet.Repository.pull;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if(args.length == 0){
            System.out.println("Please enter a commend.");
            System.exit(0);
        }
        String firstArg = args[0];
        configDIR(new File(System.getProperty("user.dir")));
        switch(firstArg) {
            case "init":
                validArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.add(args[1]);
                break;
            case "commit":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.commit(args[1]);
                break;
            case "rm":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.rm(args[1]);
                break;
            case "log":
                validArgs(args, 1);
                Repository.checkIfInitialized();
                Repository.log();
                break;
            case "checkout":
                Repository.checkIfInitialized();
                switch (args.length){
                    case 2:/* * checkout [branch name] */
                        Repository.checkoutBranch(args[1]);
                        break;
                    case 3:/* * checkout -- [file name] */
                        if(!args[1].equals("--")){
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        Repository.checkout(args[2]);
                        break;
                    case 4:/* * checkout [commit id] -- [file name] */
                        if(!args[2].equals("--")){
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        Repository.checkout(args[1], args[3]);
                        break;
                    default:
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                }
                break;
            case "status":
                validArgs(args, 1);
                Repository.checkIfInitialized();
                Repository.status();
                break;
            case "global-log":
                validArgs(args, 1);
                Repository.checkIfInitialized();
                Repository.global_log();
                break;
            case "find":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.find(args[1]);
                break;
            case "branch":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.rm_branch(args[1]);
                break;
            case "reset":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.reset(args[1]);
                break;
            case "merge":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.merge(args[1]);
                break;
            case "add-remote":
                validArgs(args, 3);
                Repository.checkIfInitialized();
                Repository.addRemote(args[1], args[2]);
                break;
            case "rm-remote":
                validArgs(args, 2);
                Repository.checkIfInitialized();
                Repository.rmRemote(args[1]);
                break;
            case "push":
                validArgs(args, 3);
                Repository.checkIfInitialized();
                Repository.push(args[1], args[2]);
                break;
            case "fetch":
                validArgs(args, 3);
                Repository.checkIfInitialized();
                Repository.fetch(args[1], args[2]);
                break;
            case "pull":
                validArgs(args, 3);
                Repository.checkIfInitialized();
                pull(args[1], args[2]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
    private static void validArgs(String[] args, int num){
        if(args.length != num){
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
