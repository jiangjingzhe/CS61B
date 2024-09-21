package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if(args == null){
            System.out.println("Please enter a commend.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                validArgs(args, 1);
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
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
                Repository repo = new Repository();
                switch (args.length){
                    case 2:/* * checkout [branch name] */
                        repo.checkoutBranch(args[1]);
                        break;
                    case 3:/* * checkout -- [file name] */
                        if(!args[1].equals("--")){
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        repo.checkout(args[2]);
                        break;
                    case 4:/* * checkout [commit id] -- [file name] */
                        if(!args[2].equals("--")){
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        repo.checkout(args[1], args[3]);
                        break;
                    default:
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                }
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
