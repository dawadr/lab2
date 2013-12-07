### Description

In this assignment you will learn:
•*how to implement a replication mechanism based on Sockets
•*cryptographically securing the communication between clients and Proxy
•*asymmetric and symmetric cryptographic algorithms
•*how to verify message integrity
•*using the Java Cryptography Architecture (JCA) API
•*how to use RMI (Remote Method Invocation)
•*how to deal with high loads
#### 
Overview

Lab 2 is divided into the following parts:

**Stage 1** (5 points): Your solution for Lab 1 provides a way to download files made available through our fileserver network. To make things more interesting, a file is not uploaded to every single fileserver, so our fileservers won't be fully replicated any longer. Obvioulsy, we will also need to adapt our implementations of the `!list` and `!download` command to fit this situation. **Gifford's scheme** shall be used to keep the access to the replicas consistent.

**Stage 2** (10 points): This stage secures the communication between the client and the Proxy by implementing a secure channel and mutual authentication using public-key cryptography. In our case, the secure channel will protect both parties against interception and fabrication of messages. Note that the common definition of a secure channel additionally implies a protection against modification. The client communication will remain vulnerable in this regard; however, Stage 3 will show how to address this issue concerning the communication between the proxy and the fileservers.

The first part of setting up our secure channel is to mutually authenticate each party (i.e., every party needs to prove its identity). In this assignment we will authenticate using the well-known challenge-response protocol (which is also discussed in the lecture). To subsequently ensure confidentiality of the messages after the authentication, we will use secret-key cryptography by means of session keys. Note that the session key is shared only between one specific client and the Proxy (and not with other clients). It is generated and exchanged during the authentication phase (i.e., if and only if the authentication is successful, both parties will know how to continue communication securely). This approach ensures that the user and the Proxy are having a confidential communication and that each party is who it claims to be.

**Stage 3** (5 points): This stage will show you how to verify that a message reaches its receiver unmodified using the JCA. For the sake of simplicity, we will add this feature solely to the otherwise unsecured communication between the Proxy and the fileservers. Here, communication is not protected against interception. By using a Message Authentication Code (MAC) and appending it to each message, the receiver can check whether the message has been modified on the way through the channel.

**Stage 4** (10 points): In this stage you will extend your Proxy and Client implementation to add a management component that facilitates RMI. The Management Component provides several operations and convenience methods that can be used by the Client.

**Stage 5** (5 points): To demonstrate that your file download system is able to handle high loads, you will implement a testing framework that simulates realistic user workloads. The testing framework serves as a benchmark for performance characteristics, and it may help in determining multi-threading/synchronization issues (e.g., deadlocks, memory leaks etc.).


--------------------------------------------------------------------------------* * * *

#### 
Installation and Static Registration of the Bouncy Castle Provider

For the second and third part we will use the Bouncy Castle library as a provider for the Java Cryptography Extension (JCE) API, which is part of JCA. The Bouncy Castle provider (JDK 1.6 version) is already part of the provided template. Please stick to the provided version as this is the one used in our lab environment.

The provider is configured as part of your Java environment by adding an entry to the `java.security` properties file (found in`$JAVA_HOME/jre/lib/security/java.security`, where `$JAVA_HOME` is the location of your JDK distribution). You will find detailed instructions in the file, but basically it comes down to adding this line (but you may need to move all other providers one level of preference up):

`security.provider.1 = org.bouncycastle.jce.provider.BouncyCastleProvider`

Where you actually put the jar file is mostly up to you, but the best place to have it is in `$JAVA_HOME/jre/lib/ext`.

The installation of a custom provider is explained in the Java Cryptography Architecture (JCA) Reference Guide in detail.

**Note:** If you get "java.lang.SecurityException: Unsupported keysize or algorithm parameters" or "java.security.InvalidKeyException: Illegal key size" exception while using the Bouncy Castle library, then check this hint.

--------------------------------------------------------------------------------* * * *

#### 
Project Template

To help you start with the assignment, we provide a template with updated and new resources that are needed for this assignment. The project contains the following directories: `files, keys, libs , src/main/resources`. The `lib` directory contains the Bouncy Castle library. The `keys` directory contains all the keys required to test your implementation and the `src/main/resources` directory contains the updated .properties files.

Each private key used in the client-proxy communication is encrypted with same password: **12345**. 

**Update 2013-12-01: **The private keys used in the client-proxy communication are encrypted with following password for the respective participant: 
•****alice=12345***
•****bill=23456***
•****proxy=12345 ***

