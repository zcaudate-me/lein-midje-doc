(ns leiningen.midje-doc.test-parser
  (:require [midje.sweet :refer :all]
            [leiningen.midje-doc.run.parser :as t]
            [leiningen.midje-doc.run.renderer :as r]
            [rewrite-clj.zip :as z]
            [hiccup.core :as html]
            [me.raynes.conch :refer [programs]]))

(fact "chapter-element?"
  (t/chapter-element?
   (z/of-string "[[:chapter {}]]"))
  => true)

(fact "attribute?"
  (t/attribute?
   (z/of-string "[[{:hello :world}]]"))
  => true

  (t/parse-attribute
   (z/of-string "[[{:hello :world}]]") {} {})
  => [[] {:attrs {:hello :world}} {}])


(fact "parse-chapter-element"
  (t/parse-chapter-element
       (z/of-string "[[:chapter {:title \"Standard Exception Flow\"}]]") {:chapter 1} {})
  '[({:tag "standard-exception-flow", :num "2", :type :chapter, :title "Standard Exception Flow"})
    {:example 1, :section 1, :subsubsection 1, :fact-level 0, :subsection 1, :chapter 2}
    {"standard-exception-flow" "2"}])


(fact
  (t/parse-image-element
   (z/of-string "[[:image {:src \"image.png\" :height \"100px\"}]]")
   {:chapter 1 :image 1} {})
  => [[{:num "2", :type :image, :height "100px", :src "image.png"}]
      {:image 2, :chapter 1} {}])

(fact
  (t/parse-element-attribute
   (z/of-string "[[:document {:deco 212}]]"))
  => {:deco 212})

(fact "oeuoeu"
  (t/ns-form?
   (z/of-string "(ns pe.eu)"))
  => true)

(t/parse-code
 (z/of-string "(+ 1 1)") {} {})


(z/of-string "1 ;; hello ")
(t/update-code
 (-> (z/of-string "(+ 1 1) ;; hello \n ;; heloo") z/right* z/right*) {} {})

(fact
 (t/parse-content
   (z/of-string "(ns hello.world
                    (:require [clojure.java.io :as j]))

                [[:chapter {:tag ch1 :title \"Parcels and Programs\"}]]
                [[:section {:tag hello1 :contet \"In the Mood for Prayer\"}]]

                [[:ns]]

                [[:chapter {}]]
                \"hello\"
                [[:section {:tag hello2 :content \"Venus Flytrap\"}]]
                [[:section {:tag hello3 :content \"Hello\"}]]")) )

(z/of-string "() ;;hello")

(comment
  (t/parse-content
          (z/of-string "[[:chapter {:tag \"heel\"}]]
 [[:chapter {}]]
 [[:section {}]]
 (fact
   [[:section {}]]
   \"This is an example from Example {{first-eq}} below:\"
   [[{:tag \"first-eq\"}]]
   (inc 1) => (int 12
                 [23 454])
   [[:chapter {}]])"))


  #_(spit "ribol.html"
        (r/render-elements
         (t/parse-content
          (z/of-file
           "/Users/Chris/dev/chit/ribol/test/ribol/test_ribol_strategies.clj"))))

  (fact
    (binding [r/*plain* true]
      (r/render-html-doc
       "minimal-ribol.html"
       {:title "Ribol Strategies" :sub-title "Use Cases for Conditional Resets" :author "Chris Zheng" :email "z@caudate.me" :version "0.0.9" :revision "0.1"}
       (t/parse-content
        (z/of-string "[[:file {:src \"/Users/Chris/dev/chit/ribol/test/ribol/test_ribol_strategies.clj\"}]]"))))
    => nil)

  (fact
    (binding [r/*plain* true]
      (r/render-html-doc
       "minimal-ribol.html"
       {:title "Ribol Strategies" :sub-title "Use Cases for Conditional Resets" :author "Chris Zheng" :email "z@caudate.me" :version "0.0.9" :revision "0.1"}
       (t/parse-content (z/of-file "test/midje_doc/midje_doc_guide.clj"))))
    => nil)

  (r/render-heading
   {:title "Ribol Strategies" :sub-title "Use Cases for Conditional Resets" :author ""})


  (fact
    (t/parse-content
     (z/of-string "(int 1) => 1"))
    => [{:tag nil, :num "0.1", :content "\n(int 1)\n => 1", :type :code}])

  (clojure.java.shell/sh "bash" "echo" "'ouoeuoe'")
)
