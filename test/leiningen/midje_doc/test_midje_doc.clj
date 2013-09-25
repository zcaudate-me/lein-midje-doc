(ns midje-doc.test-midje-doc
  (:require [leiningen.midje-doc :refer [process-doc]]))


(process-doc "my-first-document"
             {:input "test/midje_doc/my_first_document.clj"
              :title "title goes here"
              :sub-title "subtitle goes here"
              :author "name"
              :email  "email@domain.com"
              :version "v0.0.12"
              :website "www.example.com"})
