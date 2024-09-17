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
            // TODO: FILL THE REST IN
        }
    }
    private static void validArgs(String[] args, int num){
        if(args.length != num){
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
