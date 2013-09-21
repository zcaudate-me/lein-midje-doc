(ns midje-doc.midje-doc-guide
  (:require [midje.sweet :refer :all]))

"
## Notice
[`lein-midje-doc`](https://www.github.com/zcaudate/lein-midje-doc) has been used to generate its own documention 
"

[[:chapter {:title "Literate Programming is Flawed"}]]
"
The phrase 'Literate Programming' has been very popular lately. The main idea is that the code is written in a way that allows both a machine *and* a person to understand what is going on. Most people seem to agree that it is a great idea.

I have a differing view. I agree that we should be creating more understandable content. However I disagree with whom the content is primarily created for. I am advocating using the phrase **Testable Documentation** as opposed **Literate Programs**. 

Just a change of wording? Yes. But I belive that it is an important one. 
"

[[:section {:title "Programming vs Documentation"}]]

"
Humans and machines are fundamentally different and rely on completely different methods of communication:

- Communication to Machines are usually very linear and procedural. It involves giving them a specific set of instructions. First Do This, Then Do That.... Machines don't really care what the code does. It just executes whatever code it has been given.


- Communication to Humans usually take a very different form. Most of us wish to learn the bigger picture first before deciding whether to commit time and brainpower to learning a libray or reading the rest of the documentation. We wish to be engaged, inspired and taught, not given a sequence of instructions that each break down to even smaller sequences.

**In short:** Machines are programmed while humans are engaged, inspired and taught. *Programs* are written for machines. *Documentation* are written for humans.
"

[[:section {:title "Writing for Humans"}]]

"
The words 'literate programming' places importance on the word 'programming', the word 'literate' is really just an adjective that makes it sound human friendly. It is a set of instructions for a machine first - the human readable part is secondary.

Most 'literate programming' tools also take this view - taking source code or test files and generating 'human readable' documentation - which really just means that some pretty colors are used to style the code, making it more attractive to look at. It is definitely one better than reading raw source code, although how 'literate' the output of such a program is to a human being is debateble. Most outputs from literate programs still resemble a set of instructions to a machine, not a document that can engage, inspire and instruct.

We as programmers should aim to write not just for machines, but for humans as well.
"

[[:chapter {:title "Documentation is Painful"}]]

"
I *detest* the documentation process. I detest it not because consider myself as 
*hardcore* but for precisely the opposite reason. I am a *softcore*, I *am not* a 
*masochist*, and I *do not* take pleasure in throwing myself against a brick wall. 

Programming is a very precise art form. Programming mistakes, especially the little 
ones, can result in dire consequences and much wasted time. We therefore use tools 
such as debuggers, type checkers and test frameworks to make our coding lives 
easier and our source code correct. 

Documentation is our means of communicating how to use or build upon our library to the 
larger audience of peers. This means that any mistakes in the documentation results in 
wasted time for *all involved*. Therefore any mistake in documentation can have a 
*greater effect* than a mistake in source code because it wastes *everybody's* time.
"

[[:section {:title "A Fictitious Scenario"}]]

"In this fictitious example, a clojure project has been generated using `leiningen`:"

[[{:lang "bash" :numbered false}]]
(comment
> lein new fami  
> cd fami
> lein repl)

[[:subsection {:title "Version One"}]]

"
A very useful function [`add-5`](#add-5-fn) has been defined [(e.{{add-5-fn}})](#add-5-fn) and the 
corresponding tests specified [(e.{{add-5-tests}})](#add-5-tests). There are additional entries for [`add-5`](#add-5-fn) in the readme as well as also being scattered around in the readme and various other documents [(e.{{add-5-readme}})](#add-5-readme). 

This version of this library has been released as **version 1.0**
"

[[{:tag "add-5-fn" :title "src/fami/operations.clj"}]]
(comment
(ns fami.operations)

(defn add-5 
  "add-5 is a function that takes any number of arguments 
   and adds 5 to the sum"
  [& ns]
  (apply + 5 ns))
  
)
[[{:tag "add-5-tests" :title "src/fami/test-operations.clj"}]]
(comment
(ns fami.test-operations
  (:require [fami.operations :refer :all]
            [midje.sweet :refer :all]))

(fact "add-5 should increment any list of numbers by 5"
  (add-5 5)  => 10
  (add-5 1 2 3 4) => 15))

[[{:tag "add-5-readme" :title "readme.md, operations.md"}]]
(comment
...
  
Here are some of the use cases for add-5

(add-5 5)    ;; => 10
(add-5 1 2 3 4)   ;; => 15

...
)

[[:subsection {:title "Version Two"}]]

"
All is well and the library is super successful with many users. The code undergoes refactoring and it is decided that the original `add-5` [(e.{{add-5-fn}})](#add-5-fn) is too powerful and so it must be muted to only accept one argument. An additional function `add-5-multi` is used to make explicit that the function is taking multiple arguments [(e.{{add-5-v2}})](#add-5-v2). The tests throw an exception [(e.{{add-5-v2-failure}})](#add-5-v2-failure), and are quickly fixed [(e.{{add-5-v2-tests}})](#add-5-v2-tests)

This version of this library has been released as **version 2.0**
"

[[{:tag "add-5-v2" :title "src/fami/operations.clj &nbsp;-&nbsp; v2"}]]
(comment
(ns fami.operations)

(defn add-5 [n]   ;; The muted version
  "add-5 is a function that takes a number and adds 5 to it"
  (+ n 5))

(defn add-5-multi 
  "add-5-multi is a function that takes any number of arguments 
   and adds 5 to the sum"
  [& ns]
  (apply + 5 ns))
)

[[{:tag "add-5-v2-failure" :title "faliure message"}]]
(comment
FAIL "add-5 should increment any list of numbers by 5"
"Expected: 15
 Actual: clojure.lang.ArityException - Wrong number of args (4) passed to: fami.operations$add-5")

 [[{:tag "add-5-v2-tests" :title "src/fami/test-operations.clj"}]]
 (comment
 (ns fami.test-operations
   (:require [fami.operations :refer :all]
             [midje.sweet :refer :all]))

 (fact "add-5 should increment only one input by 5"
   (add-5 5) => 10
   (add-5 1 2) => (throws clojure.lang.ArityException))
   
 (fact "add-5-multi should increment any list of numbers by 5"
   (add-5-multi 1 2 3 4) => 15))


[[:subsection {:title "A Bug Surfaces"}]]

"Although the tests are correct, the documentation is not. Anyone using this library can potentially have the `clojure.lang.ArityException` bug if they carefully followed instructions in the documentation. 

This is a trival example of a much greater problem. When a project begins to evolve and codebase begins to change, the documentation then becomes incorrect. Although source and test code can be isolated through testing, fixing documentation is a miserable and futile exercise of cut and paste. With no real tool to check whether code is still valid, the documentation become less and less correct until all the examples have to be rechecked and the documention rewritten.

**Then the codebase changes again ...**

Once the library has been release to the world and people have already started using it, there is no taking it back. Bugs propagate through miscommunication. Miscommunication with machines can usually be contained and fixed. Miscommunication with people becomes potentially more difficult to contain."

[[:chapter {:title "Testable Documents"}]]

[[:section {:title "Tests are Clearer than Text"}]]

"
The best description for our functions are not found in *source files* but in the *test files*. Test files *are* potentially the best documentation because they provide information about what a function outputs, what inputs it accepts and what exceptions it throws. Instead of writing vague phrases ([e.{{split-source}}](#split-source)) in the source code, we can write the descriptions of what a function does directly ([e.{{split-tests}}](#split-tests)). 
"

[[{:tag "split-source" :title "source code (how to do something)"}]]
(defn split-string
  "The split-string function is used to split a string 
  in two according to the idx that is passed."
  [s idx]
  [(.substring s 0 idx) (.substring s idx)])

[[{:tag "split-tests" :title "test code (how something is used)"}]]
(fact
  (split-string "abcde" 3)  
  => ["abc" "de"])

"
Most test files however are usually hidden away in some deep dark corner of the project, usually neglected until a bug in the library occurs and we are forced to run our test framework to figure out what went wrong. And when a project says: **please read the test files for examples**, the common consensus is that the developer has been too slack to write proper documentation.

The biggest problem in documenting is having to to deal with change. The bigger the documentation the more there is to fear when change occurs.
"

;;[[:file {:src "test/midje_doc/clojure_tutorial.clj"}]]

