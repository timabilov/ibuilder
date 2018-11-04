# ibuilder
Mini framework for building customizable command-line interface

```xml
<dependency>
    <groupId>com.github.timabilov</groupId>
    <artifactId>ibuilder</artifactId>
    <version>1.0.0</version>
</dependency>
```


Each section (and following sections) consists from `Loop`. You can create your app from with integrating loops with each other(building loops).
with Java > 1.8 it should be more easy and friendly.

```java
public class MyApplication {
    public static void main(String[] args){
      
        Loop.create("Main")
                
                .explicitCommand("<action>", new CommandCallback() {
                                                   
                       public String call(String param, Scanner s) {
                           // Your business..
                           return "My task completed";
                       }
                   }
        
                )
                .command("logs", "to explore server logs", new CommandCallback() {
                
                    public String call(String param, Scanner s) {
                        System.out.println("Some API call..");
                        System.out.println("1. Log entry ");
                        System.out.println("2. Log entry");
                        System.out.println("3. Log entry");
                        System.out.println("4. Log entry");
                        return "Log info completed";
                    }
                })
                .launch();

    }
}
```
```

---------------------------------------------------------------------------------------
iBuilder © 2018 
>You're in Main section. Available options: 
 (type the number of action) 
->1. <action>
->2. If you want to explore server logs (type: logs)
->3. If you want to exit (type: exit)
type>
---------------------------------------------------------------------------------------

```
`Loop` itself can be used instead of `CommandCallback`.  
You can nest loops with each other wherever and however you want:

```
         Loop.create()
             .command("bitcoin", "to exchange bitcoin",
                         Loop.create()
                             .command("buy", "to buy bitcoin", new CommandCallback() {
 
                                 public Object call(String param, Scanner sc) {
                                     System.out.println("Please type value you want to buy:");
                                     // your form
                                     CLIUtils.waitInput("value");
                                     return "You successfully bought {} BTC".replace("{}", "");
                                 }
                             })
                             .command("sell", "to sell bitcoin", new CommandCallback() {
 
 
                                 public Object call(String param, Scanner sc) {
                                     System.out.println("Please type value you want to sell:");
                                     // your form
                                     CLIUtils.waitInput("value");
 
                                     return "You successfully sold {} BTC".replace("{}", "value");
                                 }
                             })
 
             )
             .launch();
```
or 

```

 Loop.create()
        // explicit custom description of command, nothing more
        .explicitCommand("employees-info",
                // returns Loop
                Loop.of(
                        // your array.. overridden toString() is enough.
                        new Person("Mark", 18, "Doctor"),
                        new Person("Zuckerberg", 20, "Founder"),
                        new Person("Mike", 46, "No one")
                )

        )
        
   
   
---------------------------------------------------------------------------------------
iBuilder © 2018 
>You're in Loop section. Available options: 
 (type the number of action) 
->1. employees-info
->2. If you want to exit (type: exit)
type>1
--->You're in List loop section. Available options: 
 (type the number of action) 
---->1. Mark 18 Doctor (read only)
---->2. Zuckerberg 20 Founder (read only)
---->3. Mike 46 No one (read only)
---->4. If you want to exit (type: exit)
type>
---------------------------------------------------------------------------------------

```


Also you can pass callbacks for each list item to take some action.


If you want change the way how item renders, you can override `renderItem` method of `Loop` 

---

You can use ask user to authenticate - to proceed some API calls as major requirement. 
`Loop.create().auth(new APIAuthStrategy()).command("my-info", callback)`

any `AuthStrategy` implementation can be used as argument. You have to handle input.
For further state manipulation you can use `Loop.setState` `Loop.getState`.

```text
    @Override
    public boolean authenticate() {
        // i.e. if token is not set through constructor
        if (token == null)
            token = CLIUtils.waitInput("Your token");

        Loop.setState("token", token);
        return true;
    }
```

User will see

