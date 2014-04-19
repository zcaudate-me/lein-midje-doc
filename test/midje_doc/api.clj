(ns midje-doc.api
  (:use midje.sweet))

[[:chapter {:title "API Reference"}]]

[[:section {:title "The Basics"}]]

[[:subsection {:title "elements"}]]
"Elements are constructed using a tag and a map contained within double square brackets. The format is shown in ([e.{{elements-ex1}}](#elements-ex1)). Elements tags have been inspired from latex:
 - [Sectioning Elements](#sectioning-elements)
 - [Text Elements](#text-elements)
 - [Code Elements](#code-elements)
 - [Additional Elements](#additional-elements)

Clojure strings are treated as paragraph elements whilst clojure forms are treated as code elements. `fact` and `comment` forms are also considered code elements. Elements will be described in detail in their respective sections.
"

[[{:title "Element Notation" :tag "elements-ex1"}]]
(comment
  [[<tag> {<key1> <value1>, <key2> <value2>}]]

  for example

  [[:chapter {:title "Hello World" :tag "hello"}]])

[[:subsection {:title "attributes"}]]
"Attribute add additional meta-data to elements. They are written as a single hashmap within double square brackets. Attributes mean nothing by themselves. They change the properties of elements directly after them ([e.{{attributes-ex1}}](#attributes-ex1)). Multiple attributes can be stacked to modify an element ([e.{{attributes-ex2}}](#attributes-ex2))."

"* * *"
[[{:title "Attribute Notation" :tag "attributes-ex1"}]]

(comment
  [[{:tag "my-paragraph"}]]
  [[:paragraph {:content "This is a paragraph"}]]

  is equivalent to

  [[:paragraph {:content "This is a paragraph"
                :tag "my-paragraph"}]])

"* * *"

[[{:title "Stacked Attributes" :tag "attributes-ex2"}]]
(comment
  [[{:numbered false}]]
  [[{:lang "python"}]]
  [[:code "
a = 1 + 1
print a   # outputs 2
"]]

  produces the following python code)



[[{:numbered false}]]
[[{:lang "python"}]]
[[:code "
a = 1 + 1
print(a)   # outputs 2
"]]

"* * *"

[[:section {:title "Sectioning Elements"}]]

"Sectioning elements are taken from latex and allow the document to be organised into logical sections. From highest to lowest order of priority, they are: `:chapter`, `section`, `subsection` and `:subsubsection`, giving four levels of organisation.

The numbering for elements are generated in sequencial order: `(1, 2, 3 ... etc)` and a tag can be generated from the title or specified for creating links within the document. `:chapter`, `section` and `subsection` elements are list in the table of contents using tags.

For example, I wish to write a chapter about animals and have organised content into categories shown in ([e.{{animal-categories}}](#animal-categories)), it is very straight forward to turn this into sectioning elements ([e.{{animal-elements}}](#animal-elements)) which will then generate the sectioning numbers for the categories as well as their tags listed as a  comment ([e.{{animal-generated-numbering}}](#animal-generated-numbering)):
"
[[{:title "Animal Categories"}]]
(comment
  Animals
  - Mammals
  - Birds
  *- Can Fly
  **- Eagle
  **- Hummingbird
  *- Flightless
  **- Penguin)


[[{:title "Animal Elements"}]]
(comment
  [[:chapter {:title "Animals"}]]
  [[:section {:title "Mammals"}]]
  [[:section {:title "Birds"}]]
  [[:subsection {:title "Can Fly"}]]
  [[:subsubsection {:title "Eagle"}]]
  [[:subsubsection {:title "Hummingbird"}]]
  [[:subsection {:title "Flightless"}]]
  [[:subsubsection {:title "Penguin"}]])

[[{:title "Animal Generated Numbering"}]]
[[:code
"
1 Animals                  ;; animals
  1.1 Mammals              ;; mammals
  1.2 Birds                ;; birds
    1.2.1 Can Fly          ;; can-fly
      1.2.1.1 Eagle        ;; eagle
      1.2.1.2 Hummingbird  ;; hummingbird
    1.2.2 Flightless       ;; flightless
      1.2.2.1 Penguin      ;; penguin
"]]

[[:section {:title "Content Elements"}]]

"Content elements include `:paragraph`, `:image`, and `file` elements."

[[:subsection {:title ":paragraph" :tag "paragraph"}]]

"Paragraph elements should make up the bulk of the documentation. They can be written as an element ([e.{{paragraph-element}}](#paragraph-element)) or in the usual case, as a string ([e.{{paragraph-string}}](#paragraph-string)). The string is markdown with templating - so that chapter, section, code and image numbers can be referred to by their tags ([e.{{markdown-string}}](#markdown-string))."

[[{:title "Paragraph Element"}]]
(comment
  [[:paragraph {:content "Here is some content"}]])

[[{:title "Paragraph String"}]]
(comment "Here is some content")

[[{:title "Markdown String"}]]
(comment
  [[:chapter {:title "Chapter Heading" :tag "ch-heading"}]]

  "
# Heading One
Here is some text.
Here is a tag reference to Chapter Heading - {{ch-heading}}

- Here is a bullet point
- Here is another one")

[[:subsection {:title ":image"}]]

"The `:image` element embeds an image as a figure within the document. It is numbered and can be tagged for easy reference. The code example in ([e.{{clojure-logo-element}}](#clojure-logo-element)) produces the image seen in [Figure {{clojure-logo}}](#clojure-logo)."

[[{:tag "clojure-logo-element" :title ":image tag example"}]]
(comment
  [[:image {:tag "clojure-logo" :title "Clojure Logo (source clojure.org)"
            :src "http://clojure.org/space/showimage/clojure-icon.gif"}]])

[[:image {:tag "clojure-logo" :title "Clojure Logo (source clojure.org)"
          :src "http://clojure.org/space/showimage/clojure-icon.gif"}]]

[[:subsection {:title ":file"}]]

"The `:file` element allows inclusion of other files into the document. It is useful for breaking up a document into managable chunks. A file element require that the `:src` attribute be specified. A high-level view of a document can thus be achieved, making the source more readable ([e.{{file-element}}](#file-element)). This is similar to the `\\include` element in latex."

[[{:tag "file-element" :title ":file tag example"}]]
(comment
  [[:file {:src "test/docs/first_section.clj"}]]
  [[:file {:src "test/docs/second_section.clj"}]]
  [[:file {:src "test/docs/third_section.clj"}]])

[[:section {:title "Code Elements"}]]

"Code displayed in documentation are of a few types:

  1. Code that needs to be run (normal clojure code)
  2. Code that needs verification taking input and showing output. (midje fact)
  3. Code that should not be run (namespace declaration examples)
  4. Code in other languages

The different types of code can be defined so that code examples render properly using a variety of methods
"

[[:subsection {:title "normal s-expressions"}]]
"Normal s-expressions are rendered as is. Attributes can be added for grouping purposes. The source code shown in ([e.{{c-add-src}}](#c-add-src)) would render the outputs ([e.{{c-add-1}}](#c-add-1)) and ([e.{{c-add-2}}](#c-add-2))"

[[{:title "seperating code blocks through attributes" :tag "c-add-src"}]]
(comment
  [[{:title "add-n definition" :tag "c-add-1"}]]
  (defn add-n [n]
    (fn [x] (+ x n)))

  [[{:title "add-4 and add-5 definitions" :tag "c-add-2"}]]
  (def add-4 (add-n 4))
  (def add-5 (add-n 5)))

[[{:title "add-n definition" :tag "c-add-1"}]]
(defn add-n [n]
  (fn [x] (+ x n)))

[[{:title "add-4 and add-5 definitions" :tag "c-add-2"}]]
(def add-4 (add-n 4))
(def add-5 (add-n 5))


[[:subsection {:title "facts form"}]]

"
Documentation examples put in `facts` forms allows the code to be verified for correctness using `lein midje`. Document element notation still be rendered except before and after the midje arrows (**=>**). Consecutive code within a fact form will stacked as one common code block. An example is given below where the source ([e.{{c-facts-src}}](#c-facts-src)) gives two outputs: ([e.{{c-facts-1}}](#c-facst-1)) and ([e.{{c-facts-2}}](#c-facts-2))

"
[[{:tag "c-facts-src" :title "Facts Form Source"}]]
(comment
  [[{:tag "facts-form-output" :title "Facts Form Output"}]]
  (facts
    [[{:title "Definining an atom" :tag "c-facts-1"}]]
    (def a (atom 1))
    (deref a) => 1

    [[{:title "Updating the atom" :tag "c-facts-2"}]]
    (swap! a inc)
    (deref a) => 2))

(facts
  [[{:title "Definining an atom" :tag "c-facts-1"}]]
  (def a (atom 1))
  (deref a) => 1

  [[{:title "Updating the atom" :tag "c-factss-2"}]]
  (swap! a inc)
  (deref a) => 2)

[[:subsection {:title "fact form"}]]

"For an entire block to be embedded in code, use the `fact` form. The source ([e.{{c-fact-src}}](#c-fact-src)) will render the output ([e.{{fact-form-output}}](#fact-form-output))"

[[{:tag "c-fact-src" :title "Fact Form Source"}]]
(comment
  [[{:tag "fact-form-output" :title "Fact Form Output"}]]
  (fact
    (def a (atom 1))
    (deref a) => 1

    (swap! a inc 1)
    (deref a) => 2))

[[{:title "Fact Form Output"}]]
(fact
  (def a (atom 1))
  (deref a) => 1

  (swap! a inc)
  (deref a) => 2)

[[:subsection {:title "comments"}]]

"Comments are clojure's built-in method of displaying non-running code and so this mechanisim is used in clojure for displaying code that should not be run, but still requires display. For example, ([e.{{c-com-src}}](#c-com-src)) will output ([e.{{c-com-1}}](#c-com-1)) without interferring with the midje tests."

[[{:title "Switching to a new namespace" :tag "c-com-src"}]]
(comment
  [[{:title "Switching to a new namespace" :tag "c-com-1"}]]
  (comment
    (in-ns 'hello.world)
    (use 'clojure.string)
    (split "Hello World" #"\s") ;=> ["Hello" "World"]
    ))

[[{:title "Switching to a new namespace" :tag "c-com-1"}]]
(comment
  (in-ns 'hello.world)
  (use 'clojure.string)
  (split "Hello World" #"\s") ;=> ["Hello" "World"]
)


[[:subsection {:title ":code"}]]
"The most generic way of displaying code is with the `:code` tag. It is useful when code in other languages are required to be in the documentation."

[[:subsubsection {:title "Python Example"}]]

"The source and outputs are listed below:"

[[{:title "Python for Loop Source" :tag "c-py-src"}]]
(comment
  [[{:lang "python" :title "Python for Loop" :tag "c-py-1"}]]
  [[:code
    "
myList = [1,2,3,4]
for index in range(len(myList)):
    myList[index] += 1
print myList
"
    ]])


[[{:lang "python" :title "Python for Loop" :tag "c-py-1"}]]
[[:code
"
myList = [1,2,3,4]
for index in range(len(myList)):
    myList[index] += 1
print myList
"
]]

[[:subsubsection {:title "Ruby Example"}]]

"The source and outputs are listed below:"

[[{:title "Ruby for Loop Source" :tag "c-rb-src"}]]
(comment
  [[{:lang "ruby" :title "Ruby for Loop" :tag "c-rb-2"}]]
  [[:code
    "
array.each_with_index do |element,index|
  element.do_stuff(index)
end
"
    ]])


[[{:lang "ruby" :title "Ruby for Loop" :tag "c-rb-2"}]]
[[:code
"
array.each_with_index do |element,index|
  element.do_stuff(index)
end
"
]]
