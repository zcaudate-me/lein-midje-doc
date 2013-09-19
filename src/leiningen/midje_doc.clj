(ns leiningen.midje-doc
  (:require [leiningen.midje-doc.renderer :refer [render-html-doc]]
            [leiningen.midje-doc.parser :refer [parse-content]]))

(defn midje-doc
  "I don't do a lot."
  [project & args]
  (println "Hi!"))
