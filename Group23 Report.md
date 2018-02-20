# Report for assignment 3

## Project
Name: Elastic Search

URL: https://github.com/elastic/elasticsearch 

Elasticsearch is a distributed RESTful search engine built for the cloud, built on top of Lucene. It can be used to search all kinds of documents. It provides scalable search, has near real-time search, and supports multitenancy. Elasticsearch is distributed, which means that indices can be divided into shards and each shard can have zero or more replicas.

## Selected issue(s)
Title: Cleanup PathTrie

URL: https://github.com/elastic/elasticsearch/issues/25563 

The issue wants to refactor the class PathTrie. They want to change it so that the parameters in RestRequest are not changed for every TrieMatchingMethod.

Furthermore, they want to switch the implementation so that it uses FST from Lucene instead of their own implementation.
Finally, they also want to use the builder pattern for PathTries. This would also make each PathTrie object immutable when they are created.

## Onboarding experience
### Did it build as documented?
Luckily, there was plenty of documentation and instructions on how to build the project. They gave detailed instructions on how to install it and run it for each different version. They also had a pretty in-depth document for testing the different modules of the project. Even integrating the project to eclipse had specific instructions. There were certain things that needed to be installed for the project to build; namely Java 9 and gradle for the workstation that didn’t have them. 

While some people in the group got the thing to run without much problem, other experienced issues while trying to run tests or build the project. Some got error messages they couldn’t fix when trying to build the project from the unix bash terminal, but got it working on eclipse. The reason for this is uncertain, but it could have been factors unrelated to the project itself. 

One final important thing to mention here is that when Rami and Niklas tried to run the project on their windows computers, they got trojan warnings from their antivirus software. This was tied to a specific test in one of the test suite, so we decided to comment it out.

## Requirements affected by functionality being refactored
PathTrie is a trie structure that is a structure of TrieNodes. Each TrieNode has a string key that represent a pathname, a value and a boolean that tells if the key for the node is a wild card (meaning that it can represent any pathname). These requirements are based on the code implementation (specifically the public methods), rather than an API or requirements document, as there is basically no documentation for this module.

#### Functional Requirement #1: Insert value into PathTrie

Function: insert(String *path*, T *value*)

##### Input: 

String *path*, a string that represents the path where the value should be stored. If any part of the path is surrounded by curly brackets, it is counted as a wildcard. 
    Example: “a/{testB}” → testB = Wildcard

T *value*, a value of type T that is inserted into PathTrie at specified path.

##### Contract
Given a path and a value, the value will be inserted into the Trie based on that specified path. If a value already exists at specified path, the function will not overwrite the current value.

#### Functional Requirement #2: Retrieve value

Function: retrieve(String *path*, {optional} Map<String, String> *params*, {optional} TrieMatchingMode *trieMatchingMode*)

##### Input:

String *path*, specifies path where desired value resides in PathTrie.

**Optional** Map<String, String> *params*, map object which is used by the retrieve function to write wildcard values to. If this is not set, then params will default to null. 

**Optional** TrieMatchingMode *trieMatchingMode*, Specifies when wildcards are permitted when retrieving value.
- EXPLICIT_NODES_ONLY, means that path in the pathname has to exist to retrieve the resource, no wildcards allowed. 
- WILDCARD_NODES_ALLOWED, means that wildcards are allowed. 
- WILDCARD_LEAF_NODES_ALLOWED, means that wildcards are only allowed for leafnodes. 
- WILDCARD_ROOT_NODES_ALLOWED, means that wildcard is only allowed for the root node. 
If trieMatchingMode isn’t set, it will default to WILDCARD_NODES_ALLOWED.

##### Contract
Given a path, if there is a value that is stored on that path, it is retrieved. Return value is the value corresponding to path or null if nothing was found. 

#### Functional Requirement #3: PathTrie Iterator

Function: retrieveAll(String *path*, Supplier<Map<String,String>> *paramSupplier*)

##### Input:

String *path*, specifies path where desired value resides in PathTrie.

Supplier<Map<String,String>> *paramSupplier*, A collection of map objects where each map object which wildcard values are written to.

##### Contract 
The function returns an iterator. The iterator will iterate through every possible TrieMatchingMode and for every iteration, the iterator will return the value that is found with path for a different TrieMatchingMode or null if no matching value exists.

#### Functional Requirement #4: Insert or update value into PathTrie

Function: insertOrUpdate(String *path*, T *value*, BiFunction<T, T, T> *updater*)

##### Input:
String *path*, a string that represents the path where the value should be stored. If any part of the path is surrounded by curly brackets, it is counted as a wildcard. 