```text
iBuilder © 2018 
>Please login to continue 'APIAuthStrategy' ...
>You're in 'APIAuthStrategy - Login' section. Available options: 
 (type the number of action) 
->1. If you want to login
->2. If you want to register
->3. If you want to exit (type: exit)
type>
```



You can define your loop as new module and do your own customizations:

```java


public class MyApplication {



    public static void main(String[] args) throws Exception {
            
        
            // we ask github token to proceed following API's
            Loop.create("Main").header("Welcome to hell").auth(new GithubAuth())
                    .command("to see repos",
                           new GithubRepoLoop()
                    )
                    .launch();


    }


}


```

```java

import com.github.timabilov.irequest.request.Method;
import com.icmd.ibuilder.Loop;
import com.icmd.ibuilder.LoopCallback;


public class GithubRepoLoop extends Loop {

    private static String API_URL = "https://api.github.com";

    // this method will be called EACH TIME BEFORE Loop is rendered. 
    // This is not necessary, same old actions can be declared in constructor or instance initializer for instant(!) calculation.
    public void preRender(){
        list(
            // get toString() overridden Repo array from your API and map callback for each item.
            HttpCallback.api(API_URL + "/user/repos?affiliation=owner", Method.GET, getState("token"), Repo[].class),
            new LoopCallback<Repo>() {

                public Loop call(final Repo item) {
                    String starURL = API_URL + "/user/starred/" + item.getFull_name();
                    return create(item.getFull_name())
                            // Our custom handler. We overridden CommandCallback to use direct Http calls. See following. 
                            .command("to ⭐", new HttpCallback(starURL, Method.PUT, getState("token"), null))
                            .command("to remove ⭐", new HttpCallback(starURL, Method.DELETE, getState("token"), null));

                }
            }

        );

    }
}

```



```java
import com.github.timabilov.irequest.request.Method;
import com.github.timabilov.irequest.request.Request;
import com.icmd.ibuilder.CommandCallback;

import java.util.Scanner;

public class HttpCallback<E> implements CommandCallback {

    String url;
    Method method;
    String token;
    Class<E> returnType;

    HttpCallback(String url, Method method, String token, Class<E> returnType){

        this.url = url;
        this.method = method;
        this.token = token;
        this.returnType = returnType;
    }
    
    // Your api library and appropriate calls..
    public static <E> E api(String url, Method method, String token, Class<E> type){

        try {
            
            if (type == null) {
                Request.url(url, method)
                        .header("Authorization", "token " + token).send();
                return null;
            }
            return Request.url(url, method)
                    .header("Authorization", "token " + token).fetchJson(type);


        } catch (Exception e){
            System.out.println(e);
            return null;
        }
    }


    public Object call(String param, Scanner sc) {
        api(this.url, method, token, returnType);
        return "Done!";
    }
}

```


```text

iBuilder © 2018 
>Please login to continue 'GithubAuth' ...
>You're in 'GithubAuth - Login' section. Available options: 
 (type the number of action) 
->1. If you want to login
->2. If you want to exit (type: exit)
type>1
Github Token:XXXXXXXXX
Successfully logged in
>Welcome to hell
>You're in Main section. Available options: 
 (type the number of action) 
->1. If you want to see repos
->2. If you want to exit (type: exit)
type>1
--->You're in GithubRepoLoop section. Available options: 
 (type the number of action) 
---->1. ⭐ 2 useful-af-repo
        Useful repo description
        https://github.com/username/useful-af-repo
---->2. ⭐ 0 starme
        Repo description
        https://github.com/username/starme
type>2
------>You're in username/starme. Available options: 
 (type the number of action) 
------->1. If you want to ⭐
------->2. If you want to remove ⭐
------->3. If you want to exit (type: exit)
type>

```

At this example i used iRequest http library. You can use your own.

```xml
<dependency>
    <groupId>com.github.timabilov</groupId>
    <artifactId>irequest</artifactId>
    <version>1.0.3</version>
</dependency>
```   
