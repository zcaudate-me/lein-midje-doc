(ns midje-doc.midje-doc-guide
  (:require [midje.sweet :refer :all]))

[[:chapter {:title "Quickstart"}]]

"`lein-midje-doc` can be used to generate documents with code examples from `midje` test files. For motivation, see [chapter {{programming-vs-documentation}}](#programming-vs-documentation) and [chapter  {{tooling-for-documents}}](#tooling-for-documents). For api descriptions, see [chapter {{api-reference}}](#api-reference). 
[`lein-midje-doc`](https://www.github.com/zcaudate/lein-midje-doc) has been used to generate its own documention. The source can be found [here](https://github.com/zcaudate/lein-midje-doc/blob/master/test/midje_doc/midje_doc_guide.clj)."

[[:section {:title "Syntax Highlighting"}]]
"For syntax hightlighting of code examples, the pygments python package should be installed. Make sure that the directory containing `pygmentize` is in your `PATH` settings."

[[:section {:title "Generating from Source"}]]
" The html documentation can be generated after downloading the project:
"

[[{:lang "bash" :numbered false}]]
[[:code
"
> git clone https://github.com/zcaudate/lein-midje-doc.git
> cd lein-midje-doc      
> lein midje-doc once
> open ./index.html
"]]

[[:section {:title "Installation"}]]

"`lein-midje-doc` is a leiningen plugin. Install by adding entries in `~/.lein/profiles.clj`:"

[[{:lang "bash" :numbered false}]]
(comment
{:user {:plugins ...
                 [lein-midje-doc "0.0.10"]
                 [lein-midje    "3.0.1"] 
                 ...}}
)

[[:section {:title "Usage"}]]

"
1. Start with a new or existing project.
1. In `project.clj` make sure that `midje` is in the dependencies and place a `:documentation` entry in the `defproject` form ([e.{{qs-project}}](#qs-project)).

1. Create a file in `test/docs/my_first_document.clj` ([e.{{qs-first-doc}}](#qs-first-doc)).

1. Run `lein midje-doc` in a terminal within your project folder. 

1. The output documentation should be generated within the project directory as `/my-first-document.html`. An example of how this should look can be seen [here](http://z.caudate.me/lein-midje-doc/my-first-document.html)

1. Run `lein midje :autotest` in another terminal window. This will ensure that the documentation is correct.

1. Use `live-reload` for the best experience of live documenting"

[[{:tag "qs-project" :title "project.clj"}]]
(comment
(defproject ...
  ... 
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}}
  ...
  :documentation 
  {:files {"<document-name>"             ;; my-first-document
           {:input "<input-file-path>"   ;; test/docs/my_first_document.clj
            :title "<title>"             ;; My First Document
            :sub-title "<sub title>"     ;; Learning how to use midje-doc
            :author "<name>"       
            :email  "<email>"}}}
   ...))
   
[[{:tag "qs-first-doc" :title "test/docs/my_first_document.clj"}]]
(comment
(ns docs.my-first-document
  (:require [midje.sweet :refer :all])) 

[[:chapter {:tag "hello" :title "Hello Midje Doc"}]]

"This is an introduction to writing with midje-doc."

[[:section {:title "Defining a function"}]]

"We define function `add-5`"

[[{:numbered false}]]
(defn add-5 [x]
  (+ x 5))

[[:section {:title "Testing a function"}]]

"`add-5` outputs the following results seen in
 [e.{{add-5-1}}](#add-5-1) and [e.{{add-5-10}}](#add-5-10):"

(fact 
[[{:tag "add-5-1" :title "1 add 5 = 6"}]]
(add-5 1) => 6

[[{:tag "add-5-10" :title "10 add 5 = 15"}]]
(add-5 10) => 15)
)


[[:chapter {:title "Programming vs Documentation"}]]

"
The phrase 'Literate Programming' has been very popular lately. The main idea is that the code is written in a way that allows both a machine *and* a person to understand what is going on. Most people seem to agree that it is a great idea.

However Humans and machines are fundamentally different and rely on completely different methods of communication:

- Communication to Machines are usually very linear and procedural. It involves giving them a specific set of instructions. First Do This, Then Do That.... Machines don't really care what the code does. It just executes whatever code it has been given.


- Communication to Humans usually take a very different form. We wish to be engaged, inspired and taught, not given a sequence of instructions that each break down to even smaller sequences. The best documentation are usally seperated into logical sections like an `overview`, `table of contents`, `list of figures`, `topic chapters`, `subsections`. There are `text`, `code`, `pictures`, even `sound` and `video`. Documentation structure resemble trees, with links between content that connect related topics and content. They do not resemble program code and therefore should be created independently of the machine code itself.

**In short:** Machines are programmed while humans are engaged, inspired and taught. *Programs* are written linearly for machines. *Documentation* are written like a woven lattice for humans. The fundamental structure of programs and documentation are very different from each other. Therefore, thinking that documentation can be automatically generated from doc-strings is a **mechanistic** approach not a **humanistic** one. Documents should be written for people, not machines. Our tools for documentation should reflect this as well. 
"

[[:chapter {:title "Tooling for Documents"}]]

[[:section {:title "Documentation Bugs"}]]
"
Programming is a very precise art form. Programming mistakes, especially the little 
ones, can result in dire consequences and much wasted time. We therefore use tools 
such as debuggers, type checkers and test frameworks to make our coding lives 
easier and our source code correct. 

Documentation is the programmers' means of communicating how to use or build upon a library, usually to a larger audience of peers. This means that any mistakes in the documentation results in 
wasted time for *all involved*. Therefore any mistake in documentation can have a 
*greater effect* than a mistake in source code because it wastes *everybody's* time.

There are various tools for documentation like `latex`, `wiki` and `markdown`. However, none address the issue that code examples in the documentation are not checked for correctness. A fictitious example illustrates how errors in documentation can produced and propagated can be seen in [chapter {{a-bugs-life}}](#a-bugs-life)
"

[[:section {:title "Test Cases vs Docstrings"}]]

"
The best description for our functions are not found in *source files* but in the *test files*. Test files *are* potentially the best documentation because they provide information about what a function outputs, what inputs it accepts and what exceptions it throws. Instead of writing vague phrases ([e.{{split-source}}](#split-source)) in the doc-string, we can write the descriptions of what a function does directly with our tests ([e.{{split-tests}}](#split-tests)). 
"

[[{:tag "split-source" :title "source code (how to do something)"}]]
(defn split-string
  "The split-string function is used to split a string 
  in two according to the idx that is passed."
  [s idx]
  [(.substring s 0 idx) (.substring s idx)])

[[{:tag "split-tests" :title "test code (how something is used)"}]]

(comment
(fact "split-string usage:"

  (split-string "abcde" 1)  
  => ["a" "bcde"]

  (split-string "abcde" 3)  
  => ["abc" "de"]))

"It can be seen that test cases provides a much better explaination for `split-string` than the source doc-string. The irony however is that when a readme says: *`'for documentation, please read the test files'`*, the common consensus is that the project developer is too slack to write proper documentation. However, if we are truly honest with our own faults, this occurs because most programmers are too slack to read tests. **We only want to read pretty documentation.**"

[[:section {:title "Bridging the Divide"}]]

"`lein-midje-doc` plugin attempts to bridge the gap between writing tests and writing documentation by introducing three novel features:"

[[:subsection {:title "Features"}]]

"The features are:
 1. To generate `.html` documentation from a `.clj` test file.
 2. To express documentation elements as clojure datastructures.
 3. To render clojure code and midje facts as code examples.
 4. To allow tagging of elements for numbering and linking.
 "
 
[[:subsection {:title "Benefits"}]]
"In this way, the programmer as well as all users of the library benefits:

 1. All documentation errors can be eliminated.
 2. Removes the need to cut and copy test examples into a readme file.
 3. Entire test suites can potentially be turned into nice looking documentation with relatively little work.
 "

[[:file {:src "test/midje_doc/api.clj"}]]

[[:file {:src "test/midje_doc/bug_example.clj"}]]

[[:chapter {:title "End Notes"}]]

"For any feedback, requests and comments, please feel free to lodge an issue on github or contact me directly.

Chris.
"
