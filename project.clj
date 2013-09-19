(defproject lein-midje-doc "0.0.2"
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
                 [clojure-watch "0.1.9"]
                 [org.xhtmlrenderer/flying-saucer-pdf "9.0.2"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}}
  :eval-in-leiningen true)
