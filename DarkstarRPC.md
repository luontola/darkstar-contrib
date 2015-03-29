# Introduction #

With this library it is possible to call methods on remote objects _almost_ as if they were local objects. You need to define an interface for your RPC service and register an instance implementing that interface. Then you can query from the remote side for that service. Two-way communication is supported - the client can call RPC services on the server and the server can call RPC services on the client.

All method calls are handled asynchronously. If the method needs to return a value, you may obtain the response (returned value or thrown exception) through a [Future](http://java.sun.com/javase/6/docs/api/java/util/concurrent/Future.html). Void methods will not send a response when they are executed, so you can use void methods to create a message passing style API.


## Notes About Server-side Implementation ##

On Darkstar's server-side, the `RpcGateway` and `Future` implementations are `ManagedObjects`. Use `ManagedReferences` as needed.

Because of Darkstar's transaction model, it is not possible nor sensible to block on the `Future.get()` method on the server-side. Always check `Future.isDone()` first and if it is not done, try again later in another task. _(TODO: It might be useful to be able to register a callback which is called when the Future becomes done.)_

The server-side Futures will remove themselves from the data store on `get()` and `cancel()`. Do not use the Future after you have retrieved its value or after you have cancelled it. Also, you should **always** call either the `get()` or the `cancel()` method of a Future, in order to free the data store space taken by the Future. (Client-side Futures are garbage collected as any Java classes, so they do not have this requirement.)

On both client and server, the resources reserved by the Futures are **not** freed automatically if the request gets no response. That's why all RPC channels should use `RELIABLE` delivery requirement, unless the return types of all RPC services on that channel are void (and even then the RPC service lookups have a return value). _(TODO: Allow using `RELIABLE` for RPC service lookups and `UNRELIABLE` for the methods of another RPC service.)_ Memory leaks (because of Futures waiting indefinitely for a response) should not happen under normal circumstances, but in case they do, a cleanup mechanism might be necessary (please submit a bug report if you experience memory leaks).


## Example Application ##

See [darkstar-rpc-example](http://code.google.com/p/darkstar-contrib/source/browse/trunk/darkstar-rpc-example/) for an example on how to use the RPC library with Darkstar. You can download its sources and pre-built binaries from the [downloads page](http://code.google.com/p/darkstar-contrib/downloads/list).

To build it from sources with Maven, use the command "mvn verify". The /target directory will contain the server application (darkstar-rpc-example-1.0-server.zip) and an executable client jar (darkstar-rpc-example-1.0-jar-with-dependencies.jar).

The test sources of [darkstar-rpc](http://code.google.com/p/darkstar-contrib/source/browse/trunk/darkstar-rpc/) may also serve as examples.


## Using Maven ##

To use the RPC library in a Maven project, add the following dependency to your pom.xml:

```
    <repositories>
        <repository>
            <id>orfjackal</id>
            <url>http://repo.orfjackal.net/maven2</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>net.orfjackal.darkstar-contrib</groupId>
            <artifactId>darkstar-rpc</artifactId>
            <version>1.0.1</version>
        </dependency>
    </dependencies>
```


## Version History ##

Version 1.0.1 (2008-08-11)
  * Fixed calling client-side RPC services from the server-side

Version 1.0 (2008-07-13)
  * Initial release


## Links ##

  * Darkstar RPC SVN: http://darkstar-contrib.googlecode.com/svn/trunk/darkstar-rpc/
  * Darkstar RPC Example SVN: http://darkstar-contrib.googlecode.com/svn/trunk/darkstar-rpc-example/
  * Original discussion thread: [Remote Procedure Calls (RPC) and Darkstar](http://www.projectdarkstar.com/component/option,com_smf/Itemid,99999999/topic,470.0)
  * Developer: [Esko Luontola](http://www.orfjackal.net/) (Jackal von ÖRF)