T *value*, a value of type T that is inserted into PathTrie at specified path.

BiFunction<T, T, T> *updater*, used for when value needs to be updated. 

##### Contract
Given a path and a value, the value will be inserted into the Trie based on that specified path. If a value exists for the specified path, then it will be overwritten. 
NOTE: This function/requirement may not be that relevant, since the issue wishes for the PathTrie to be immutable. 


#### Classes that use the PathTrie class
- MockHTTPTransport
- RestController
- RestUtils
- PathTrieTests
- RestControllerTests

## Existing test cases relating to refactored code
There exists multiple test methods for the PathTrie functionality:
- testPath(): Tests to insert values at certain path, with some of these paths containing wildcards (e.g value: “one” is stored at path “a/x/\*”). Test tries to retrieve a value from certain path and assert that the value is correct. It even checks paths that do not contain values. 
In addition, it also checks that it can retrieve the correct parameters when retrieving a value (which are supposed to be the correct wildcards from the paths it retrieved from).

- testEmptyPath(): Test to insert and retrieve a value from the empty path (“/”).

- testDifferentNamesOnDifferentPath(): Tries to retrieve value from a path and check the parameters. Both paths ends with a wildcard. After asserting that parameters has been set correctly, the parameters are cleared and the test tries to retrieve a different value from a different path. The test asserts that the correct values have been retrieved and that the parameters are correctly set.

- testSameNameOnDifferentPath(): Same as above, except that the paths has the same name for the wildcards. 

- testPreferNonWildcardExecution(): Tests to receive values from paths that could fit in several defined paths in the Trie. The assertion checks that the retrieve-instruction favours matching paths that uses the least wildcards.

- testWildcardMatchingModes(): Set different values at different paths and then tries to retrieve resources with different matching modes (e.g EXPLICIT_NODES_ONLY) Assertions checks that the retrieved values follows the rules set by the set matching mode.

- testExplicitMatchingMode(): Tries to retrieve values with the matching mode EXPLICIT_NODES_ONLY. The assertions checks that the retrieve function only return values for matching paths that do not use wildcards.

- testSamePathConcreteResolution():  Tries to get values from paths that consists of only wildcards. 

- testNamedWildcardAndLookupWithWildcard(): Test to specify paths with wildcards when retrieving values. 

- testEscapedSlashWithinUrl(): Tries to insert and receive values where the path contains escaped characters. This test actually uses a special decoder from RestUtils.

These tests should preferably still be applicable after refactoring, although some changes may be needed (e.g they need to be updated if we create a PathTrieBuilder).

## The refactoring carried out

### UML diagrams
UML Class diagram of the architecture before refactoring:

https://drive.google.com/file/d/11Z54-5qnUDzZQQ-d023nqF74I_Or4v3u/view?usp=sharing

In this UML diagram we can see the immediate classes that will be affected by any change in the PathTrie, which is the proposed change that we are mainly concentrating on. This gives a better picture of what each entity does and also how they are related, so that we can write appropriate test cases and also ensure the class interaction changes are not causing any regression.

UML Class diagram of the architecture after refactoring:

https://drive.google.com/file/d/1jXFHhz-pjbl0Vh-n_Xb_yVVbpqKwTOoK/view?usp=sharing

This UML diagram shows how the class references have changed and also we can see that the respective newer test modules were added in order to ensure coverage. The major refactoring here is that the builder class that takes over the dependencies from the class PathTrie. And the newer FSTRepresentation added to the PathTrie.

## Refactor 1: RestRequest params

### Task

In the issue they said that they didn't want the params from RestRequest to be modifed by the retieve method in PathTrie.
> We should further refactor PathTrie so that the params of RestRequest are not modified for each TrieMatchingMethod

### Carried out work with params

Not much was actually accomplished with this refactoring in the end due to the difficulties trying to figure out how it is used and the necessity of it. The only "work" done on it was trying to get an understanding of it:

Params is a hashmap that holds pointers for the parameters that are sent with a REST request. The request uri might contain parameters that are decoded with the function decodeQueryString in RestUtils that uses an implementation of the Decode interface from PathTrie. The implemented decoder REST_DECODER calls the function decodeComponent on a string. decodeQueryString calls decodeComponent but it also extracts a name from the uri that will be the key for the component and add it to the hashmMap(name,parameter).