The secret key used in the fileserver network is not password protected at all.

In the `src/main/resources` directory you will find configuration files, which are used by the client, fileserver and proxy applications, respectively. In these files you only have to adjust the port properties according to the Lab Port Policy. All other configuration properties have meaningful default values and you do not need to adapt them. In particular, do not change the names of the properties defined!

The project also contains a new ant build file (build.xml), in which you only have to adjust the class names. Note that it's absolutely required that we are able to start your programs with these predefined commands!

--------------------------------------------------------------------------------* * * *

## 
Stage 1 - Replication and Consistency (5 points)

Gifford's scheme shall be used to keep the access to the replicas consistent. To support this, version numbers shall be assigned to the downloadable files (exactly one version number for one file). Gifford's scheme works as follows: To read a file that has N replicas a so-called read quorum is assembled, i.e. a subset of NR fileservers among these N replicas. To modify a file a write quorum of NW fileservers is built in the same way as the read quorum. In our case N represents the number of replicas is the number of fileservers.

The two numeric values NR and NW need to satisfy two constraints:
1.1.**NR + NW > N**
2.2.**NW > N/2**

Depending on N there might be several different possibilities to built the two quorums, but as long as the constraints are satisfied they are all correct. Although don't just choose N for the quorums as this nullifies Gifford's scheme.

The following figure gives an example of Gifford's scheme. The first three fileservers have been selected as the write quorum, the last two fileservers have been selected as a read quorum.

Gifford's Scheme 
**Figure 1** 


To support the upload of files, the client uses the interactive command already used for Lab 1 (!upload). If the above constraints are satisfied then at least one fileserver of the read quorum will lead to recent data. It is the Proxy's task to implement the algorithm correctly.

When receiving an upload request, the Proxy asks NR fileservers for the current version number of the respective file. Choose the NR fileservers with the lowest usage. Then introduce a versioning mechanism: the version of a file depends on the amount of uploads that 'changed' the file. Accordingly, an already existing file has version 0 in the beginning; a file that has just been uploaded and didn't exist before has version 1. Another upload of a file with the same name will increase the version to an amount of 2 and so on. Fileservers do not need to check if the file has been changed or is different from the one that existed before. That is, the used versioning approach is not sophisticated at all and should be straightforward in its implementation.

After consulting NR fileservers, the Proxy now knows the most recent version of the file (the highest version returned). By adding this version + 1 to the request, it can now initialize the actual upload to NW fileservers. Again, these fileservers are the NW fileservers with the lowest usage. After this, it updates the usage statistics of these fileservers (i.e., the filesize is added to the current values) and informs the client of the successful operation.

Fileservers simply need to store the uploaded files in their file directory``. Moreover, since each fileserver keeps track of the current version of each hosted file, it has to update the version to the value sent by the Proxy. It is enough to keep version information in memory; that is, versions do not need to survive fileserver restarts.

You will also need to adapt your former solution regarding the `!download` and `!list` commands: If the Proxy receives a download request, it asks NR fileservers (the NR with the lowest usage) for the current version of this file and obtains it from the one with the highest version. If multiple fileservers provide this version, the fileserver with the lowest usage is chosen. Do not forget to update the usage of the chosen fileserver afterwards. Concerning the `!list` command, contact all fileservers and send a merged list back to the client.

Concerning the implementation of Gifford's scheme, you may assume that no additional fileservers will join the network or old ones disappear after the first client has made an upload. However, make sure that your implementation is reasonably stable, in particular make sure that correct synchronisation of concurrent client requests is ensured.


--------------------------------------------------------------------------------* * * *

## 
Stage 2 - Secure Channel (10 points)

The first stage of establishing a secure channel is a mutual authentication. We will authenticate a client and the Proxy using public-key cryptography. This type of authentication is explained in the book *Distributed Systems: Principles and Paradigms (2nd edition)*, page 404, Figure 9-19. However, we also describe the principles in detail below.

Please note that in this assignment you are not allowed to use `javax.crypto.CipherInputStream` and `javax.crypto.CipherOutpurStream`. Instead you should encrypt and decrypt all messages yourself.

In order to get rid of any special characters which are unsuitable for transmission as text you should **encode your already encrypted messages using Base64 before transmission** (see code snippets below). However, do not forget to Base64 decode the messages after receiving, otherwise the decryption of the messages will of course fail.

