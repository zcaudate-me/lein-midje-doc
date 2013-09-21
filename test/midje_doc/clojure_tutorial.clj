(ns midje-doc.clojure_tutorial
  (:require [midje.sweet :refer :all]))

[[:chapter {:title "Example: An Introduction to Clojure"}]]

"Clojure is an easy language to learn. We start off by learning the basics in [Section {{basic-clojure}}](#basic-clojure) and things get harder in [Section {{advance-clojure}}](#advance-clojure)"

[[:section {:title "Basic Clojure"}]]

[[:subsection {:title "def"}]]

"We begin by defining `a` and `b` using the `def` special form"

[[{:tag "definitions" :title "def usage"}]]
(def a "Hello")
(def b "There")

(fact
  "The purpose of the `def` form, seen in [e.{{definitions}}](#definitions) is to assign values to symbols. In this case, the symbol `a` has been given the string value `'Hello'` whilst the symbol `b` has been given the string value `'There'`."


  "The 'str' function composes them togther"
  (str a " " b) => "Hello There")

[[:section {:title "Advance Clojure"}]]