In RestController there is an iterator for allHandlers that match the request with the possible correct handlers. It also passes on the params variable within the RestRequest object to the retrieval method in PathTrie. When a NamedWildCard is needed to find some value, the value will be decoded just like any component found in the uri and added to params with NamedWildCard as the key. This means that the params will only be affected if NamedWildCard is used i.e. the key for the TrieNode is a WildCard.
The iterator method in RestController calls the iteratior method in PathTrie that runs the retrieval method 4 times. It runs 1 time for every TrieMatchingMode of which there are 4. Because of this they have created a lambda expression that passes on an original copy of params to the iterator in PathTrie through a supplier so that every retrieval will use an original params base.

After the iteration is done and it has been able to retrieve all the handlers it will use them in a method called dispatchRequest that seemingly will dispatch the request with a correct handler if successful. The way this is accomplished however is a bit of a mystery though because of the multiple of different classes used that in turn use other classes and with no clear documentation of the method it is very hard to discern anything useful in order to understand why the params variable is necessary, why it is used like it is and exactly how it is used.

There is a Tests-class for RestController whose test-methods doesn’t fail even if the params reset is disabled. This either mean that the don’t have any test that rely on that function or it doesn’t actually serve any purpose any more. Based on a discussion in a pull request  related to this issue however, it doesn’t seem like it is some piece of forgotten code. Running all the test suits also didn’t turn anything up. Running the integration tests however gave a huge amounts of errors and a really long test-log to read through.

### Remaining work on params

Since no refactoring was actually done on this issue, all the work remains on params. There might be some clues to find in the test logs but they are very big and unwieldy making it somewhat difficult. Even if you are able to get a grasp on how it all hangs together it still might take a long time to come up with a good solution making it hard to estimate how much time this would take to do. The big issue here is to be able to grasp how params is used and what it affects. After that you might be able to figure out a way to actually perform the refactoring.

### Testing logs from trying to smoke params out

https://raw.githubusercontent.com/rka0917/elasticsearch/master/Group23_Logs/paramsSearching/Incorrect_Params_Integ_Log_Part1.txt

https://raw.githubusercontent.com/rka0917/elasticsearch/master/Group23_Logs/paramsSearching/Incorrect_Params_Integ_Log_Part2.txt

https://raw.githubusercontent.com/rka0917/elasticsearch/master/Group23_Logs/paramsSearching/Incorrect_Params_Test_Log.txt

## Refactor 2: Switching to Lucene FST

### Task
In the issue they said that they wanted to replace their current implementation for one using the FST class of the Apache Lucene library:
> switch PathTrie over to using an FST from Lucene, rather than our own trie implementation

### Carried out work in Lucene FST
We have implemented two versions that separately solve a sub-problem of the PathTrie functional requirements. However, this issue turned out to be harder than expected. Therefore we were unable to complete the refactoring in the given time-frame and had to leave the solutions as independent files.

There are three sub-requirements for the PathTrie class.
1. It must be able to match exact strings
2. It must be able to match using wildcards
3. It must be able to group/capture matched sub-strings

##### Exact string-matching
This problem was solved and partly integrated into the PathTrie and related sub-classes.

Exact matching with strings is easy enough using Lucene FST. The problem in this case has to do with the labels of the different states in the implementation.
PathTrie is a generic class that may take any type of data. This data is then supposed to be stored as labels in the respective states. This is not supported by the Lucene FST library which expects the labels to be one of; byte sequence, character sequence, integer sequence, no output, pairs or positive integers. The library does however have a public interface Outputs<T> which is left for the developer to implement. After much struggle with the implementation and discussions within the group we came up with the work-around of converting the data into byte-arrays while storing them and then converting them back to objects upon retrieval.

##### String-matching with wildcards
This problem was solved but left as an independent class as a future reference.

The second issue was that PathTrie supports the use of wildcards, which the FST library does not. Lucene FST can only check for exact string-matches. Because of this limitation we looked into Lucene Automata despite the issue asking for the FST class. The Automata allows for regexp and may thus be used for less strict matchings. Unfortunately we were unable to find any way of changing the labels of the states within the Automata, and as long as the labels are not changed, the Automata can not be used for anything other than answering if the query is in the given language.

This should help the person to get the required results while using the Lucene library which is otherwise very poorly explained/documented online.

### Remaining work on Lucene FST
Our patch was able to solve 2 out of 3 sub-criteria of the PathTrie file. The third one remains to be solved. Additionally, one would have to combine the solutions into something that can solve all three problems simultaneously and also integrate this solution into the main PathTrie-file.

##### Regex-matching with grouping/capturing
Solving the third issue is non-trivial. This problem requires regex grouping/capturing which there does not seem to be any support for in Lucene. Solving this issue would therefore be very hard if one was to stick purely to Lucene as requested. Based on the time it took to figure out and implement the solutions to the first two problems we would assume the third one takes somewhere around 10-20 hours.