We highly recommend to hide the security aspects from the rest of your application as much as possible. Note that plain Sockets or encrypted channels have many communalities, e.g., the are both used to send and receive messages. Therefore, it may be a good idea to define a common interface that abstracts the details of the underlying implementation away. You should use the Decorator pattern to add further functionalities step by step: For example, you could write a `TCPChannel `that implements your `Channel` interface and provides ways to send and receive Objects over a Socket. Next, you could write a `Base64Channel` class that also implements your `Channel` interface and encodes or decodes Objects using Base64 before passing them to the underlying `TCPChannel`. Following this approach may simplify your work in Stage 2 and 3.
### 
Authentication Algorithm

**Note:** **You should implement the authentication algorithm (including the syntax of messages) exactly as described here!** Failure to do so may result in losing points. The reason for this is that we will probably test your assignment using a modified client which relies on the protocol being **exactly** as described. Do yourself and the tutors a favor and implement the protocol as described.


Stage 2 - Channel creation 
**Figure 2** 


The authentication algorithm consists of sending three messages:

**1st Message:** The first message is a `!login` request (analogous to the `!login` command from Lab 1). The syntax of the message is: `!login <username> <client-challenge>`. This message is sent by the client and is encrypted using RSA initialized with the Proxy's public key.
•*The `username` is the name of the user who wants to authenticate and log in (e.g. alice) and is passed to the Client by the user using the `!login <username> `command.
•*The `client-challenge` is a 32 byte random number, which the client generates freshly for each request (see code snippets to learn how to generate a secure random number). Encode the challenge separately using Base64 before encrypting the overall message.
•*Initialize the RSA cipher with the `"RSA/NONE/OAEPWithSHA256AndMGF1Padding"` algorithm.
•*As stated above, do not forget to encode your overall ciphertext using Base64 before sending it to the Proxy.

