(ns midje-doc.my-first-document
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
  (+ 1 1) => 2
  (+ 1 3) => 2)

(comment
  (+ 1 1) => 2
  (+ 1 3) => 2)


(facts
[[{:tag "add-5-1" :title "1 add 5 = 6"}]]
(add-5 1) => 6

[[{:tag "add-5-10" :title "10 add 5 = 15"}]]
(add-5 10) => 15)
