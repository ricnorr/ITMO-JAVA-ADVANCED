# ITMO-JAVA-ADVANCED
## Problem statements

### 1. I/O: **Walk**

Develop class ```RecursiveWalk```, calculating files hash in specified direcotry recursively.

* Console run format: ```java RecursiveWalk <input file> <output file>```.
* Input file contains list of directories needed to walk through.
* Output file must contains calculation results for each line of unput line in the following format: ```<hex hash> <path>```, one per line.
* Hash calculation algorithm: FNV.
* In case of any errors, the hash result must be ```00000000```.
* Input and output files encoding is UTF-8.
* Parent directory to the output file should be created if it is absent.
* File sizes may exceed RAM size.


### 2. Collections Framework: **ArraySet**

Develop class ```ArraySet```, implementing immutable ordered set.

* Class ```ArraySet``` must implement ```NavigableSet``` interface.
* All methods must be maximally asymptotic effective.


### 3. Data streams: **Students**

Develop class ```StudentDB```, processing search queries over students database.

* Class ```StudentDB``` must implement both ```StudentQuery``` and ```StudentGroupQuery``` interfaces.
* Each method must contains exactly one operator.


### 4. Reflection: **Implementor**

Develop class ```Implementor```, generating basic class or interface implementation source code.

* Console arguments: qualified interface name to generate implementation of.
* The result of the run must be a generated java code with ```Impl``` suffix, extending (implementing) given class (interface).
* It must be possible to compile generated code without errors.
* Generated class cannot be abstract.
* Generated methods must ignore arguments and return default value of appropriate return type.
* Input classes or interfaces do not utilize generics.

    
### 5. Jar: **Jar Implementor**

1. Create ```.jar``` file including compiled ```Implementor``` and all related classes.

    * Created archive must be runnable using command ```java -jar```
    * All additional command line arguments must be passed to ```Implementor```

2. Modify ```Implementor```. With additional argument ```-jar``` it must create ```.jar``` archive including generated implementation of given class or interface. 

    
### 6. Javadoc: **Implementor Javadoc**

1. Add comprehensive documentation to solutions of problems 4 and 5 using Javadoc.

    * All classes and all methods (including ```private``` ones) must be provided with documentation.
    * Documentation must be compilable without errors.
    * All links to standard library classes must be valid.
    
### 7. Basic parallel computing: **Iterative parallelism**

Develop class ```IterativeParallelism```, processing lists in multiple threads.

* It is required to support following queries:

    * ```minimum(threads, list, comparator)```: first occurring minimum.
    * ```maximum(threads, list, comparator)```: first occurring maximum.
    * ```all(threads, list, predicate)```: check whether all elements satisfy given predicate.
    * ```any(threads, list, predicate)```: check whether any element satisfies given predicate.
    * ```filter(threads, list, predicate)```: list containing only mathcing given predicate elements.

* Number of threads created to compute the query must be equal to ```threads``` argument.
* Usage of Concurrency Utilities is prohibited.

    
### 8. Basic parallel computing: **Parallel mapper**

1. Develop class ```ParallelMapperImpl```, implementing ```ParallelMapper``` interface.

    * Method ```run``` must concurrenlty compute given function on each element of ```args```.
    * Method ```close``` must terminate all threads.
    * Constructor ```ParallelMapperImpl(int threads)``` must create ```threads``` threads which may be utilized for future computations.
    * It is assumed that there may be several clients generating queries to compute.
    * All given tasks must be processed in queue.
    * There must not be any active standby.
    
### 9. Concurrency Utilities: **Web Crawler**

Develop thread-safe class ```WebCrawler``` implementing ```Crawler``` interface, recursively visiting web pages.

* The class must have the constructor, accepting the following arguments:

    * ```downloader```: ```Downloader``` instance to use in order to download pages and extract links.
    * ```downloaders```: Upper bound for number of pages concurrently downloading.
    * ```extractors```: Upper bound for number of pages concurrently analyzed for links extraction.
        
### 10. Networking: **HelloUDP**
    
Develop client and server, interacting via UDP.

* Class ```HelloUDPClient``` must send requests to a designated server, receive responses and echo them to the console.

    * Command line arguments:
    
        * Server host name or IP.
        * Port which the server is listening to.
        * Request prefix.
        * Number of threads sending requests.
        * Number of requests per thread to send.
        
    * All requests must be sent concurrently in the given number of threads. Each thread must send its request, standby for response and echo both request and response. In case of no response, thread must try to send request again, terminating only upon reaching target number of requests for each thread.
    * Requests format: ```<request prefix><thread number>_<request number>```.
    
* Class ```HelloUDPServer``` must accept requests sent by ```HelloUDPClient``` and send appropriate response.

    * Command line arguments:
    
        * Port to listen.
        * Number of threads processing requests.
        
    * Response format: ```Hello, <request>```.
    * It is allowed to temporarily halt receiving new requests if server is busy.
        
### 11. Serialization and RMI: **Bank**

**The initial bank implementation is located in course [examples of RMI usage](http://www.kgeorgiy.info//courses/java-advanced/examples/rmi.zip).**

* Add functionality to work with individuals (```Person```).

    * It must be possible to get first name, last name and passport number of given ```Person```.
    * Local ```Person``` instances (```LocalPerson```) must be transferred using serialization. These are snapshots and are not affected remotely since created.
    * Remote ```Person``` instances (```RemotePerson```) must be used via RMI. Only skeleton is transferred so all changes affect original ```Person``` stored in bank.
    * Each ```Person``` may possess several bank accounts those must be accessible. These accounts must have following ids: ```passport:subId```.
    * Bank functionality to add:
    
        * Lookup any instance type of ```Person``` by passport number.
        * Create new ```Person``` record.
        
* Develop demo application.

    * Command line arguments:
    
        * First name.
        * Last name.
        * Passport number.
        * Account ID.
        * Money amount to add.
        
    * If provided individual information is absent in bank, an appropriate ```Person``` must be created, otherwise given data is verified.
    * If account with given ID is not in individual's possession, the new one is created with initial amount of 0.
    * The designated account amount is increased by a given value. New amount must be printed to the console.
    
* Create tests verifying the foregoing behavior.

    * It is recommended to use JUnit 5.
    * Tests should not expect RMI Registry to be started preliminarily.
    * Create class ```BankTests``` which starts tests and prints results to the console.
    * Create script, launching ```BankTests```. Return code must be 0 only in case of success.
    * Create script, launching tests in a common way for the used testing framework. Return code must be 0 only in case of success.
    

    
### 13. Internationalization and localization: **Text statistics**

* Develop ```TextStatistics``` application, analyzing languages in various languages.

    * Command line arguments: ```TextStatistics <text locale> <output locale> <text file> <report file>```
    * Supported text locales: all available locales in system.
    * Supported output locales: Russian, English.
    * Files encoding: UTF-8.
    * Categories to analyze:

        * Sentences.
        * Lines.
        * Words.
        * Numbers.
        * Money amounts.
        * Dates.

    * Statistcs to calculate by each category:

        * Number of occurrencies.
        * Number of distinct values.
        * Minimal value.
        * Maximal value.
        * Minimal length.
        * Maximal length.
        * Mean value / length.

    * Report format: HTML.
    * Texts to analyze size is expected to be less than RAM size.
    
* Create tests verifying the foregoing behavior.

