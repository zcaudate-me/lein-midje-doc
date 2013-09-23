(defproject lein-midje-doc "0.0.10"
  :description "Documentation generator for midje"
  :url "http://www.github.com/zcaudate/lein-midje-doc"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [rewrite-clj "0.3.0"]
                 [hiccup "1.0.4"]
                 [markdown-clj "0.9.29"]
                 [stencil "0.3.2"]
                 [me.raynes/conch "0.5.0"]
                 [org.clojars.zcaudate/watchtower "0.1.2"]
                 [org.xhtmlrenderer/flying-saucer-pdf "9.0.2"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}}
  :documentation {:files {"index"
                          {:input "test/midje_doc/midje_doc_guide.clj"
                           :title "midje-doc"
                           :sub-title "write testable documentation, not literate programs"
                           :author "Chris Zheng"
                           :email  "z@caudate.me"}
                          "my-first-document"
                          {:input "test/midje_doc/my_first_document.clj"
                           :title "title goes here"
                           :sub-title "subtitle goes here"
                           :author "name"
                           :email  "email@domain.com"}}}
  :eval-in-leiningen true)
