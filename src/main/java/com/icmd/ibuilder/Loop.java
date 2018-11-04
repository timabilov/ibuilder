package com.icmd.ibuilder;import com.icmd.ibuilder.auth.AuthStrategy;
import com.icmd.ibuilder.exception.BreakLoopException;

import java.util.*;

public class Loop implements CommandCallback{

    protected String name;
    HashMap<String, Command> commands = new LinkedHashMap<String, Command>();

    // TODO improve. custom key commands, allows us to manipulate with CLI like interpreter(with params in future?)
    HashMap<String, Command> customCommands = new LinkedHashMap<String, Command>();
    int level = 0;

    int order = 1;

    boolean interactive = true;

    AuthStrategy authStrategy;


    /**
     * Particularly for API's, just in case. To not declare your storage.
     */
    private static Storage globalStorage = new Storage();

    static Scanner s = new Scanner(System.in);
    /*
        Todo
        Chains consequence of loop execution - kind of merge.
        Loop.generateLoop
            .register..
            .register..
            .chain(
                Loop.generateLoop
                    .register
            )


     */
    Loop chained = null;

    {

    }
    static {

        System.out.println("iBuilder © 2018 ");
    }


    public Loop(){

        this.name = this.getClass().getSimpleName();

    }

    public Loop(String name, String customHeadInfo){

        this(name);
        this.customHeadInfo = customHeadInfo;

    }

    Loop(String name, boolean onlyRead){
        interactive = !onlyRead;
        this.name = name;
    }

    private Loop(String name, boolean onlyRead, String customHeadInfo){
        this.customHeadInfo = customHeadInfo;
        interactive = !onlyRead;
        this.name = name;
    }

    String customHeadInfo = null;

    private Loop(String name){

        this.name = name;
    }


    /**
     * Just  pre - hint for following methods for our fellow developers.
     *
     */
    public Loop register_(){
        // stub. verbose OPTIONAL
        return this;
    }

    public static Loop create(String name){

        return new Loop(name);
    }

    public static Loop create(){

        return new Loop();
    }

    public static Loop create(Command... commands){

        return new Loop("Loop").command(commands);
    }


    public static <E> Loop of(final List<E> items, final Callback<E, Object> forEach) {

        Loop l = new Loop("List loop").list(items, forEach);
        return l;

    }

    public static <E> Loop of(final List<E> items, final LoopCallback<E> forEach){

        return new Loop("Listed loop").list(items, forEach);

    }

    public static Loop of(final Object... args){

        Loop l = new Loop("List loop");


        for (int i = 0; i < args.length; i++){
            final int j = i;
            l.explicitCommand(args[j].toString(), new CommandCallback() {

                public String call(String param, Scanner sc) {
                    return args[j].toString();
                }
            }, true);
        }
        return l;

    }


    public static Loop of(List items){

        return of(items.toArray());

    }


    public  <E> Loop list(final List<E> items, final Callback<E, Object> forEach){


        for (int i = 0; i < items.size() ; i++){
            final int j = i;
            // name should identify operation - because of re render / duplicate issue.
            explicitCommand(i + "", items.get(j).toString(), new CommandCallback() {

                public Object call(String param, Scanner sc) {
                    return forEach.call(items.get(j));
                }
            }, false);
        }
        return this;

    }

    public <E> Loop list(final List<E> items, final LoopCallback<E> forEach){



        for (int i = 0; i < items.size() ; i++){
            final int j = i;
            boolean readOnly = forEach == null;
            // name should identify operation - because of re render / duplicate issue
            explicitCommand(i + "", items.get(j).toString(), readOnly ? null: forEach.call(items.get(j)), readOnly);

        }

        return this;

    }

    public <E> Loop list(final E[] items, final LoopCallback<E> forEach){

        return list(Arrays.asList(items), forEach);

    }



    public <E> Loop from(final Object... args){

        return of(args);

    }



    /**
        Re builds loop each time. Deferred operation.
     */
    public void preRender(){

    }

    public static Loop createWatchLoop(){
        return null; // ? like linux -f?
    }


    /**
     * Forces user to proceed authentication.
     * @param authStrategy
     * @return
     */
    public Loop auth(AuthStrategy authStrategy){

        this.authStrategy = authStrategy;

        return this;

    }

    public Loop auth(AuthStrategy authStrategy, boolean shutdownOnExit){

        this.authStrategy = authStrategy;
        if (!forceAuthenticate() && shutdownOnExit){
            System.exit(0);
        }

        return this;

    }

    public Loop command(Command c){

        commands.put(c.name, c);
        return this;
    }

    public Loop command(String name, String action, CommandCallback cc){

        command(name, action, cc, false);
        return this;
    }



    public Loop command(String name, String action, CommandCallback cc, boolean readOnly){

        Command c = Command.create(name, action);
        c.attach(cc);
        c.setInteractive(!readOnly);


        Command existing = customCommands.get(c.name);
        // we don't want to push duplicates in ordered/numbered map
        if (existing == null) {
            if (c.name != null)

                // push command reference by name one-time. After that we only update fields to not loss reference
                customCommands.put(c.name, c);
            commands.put(order++ + "", c);
        } else{
            // if we already have this command - update fields.
            existing.action = action;
            existing.isInteractive = !readOnly;
            existing.cc = cc;
        }


        return this;
    }

    public Loop command(String name, String action, Loop loop){

        loop.setLevel(level + 4);
        loop.setName(name.toUpperCase());
        return command(name, action, (CommandCallback)loop);
    }


    public Loop command(String action, CommandCallback cc){

        Command c = Command.create("" + order++, action);
        c.attach(cc);
        commands.put(c.name, c);
        return this;
    }

