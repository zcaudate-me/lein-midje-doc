(ns midje-doc.test-midje-doc
  (:require [leiningen.midje-doc :refer [process-doc]] :reload))


#_(process-doc "my-first-document"
             {:input "test/midje_doc/my_first_document.clj"
              :title "title goes here"
              :sub-title "subtitle goes here"
              :author "name"
              :email  "email@domain.com"
              :version "v0.0.12"
              :website "www.example.com"})

(process-doc "my-first-document"
             {:input "/Users/zhengc/dev/chit/purnam-angular/test/cljs/midje_doc/purnam_angular_guide.cljs"
                           :title "purnam.angular"
                           :sub-title "Angular.js DSL in Clojurescript"
                           :author "Chris Zheng"
                           :email  "z@caudate.me"
              :tracking "UA-31320512-2"}
             {:version "0.3.2"
              :description "Angular.js DSL in Clojurescript"})