**2nd Message:** The second message is sent by the Proxy and is encrypted using RSA initialized with the user's public key. Its syntax is: `!ok <client-challenge> <proxy-challenge> <secret-key> <iv-parameter>`.
•*The `client-challenge` is the challenge that client sent in the first message. This proves to the client that the Proxy successfully decrypted the first message (i.e., it proves the Proxy's identity).
•*The `proxy-challenge` is also a 32 byte random number generated freshly for each request by the Proxy.
•*The last two arguments are our session key. The first part is a random 256 bit secret key and the second is a random 16 byte initialization vector (IV) parameter.
•***Every argument** has to be encoded using Base64 before encrypting the overall message!
•*The ciphertext is sent Base64 encoded again.

**3rd Message:** The third message is just the `proxy-challenge` from the second message. This proves to the Proxy that the client successfully decrypted the second message (i.e., it further proves the client's identity). This message is the first message sent using AES encryption.
•*Initialize the AES cipher using the `<secret-key>` and the `<iv-parameter>` from the second message. Details about these parameters are out of scope of this lab - you will learn about them in a Cryptography lecture.
•*Use the `"AES/CTR/NoPadding"` algorithm for the AES cipher.
•*Again, encrypt and encode the message before sending it.

The final end product of the authentication is an AES-encrypted secure channel between the client and the Proxy, which is used to encrypt all future communication. You MAY NOT send any message unencrypted (between a client and the Proxy) in this assignment. You MAY NOT send any messages besides the first two authentication messages (as described above) using the RSA encryption (i.e., the RSA encryption is strictly for the authentication part).

To generate a random 32 byte numbers to be used for challenges and random 16 bytes numbers to be used for IV parameter, you should use the`java.security.SecureRandom` class and its `nextBytes()` method as shown in the Hints & Tricky Parts section. Base64 encodinging them is required because this method could return bytes which are unsuited to be inserted in a text message. Same holds true for random secret keys in the AES algorithm (see the Hints & Tricky Parts section on how to generate them). That is: Always encode your challenges, IV parameters and secret keys separately in your message using Base64. This message is then encrypted and gets Base64-encoded again before sending it.
### 
Client Application Behavior

When a client application is started, it doesn't know which user will try to log in. Therefore the client application will need to process the `!login` request before it is sent to the Proxy to find out which user is trying to log in and read his private key (used for decrypting the `!ok` message). Make sure a private key for this user does exist, otherwise, print an exception. The processing of the `!login` command also includes appending the `client-challenge`.

There are two new configuration properties in the `client.properties` file, which the client application will need for the authentication phase:
•*the `keys.dir` property denotes the directory where to look for the user's private key (named `<username>.pem`),
•*and the `proxy.key` property defines the file from where to read Proxy's public key.
### 
Proxy Application Behavior

The Proxy application should read its private key during the startup time. The user's public keys are read when the Proxy receives a log in request. The user is said to be online when the authentication phase is successfully completed.

There are also two configuration properties in the `proxy.properties` file, which the Proxy will need for the authentication phase:
•*the `keys.dir` property denotes a directory where to look for user's public keys (named `<username>.pub.pem`),
•*and the `key` property telling where to read the Proxy's private key.



--------------------------------------------------------------------------------* * * *

## 
Stage 3 - Message Integrity (5 points)

**Note:** You should implement the syntax for private messages exactly as described here, for the same reasons as discussed above. Furthermore, make sure that the UDP messages your fileservers send to the Proxy actually adhere to the `!alive <tcpPort>` format (this has to do with the way we will test this stage during your interview).

In this part of the assignment we will add an integrity check for the TCP messages exhanged between the Proxy and the Fileservers. However, the communication won't get encrypted - the implementation will only make sure that a third party cannot tamper with a message unnoticed. To this, it relies on Message Authentication Codes (MACs).

Whenever the Proxy sends a TCP request to a fileserver and whenever a fileserver responds, the application needs to compute a HMAC (a hash MAC). To generate such a HMAC you should use SHA256 hashing (`"HmacSHA256"`) initialized with a secret key shared between the Proxy and all fileservers in the network. See the Hints & Tricky Parts on how to read in the shared secret key or create and initialize HMACs. After the HMAC is generated, it should be encoded using Base64. Prepend the original message with the HMAC (e.g. <HMAC> !upload <filename> <version> <filecontent>) and Base64 encode the resulting message before sending it (**update 2013-11-25**: no need to Base64 encode the entire message, just make sure that the HMAC is transmitted as a String, i.e., Base64 encoded).


To verify the integrity of the message, the receiver generates a new HMAC of the received plaintext to compare it with the received one. In case of a mismatch, the behaviour of a fileserver should be as follows: The respective message is printed to the standard output and the Proxy is informed about the tampering, using the same channel the message was received. When the Proxy receives this report or notices a message from a fileserver itself was changed, the Proxy repeats the respective operation. Like a fileserver, whenever it receives unverified messages, the Proxy has to print their content to the console.

Note that the described behaviour now requires the fileservers to respond to every request that arrives from the Proxy. Nevertheless, stick to the design of Lab1 where the TCP connection was closed after a single request/response cycle, even though an unverified message has been received.

In order to read the secret key both `proxy.properties` and `fs1.properties` (respectively `fs2.properties`) define a `hmac.key` property, which denotes from where to read the secret key.



--------------------------------------------------------------------------------* * * *

## 
Stage 4 - RMI (10 points)
### 
Description

In this assignment you will learn:
•*the basics of a simple distributed object technology (RMI)
•*how to bind and lookup objects with a naming service
•*how to implement callbacks with RMI
#### 
Overview

In this stage you will extend your Proxy of the first lab by adding a management component. This component will provide several methods that can be used by the client to e.g. retrieve statistics about the replication mechanism. The following figure depicts the overall updated (simplified) architecture of our system.

Lab2 RMI overview 
**Figure 3** 

In Figure 3 you will see that the Proxy now has two components, which are the Common-Component (left-hand side) and the Management-Component (right-hand side). The Common-Component, which was already implemented by you as part of the previous lab, uses TCP for the communication with clients. In contrast the Management-Component represents the new part of the Proxy and communicates with the Client via RMI, where in the Figure the doted lines represent the communication of Client and Proxy via RMI. In order to address these changes you have to extend both your Proxy and Client implementation from Lab 1.

Overall the Proxy - Management Component provides the following operations:
1.1.An operation to request the currently used Read-Quorum (can be omitted if Stage 1 was not solved)
2.2.An operation to request the currently used Write-Quorum (can be omitted if Stage 1 was not solved)
3.3.An operation to request a list of the Top Three of all downloaded files. Where the top three is an ordered list of files that got downloaded the most.
4.4.An operation that allows a user to subscribe for a given file and get a notification, when the file got downloaded for a given number of times. In order to implement this behavior the Client creates a remote callback object and adds this object when invoking the method. The Management Component stores this callback object and whenever the defined number of downloads is reached the Client gets notified by using its callback.
5.5.An operation that allows a user to gather the public key of the Proxy.
6.6.An operation that allows a user to transmit his own public key to the Proxy.

Have a look at the Hints & Tricky Parts section, in case you should face any difficulties!
#### 
Proxy Updates


##### 
Arguments

Additionally to the `proxy.properties` the Proxy should now also read the `mc.properties` file, which defines the following properties:
•*`binding.name`: the name the management component shall use to bind its remote reference in the RMI registry.
•*`proxy.host`: the host name or IP address where the Proxy is running.
•*`proxy.rmi.port`: the TCP port where the Proxy is listening for connections.
•*`keys.dir`: the directory where the to look and store keys.
##### 
Implementation Details

RMI uses the `java.rmi.registry.Registry` service to enable applications to retrieve remote objects. This service can be used to reduce coupling between clients (looking up) and servers (binding): the real location of the server object becomes transparent. In our case, the management component will use the registry for binding and the client will look up the remote objects.

One of the first things the management component needs to do is to connect to the `Registry`, therefore it has to set up the RMI registry. This can be achieved by calling the `LocateRegistry.createRegistry(int port)` method, which creates and exports a `Registry` instance on localhost. Use the provided property (named `proxy.rmi.port`) to get the port the `Registry` should accept requests on. Furthermore use the `proxy.host` to read the host the `Registry` is bound to. This information is vital to the client application that needs to connect to the `Registry` using the `LocateRegistry.getRegistry(String host,int port)` method.

After obtaining a reference to the `Registry`, this service can be used to bind an RMI remote interface (using the `Registry.bind(String name, Remote obj)`method). Remote Interfaces are common Java Interfaces extending the `java.rmi.Remote` interface. Methods defined in such an interface may be invoked from different processes or hosts. In our case, methods may be invoked by clients. The remote object you bind to the registry should contain exactly the previously mentioned 6 methods, which you have to bind to the registry. Furthermore to implement the notification mechanism for the client, the client has to provide a callback object. This callback objects again implements a remote interface, but the method(s) defined there are now designated to be used by a client. This way, you only have to bind a single object to the registry.

The Management Component of your Proxy is now ready to serve requests. Requests are handled by the remote object, which implements the methods that may be called by clients.

To make your object remotely available you have to export it. This can either be accomplished by extending `java.rmi.server.UnicastRemoteObject` or by directly exporting it using the static method `UnicastRemoteObject.exportObject(Remote obj, int port)`. In the latter case, use 0 as port: This way, an available port will be selected automatically.

#### 
Client  Updates


##### 
Arguments

Additionally to the `client.properties` the Client should now also read the `mc.properties` file, which defines the following properties:
•*`binding.name`: the name the management component shall use to bind its remote reference in the RMI registry.
•*`proxy.host`: the host name or IP address where the Proxy is running.
•*`proxy.rmi.port`: the TCP port where the Proxy is listening for connections.
•*`keys.dir`: the directory where the to look and store keys.
##### 
Implementation Details

At startup, the client reads out the previously mentioned properties to obtain the information where the RMI registry is located. The client is then able to retrieve the remote reference of the server using the `binding.name` property. Next you should export the client's remote object (callback object) so that the Proxy can notify the user about a subscription. The classes and methods you will need for all these steps have already been explained above: `LocateRegistry.getRegistry(String host,int port)`, `Registry.lookup(String name)` and `UnicastRemoteObject.exportObject(Remote obj, int port)`. Note that a client, in contrast to the servers, must not bind any objects to the `Registry`.

After these steps, the user can already type in commands. Please note that the Proxy's task is to check whether a specified user exists; that is, your Proxy should be able to deal with wrong inputs.
##### 
New Interactive commands
•*`!readQuorum`

This command returns the number of Read-Quorums that are currently used for the replication mechanism.

**This command does not require the user to be logged in!**


E.g.:

`>: !readQuorum`

`Read-Quorum is set to 2.
`

•*`!writeQuorum`

This command returns the number of Write-Quorums that are currently used for the replication mechanism. 

**This command does not require the user to be logged in!**



E.g.:

`>: !writeQuorum`

`Write-Quorum is set to 3.`


•*`!topThreeDownloads`

This command retrieves a sorted list that contains the 3 files that got downloaded the most. Where the first file in the list, represents the file that got downloaded the most.
**This command does not require the user to be logged in!**


E.g.:

`>: !topThreeDownloads`

`Top Three Downloads:`

`1. short.txt 20`

`2. long.txt  15`

`3. misc.txt  10`

•*`!subscribe <filename> <numberOfDownloads>`

When using this command the user creates a subscription for the given file, which means that the user gets notified by the Proxy whenever the file gets downloaded the given number of times. Together with the parameters, the client should also send its remote object as a callback object for the Proxy. Without the callback object, the Proxy has no possibility to notify the client.

**A successful login is required for this command! Furthermore update the *logout* respectively *exit* command so that the Proxy removes any existing callback object it may have stored for this client. If the user is no longer logged in, the Proxy can drop any subscriptions of this user!**


E.g.:

`>: !subscribe short.txt 10`

`Successfully subscribed for file: short.txt`


After the file got downloaded 10 times the client receives the following notification:


`Notification: short.txt got downloaded 10 times!.`

•*`!getProxyPublicKey`

A User can use this command to retrieve the Proxy's public key. When invoking this command the Proxy sends it's public key and the client stores the key in the key folder (given `keys.dir` property).

**This command does not require the user to be logged in!**


E.g.:

`>: !getProxyPublicKey`

`Successfully received public key of Proxy.`

•*`!setUserPublicKey <userName>`

With this command the user can exchange it's own public key with the Proxy. Therefore additionally to the name, the client should also send the public key for the given name. The Proxy stores the received key in the key folder (given `keys.dir` property).
**This command does not require the user to be logged in!**


E.g.:

`>: !setUserPublicKey Alice`

`Successfully transmitted public key of user: Alice.`


--------------------------------------------------------------------------------* * * *

## 
Stage 5 - Testing / Multi-Threading (5 points)
### 
Load Testing Component

In practice, file download systems are highly concurrent and should provide robustness and scalability, even for a large number of clients. Concurrency issues are hard to detect under normal operation, but generating artificial load on the system may help to detect problems and inconsistencies. Hence, you should create a load testing component to test the system's scalability and reliability. The following test parameters should be configurable in a properties file `src/test/resources/loadtest.properties`:
•**clients*: Number of concurrent download clients
•**uploadsPerMin:* Number of initiated uploads per client per minute
•**downloadsPerMin*: Number of initiated downloads per client per minute
•**fileSizeKB*: Size of files available for download, in KiloBytes (simply generate test files with random content)
•**overwriteRatio*: average ratio of uploads which overwrite an existing file (updating its version etc), as opposed to uploads which create a new file. For instance, *overwriteRatio=0.8* means that 80% of uploads in the test environment overwrite an existing file.
Moreover, the test environment should instantiate a client which subscribes to one or multiple files and receives notifications as soon as this/these file(s) are downloaded. During the tests, the client should simply print all the incoming events automatically to the command line.

Play around with different test settings and try to roughly determine the limits of your machine. By limits we mean the configuration values for which the system is still stable, but becomes unstable (e.g., runs out of memory after some time) if any of these values are further increased (or decreased). Monitor the memory usage with tools like "top" (Unix) or the process manager under Windows, and let the test program execute for a sufficiently long time (around 5-10 minutes). Obviously, the load test can also help you identify issues in your implementation (e.g., deadlocks, memory leaks, ...).
In your submission, include a file **evaluation.txt** in which you provide your machine characteristics (operating system version, number and clock frequency of CPUs, RAM capacity) and the key findings of your evaluation (at least the limit values for all configuration parameters). **Note: **Please try to **avoid** running your tests on our **lab servers**; however, if it is absolutely necessary that you run the tests on the servers, **make sure that your tests terminate after a short time** (say, 5 minutes)! The servers will be monitored and long-running resource-greedy processes may be forcibly killed without prior notice! If you decide to use the servers, only execute the final run of your evalution on the servers, but please develop your code somewhere else (e.g., on the PCs in the DSLab) to avoid blocking the server resources for other students. Also, we advise you not to use (and rely on) the Lab servers in the last days before the deadline.


--------------------------------------------------------------------------------* * * *

#### 
Lab Port Policy

Since it is not possible to open `ServerSocket` on ports where other services are already listening for requests, we have to make sure each students uses its own port range. 
So if you are testing your solution in the lab environment (i.e., on the lab server) you have to obey the following rule: you may only use ports between **10.000 + dslabXXX * 10** and **10.000 + (dslabXXX + 1) * 10 - 1**. So if your account is dslab250 you may use the ports between 12500 and 12509 inclusive. Note that you can use the same port number for TCP and UDP services (e.g., it is possible to use TCP port 12500 and UDP port 12500 at the same time).

--------------------------------------------------------------------------------* * * *

### 
Regular expressions

We provide some regular expressions you can use to **verifiy that the messages you exchange between the three applications are well-formed**. This is important because we will test your program against own code. We recommend to use Java assertions for this. The provided build file enables them automatically.

`final String B64 = "a-zA-Z0-9/+";`

`// stage II`
`// Note that the verified messages still need to be encrypted (using RSA or AES, respectively) and encoded using Base64!!!`

`// login request send from the Client to the Proxy`
`String firstMessage = ...`
`assert firstMessage.matches("!login \\w+ ["+B64+"]{43}=") : "1st message";`
`// the Proxy's response to the client`
`String secondMessage = ...`
`assert secondMessage.matches("!ok ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{22}==") : "2nd message";`
`// the last message send by the Client`
`String thirdMessage = ...`
`assert thirdMessage.matches("["+B64+"]{43}=") : "3rd message";`

`// stage III`

`// UDP message that is send from the fileserver to the Proxy in a recurring manner`
`String udpMessage = ...`
`assert message.matches("!alive 1[0-9]{4}");`

`// messages beeing exchanged between Proxy and fileservers before the final Base64 encoding`
`String hashedMessage = ...```
`assert message.matches("["+B64+"]{43}= [\\s[^\\s]]+");`


--------------------------------------------------------------------------------* * * *

### 
Hints & Tricky Parts (Security)
#### 
"java.lang.SecurityException: Unsupported keysize or algorithm parameters" or "java.security.InvalidKeyException: Illegal key size"

You will need to install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files from Java SE Download page (last download link). The installation explanation can be found in README file, but basically you will just have to copy `local_policy.jar` and `US_export_policy.jar` into`$JAVA_HOME/jre/lib/security/` directory.

#### 
The decrypted messages are in gibberish and no exception is thrown.

Possible reasons:
1.1.You forgot to decode the decrypted message back from Base64 format.
2.2.Wrong value was used for initialization vector (IV) parameter when initializing the AES cipher.
3.3.You didn't use `Cipher.DECRYPT_MODE` when initializing the AES cipher

#### 
java.security.NoSuchProviderException: No such provider: BC

This exception is thrown when the Bouncy Castle library is not properly installed. Please recheck that you correctly did all the steps from the Installation and Static Registration of the Bouncy Castle Provider section and actually use the so configured JDK.

#### 
java.security.NoSuchAlgorithmException: No such algorithm: "..."

The most probable cause is that you misspelled the name of the algorithm. Please check that name of the algorithm is same as mentioned in this assignment. 
This exception is also thrown when the Bouncy Castle provider is not properly installed. If the name of the algorithm is correct, than you should recheck that you correctly did all the steps from the Installation and Static Registration of the Bouncy Castle Provider section.

#### 
java.security.InvalidKeyException: no IV set when one expected

You forgot to initialize the AES cipher using the initialization vector (IV) parameter. The IV parameter is mandatory for the `"AES/CTR/NoPadding"` algorithm. See code snippets on how to correctly initialize the AES cipher.

#### 
Helpful Code Snippets

All code snippets are just examples and omit all exception handling for clarity. This does not mean that you should not do exception handling in your lab solution!

•*How to encode into and decode from Base64 format?
•*How to read a PEM formatted RSA private key?
•*How to read a PEM formatted RSA public key?
•*How to generate a secure random number?
•*How to generate an AES secret key?
•*How to initialize a cipher?
•*How to read the shared secret key?
•*How to create a hash MAC?
•*How to verify a hash MAC?

##### 
How to encode into and decode from Base64 format?

`import org.bouncycastle.util.encoders.Base64;`

`[...]`

`// encode into Base64 format` 
`byte[] encryptedMessage = ...` 
`byte[] base64Message = Base64.encode(encryptedMessage);`

`// decode from Base64 format` 
`encryptedMessage = Base64.decode(base64Message);`

##### 
How to read a PEM formatted RSA private key?

`import java.security.KeyPair;` 
`import java.security.PrivateKey;` 
`import org.bouncycastle.openssl.PEMReader;` 
`import org.bouncycastle.openssl.PasswordFinder;`

`[...]`

`String pathToPrivateKey = ...` 
`PEMReader in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {`

`@Override`

`public char[] getPassword() {`
`// reads the password from standard input for decrypting the private key`

`System.out.println("Enter pass phrase:");`

`return new BufferedReader(new InputStreamReader(System.in)).readLine();`

`}`

`});`

`KeyPair keyPair = (KeyPair) in.readObject();` 
`PrivateKey privateKey = keyPair.getPrivate();`

##### 
How to read a PEM formatted RSA public key?

`import java.security.PublicKey;` 
`import org.bouncycastle.openssl.PEMReader;`

`[...]`

`String pathToPublicKey = ...` 
`PEMReader in = new PEMReader(new FileReader(pathToPublicKey));` 
`PublicKey publicKey = (PublicKey) in.readObject();`

##### 
How to generate a secure random number?

`import java.security.SecureRandom;`

`[...]`

`// generates a 32 byte secure random number` 
`SecureRandom secureRandom = new SecureRandom();` 
`final byte[] number = new byte[32];` 
`secureRandom.nextBytes(number);`

##### 
How to generate an AES secret key?

`import javax.crypto.KeyGenerator;` 
`import javax.crypto.SecretKey;`

`[...]`

`KeyGenerator generator = KeyGenerator.getInstance("AES");` 
`// KEYSIZE is in bits `
`generator.init(KEYSIZE);` 
`SecretKey key = generator.generateKey();` 
##### 
How to initialize a cipher?

`import javax.crypto.Cipher;`

`[...]`

`// make sure to use the right ALGORITHM for what you want to do `
`// (see text) `
`Cipher crypt = Cipher.getInstance(ALGORITHM);` 
`// MODE is the encryption/decryption mode `
`// KEY is either a private, public or secret key `
`// IV is an init vector, needed for AES `
`crypt.init(MODE, KEY [,IV]);`

##### 
How to read the shared secret key?

`import java.io.FileInputStream;`
`import java.security.Key;`
`import javax.crypto.spec.SecretKeySpec;`
`import org.bouncycastle.util.encoders.Hex;`

`[...]`

`byte[] keyBytes = new byte[1024];`
`String pathToSecretKey = ...`
`FileInputStream fis = new FileInputStream(pathToSecretKey);`
`fis.read(keyBytes);`
`fis.close();`
`byte[] input = Hex.decode(keyBytes);`
`// make sure to use the right ALGORITHM for what you want to do `
`// (see text) `
`Key key = new SecretKeySpec(input,ALGORITHM);`

##### 
How to create a hash MAC?

`import java.security.Key;`
`import javax.crypto.Mac;`

`[...]`

`Key secretKey = ...` 
`// make sure to use the right ALGORITHM for what you want to do `
`// (see text) `
`Mac hMac = Mac.getInstance(ALGORITHM);` 
`hMac.init(secretKey);`
`// MESSAGE is the message to sign in bytes `
`hMac.update(MESSAGE);`
`byte[] hash = hMac.doFinal();`

##### 
How to verify a hash MAC??

`import java.security.MessageDigest;`
`import javax.crypto.Mac;`

`[...]`

`// computedHash is the HMAC of the received plaintext `
`byte[] computedHash = hMac.doFinal();`
`// receivedHash is the HMAC that was sent by the communication partner `
`byte[] receivedHash = ...`

`boolean validHash = MessageDigest.isEqual(computedHash,receivedHash);`


--------------------------------------------------------------------------------* * * *

### 
Hints & Tricky Parts (RMI)
•*To make your object remotely available you have to **export** it. This can either be accomplished by extending `java.rmi.server.UnicastRemoteObject` or by directly exporting it using the static method `java.rmi.server.UnicastRemoteObject.exportObject(Remote obj, int port)`. Use `0` as port, so any available port is selected by the operating system.

•*Before shutting down a server or client, unexport all created remote objects using the static method `UnicastRemoteObject.unexportObject(Remote obj, boolean force)` – otherwise the application may not stop.

•*Since Java 5 it's not required anymore to create the stubs using the **RMI Compiler** (`rmic`). Instead java provides an automatic proxy generation facility when exporting the object.

•*Take care of **parameters and return values** in your remote interfaces. In RMI all parameters and return values except for remote objects are passed per value. This means that the object is transmitted to the other side using the java serialization mechanism. So it's required that all parameter and return values are serializable, primitives or remote objects, otherwise you will experience `java.rmi.UnmarshalException`s.

•*To create a **registry**, use the static method `java.rmi.registry.LocateRegistry.createRegistry(int port)`. For obtaining a reference in the client you can use the static method `java.rmi.registry.LocateRegistry