    public Loop explicitCommand(String description, CommandCallback cc){

        explicitCommand(description, cc, false);
        return this;
    }

    public Loop explicitCommand(String description, CommandCallback cc, boolean readOnly){

        return explicitCommand(null, description, cc, readOnly);
    }


    public Loop explicitCommand(String name, String description, CommandCallback cc, boolean readOnly){

        boolean isKey = name != null;
        int newOrder = order++;
        Command c = Command.create(!isKey ? newOrder + "": name, null, description);
        c.attach(cc);
        c.setInteractive(!readOnly);


        Command existing = customCommands.get(c.name);
        // we don't want to push duplicates in ordered/numbered map
        if (existing == null) {

            // push command reference by name one-time. After that we only update fields to not loss reference
            if (isKey)
                customCommands.put(c.name, c);

            commands.put(newOrder + "", c);
        } else{
            // if we already have this command - update fields.
            existing.description = description;
            existing.isInteractive = !readOnly;
            existing.cc = cc;
        }

        return this;
    }

    public Loop command(Command... commands){

        for (Command c: commands)
            command(c.name, c.action, c.getCommandCallback(), false);
        return this;
    }

    public <E> Loop registerListCallback(final List<E> items, final Callback<E, String> forEach){


        for (int i = 0; i < items.size() ; i++){
            final int j = i;
            command(i + "", "to edit (" + items.get(j) + ")", new CommandCallback() {

                public String call(String param, Scanner sc) {
                    return forEach.call(items.get(j));
                }
            });
        }

        return this;
    }

    public Loop commands(List<Command> cs){

        for (Command c: cs){

            commands.put(c.name, c);
        }

        return this;
    }

    private boolean forceAuthenticate(){

        // registration supported only if provider implements register method.
        // see following.
        boolean isRegistrationSupported = authStrategy.isRegistrationSupported();
        final BooleanWrapper booleanWrapper = new BooleanWrapper();
        Loop l = Loop.create().
                    command("to login", new CommandCallback() {
                        public String call(String param, Scanner sc) {

                            if (authStrategy.process()) {
                                booleanWrapper.setBool(true);
                                System.out.println("Successfully logged in");
                                throw new BreakLoopException();
                            }
                            return "Wrong username and password";
                        }

                    });

        if (isRegistrationSupported)
            l.command("to register", new CommandCallback() {

                public String call(String param, Scanner sc) {
                    System.out.println("Please provide new username and password.");
                    System.out.print("username:");
                    String username = sc.next();
                    System.out.print("password:");
                    String password = sc.next();
                    if (authStrategy.register(username, password))
                        return "Successfully registered!";
                    else {
                        return "Not registered";
                    }
                }
            });

        l.setName("'{} - Login'".replace("{}", authStrategy.getClass().getSimpleName()));
        l.header("Please login to continue '{}' ...".replace("{}", authStrategy.getClass().getSimpleName()));
        l.run(0);
        return booleanWrapper.bool;
    }


    protected String inputPrefix(String indentation){

        return "type>";
    }

    private void run(int level){


        String indentation = StringUtils.repeat('-', level) + '>';

        Scanner s = null;
        String commandRaw = "";
        if (authStrategy != null){

            if (!forceAuthenticate()) // true if user successfully logged in - else false
                return;
        }

        while (true) {
            // render calculations
            preRender();
            command("exit", "to exit" , new ExitCallback(), false);
            render(indentation);
            s = new Scanner(System.in).useDelimiter("\\s");

            System.out.print(inputPrefix(indentation));
            commandRaw = s.next(); // command name

            Command command = commands.get(commandRaw); // Command(name="1", callback => func)
            if (command == null)
                command = customCommands.get(commandRaw);


            if ( command != null && (command.isInteractive || interactive)) {

                s.useDelimiter("\\n");

                Object result;
                try {
                    String arg = "";
                    if (command.getCommandCallback() instanceof Loop)
                        arg = level + "";
                    result = command.call(arg, s);

                    if (result instanceof Loop)
                        result = ((Loop) result).call(level +"", s);
                } catch (BreakLoopException ble) {
                    break;
                }


                System.out.println(result);
                System.out.println(indentation + "Command is completed...");
            }


        }


    }


    public String renderItem(Command c, String indentation, int index){
        // FIXME indentation and index is not part of item?
        return "-" + indentation + index + ". " + c.renderCommand(indentation, index);
    }

    private void render(String indentation){


        if (customHeadInfo != null)
            System.out.println(indentation + customHeadInfo);
        System.out.println(indentation + "You're in {} section. Available options: \n (type the number of action) ".replace("{}", this.name));
        int i = 1;
        for (Command c : commands.values()) {
            String params = ""; //c.getCommandCallback() instanceof Loop ? "": "<params>";

            System.out.println(renderItem(c, indentation, i));

            i++;

        }

    }

//    public String renderCommand(String indentation, int index, String action, String name){
//
//        return "-" + indentation + index + ". If you want {} please type ---- > {} < ----\n"
//                .replace("{}", action)
//                .replace("{}", name);
//    }



    public String call(String param, Scanner sc) {
        run(Integer.parseInt(param) + 3);
        return StringUtils.repeat('-', Integer.parseInt(param) + 3) + ">" +
                "{} completed".replace("{}", this.name);
    }

    public void launch(){

        run(0);
    }


    public Loop chain(Loop l){
        this.chained = l;
        return this;
    }

    private void setLevel(int level){

        this.level = level;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Loop header(String header){

        customHeadInfo = header;
        return this;
    }

    public Loop name(String name){
        this.name = name;
        return this;
    }


    public static String setState(String key, String value){

        globalStorage.set(key, value);
        return value;
    }


    public static String getState(String key){


        return globalStorage.get(key);
    }
}