##### Merge solutions and integrate them
Once you have a good understanding of PathTrie and Elasticsearch, merging the solutions and integrating them into PathTrie would probably take at least 10 hours. This estimate includes planning, constructing test-cases and finally the actual implementation.

##### Total work
In total we would expect the remaining FST requirements to take at least 20 hours for a developer who is already accustomed to Elasticsearch and PathTrie. If a new developer like us attempts to complete the issue, another 5-10 hours would be added upon this estimate to understand the related classes.
## Refactor 3: PathTrie Builder

### Task
The last task that was mentioned in the issue was to create a PathTrie Builder, for the sake of making the PathTrie object immutable. 

### Work carried out on Builder
An additional nested class was created inside the PathTrie class called PathTrieBuilder. Methods for inserting values into the PathTrie was moved to the PathTrieBuilder, to ensure that PathTrie objects cannot be modified. Since the builder is a inner class of PathTrie and it is the only one that should be accesible publicly, the constructor for PathTrie is now private and TrieNode was changed to a private inner class (before it was public, for no practical reason). A function was added to create and return a PathTrie inside the Builder. 

### Link to patch
https://github.com/elastic/elasticsearch/compare/master...rka0917:master

## Test logs

Test log before refactoring: 

https://raw.githubusercontent.com/rka0917/elasticsearch/master/Group23_Logs/paramsSearching/Incorrect_Params_Test_Log.txt

Test logs after refactoring: 

https://raw.githubusercontent.com/rka0917/elasticsearch/master/Group23_Logs/refactoringTestLogs/Our_Fork_Integ_Log.txt

https://raw.githubusercontent.com/rka0917/elasticsearch/master/Group23_Logs/refactoringTestLogs/Our_Fork_Test_Log.txt

## Effort spent
For each team member, how much time was spent in:

#### plenary discussions/meetings
Anton - 7 hours

Benjamin - 7 hours 

Ganesh - 7 hours 

Niklas - 7 hours 

Rami - 7 hours

#### discussions within parts of the group
Anton - 2 hours

Benjamin - 1 hours

Ganesh - 2 hours

Niklas - 2 hours

Rami - 3 hours

#### reading documentation
Anton - 7 hours

Benjamin - 2 hours

Ganesh - 3 hours

Niklas - 7 hours

Rami - 2 hours

#### configuration
Anton - 2 hours

Benjamin - 3 hours

Ganesh - 4 hours

Niklas - 2 hours

Rami - 3 hours

#### analyzing code/output
Anton - 4 hours

Benjamin - 6 hours

Ganesh - 5 hours

Niklas - 6 hours

Rami - 6 hours

#### writing documentation
Anton - 3 hours

Benjamin - 13 hours

Ganesh - 9 hours

Niklas - 3 hours

Rami - 7 hours

#### writing code
Anton - 14 hours

Benjamin -

Ganesh - 

Niklas - 3 hours

Rami - 4 hours

#### running code
Anton - 1 hour

Benjamin - 1 hour

Ganesh - 1 hour

Niklas - 5 hours

Rami - 3 hours 

## Overall experience
This project work was much larger than anticipated. We are all pretty inexperienced with working with open source and have not encountered something of that scale on any assignments at KTH. Something that really stood out compared to code we work with in our assignments are that this project took way more time to build and run, lasting for several minutes. We initially lost a lot of time trying to get the project to run because of this as we ran into some issues. 

There was good documentation for importing, building and running the project. There was even documentation for specific things like running it in eclipse. However, when it comes to the structure of the code or any documentation of how the code works, it was kind of lackluster. We didn’t have much to work with when it comes to analyzing the code and its functions. That meant that it was very difficult to navigate when looking for java files in different packages and to figure out what everything did and the importance of certain parts of the project were. When issues are discussed in the issue tracker, they are not very thorough, making this project a little unfriendly to new people who want to contribute. There was certainly a much steeper difficulty curve than anticipated. 

It was also odd that we encountered warnings on windows computers, which threw us for a loop for a short amount of time. It was fixed when a certain method in a class was commented out. 

What we have learned from this project is that there is a lot of time that there is most likely going to be very little time spent on coding and more time on actually understand the project itself and looking through code/documentation and discussing things.

In hindsight, we should have investigated the issue further before deciding on using that as the assignment. We could have spotted that the documentation for the code was a little bit lackluster and that it was hard to understand the code. We also should have communicated more during the beginning and middle of the project. Most of the communication happened unfortunately at the end of the week. 

To summarize, we learnt about the importance of proper documentation, as it would have saved us a lot of time and maybe even enabled us to finish the task that we set out to do. That is very important when you are working on open-source. 
