(ns leiningen.midje-doc.scaffold
  (:require [rewrite-clj.zip :as z]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.midje-doc.common :refer :all]))

(defn func-info [zloc]
  (let [tagloc  (z/down zloc)
        nameloc (z/right tagloc)
        [doc docloc]   (let [docloc (z/right nameloc)]
                         (if (-> docloc z/tag #{:token :multi-line})
                           [(z/sexpr docloc) docloc]
                           [nil nameloc]))
        [attr attrloc] (let [attrloc ( z/right docloc)]
                         (if (-> attrloc z/tag (= :map))
                           [(z/sexpr attrloc) attrloc]
                           [{} attrloc]))]
    [(z/sexpr nameloc) doc attr]))


(defn create-fact-meta [project namespace name attrs]
  (format "^{:refer %s/%s%s :added \"%s\"}"
          namespace name
          (if (empty? attrs) ""
              (apply str (map (fn [[k v]]
                                (-> (str " " (with-out-str (prn k v)))
                                    (.replace "\n" ""))) attrs)))
          (if-let [version (and attrs
                                (:version attrs))]
            version
            (:version project))))

(defn create-fact-form [namespace doc]
  (let [lines (->> (string/split (str "(fact \"" doc "\")") #"\n")
                   (map #(.replaceAll % "^  " "")))]
    (string/join "\n" lines)))

(defn create-fact-entry [project namespace funcloc]
  (let [[name doc attrs] (func-info funcloc)
        meta  (create-fact-meta project namespace name attrs)
        _     (println "DOC:" doc  "NAME:" name)
        fact  (create-fact-form namespace
                                (-> (or doc (str name))
                                    (.replaceAll "\\\"" "\\\\\"")))]
    (str meta "\n" fact)))

(defn generate-fact-forms
  ([project namespace zloc] (generate-fact-forms project namespace zloc []))
  ([project namespace zloc output]
     (cond (nil? zloc) output

           (is-func-form? zloc)
           (recur project namespace (z/right zloc) (conj output (create-fact-entry project namespace zloc)))

           :else
           (recur project namespace (z/right zloc) output))))

(defn generate-test [project source]
  (let [zloc  (z/of-file source)
        nsloc (->> (iterate z/right zloc)
                   (filter #(is-func-form? % #{'ns}))
                   (first))
        namespace (-> nsloc z/down z/right z/sexpr)]
    (->> (generate-fact-forms project namespace nsloc)
         (cons (format "(ns %s\n  (:use midje.sweet)\n  (:require [%s :refer :all]))"
                       (str namespace "-test") namespace))
         (string/join "\n\n"))))

(defn scaffold [project]
  (let [src-dir   (-> project :source-paths first)
        src-files (->> (io/as-file src-dir)
                       (file-seq)
                       (filter #(->> % (.getName) (re-find #"\.cljs?$"))))]
    (doseq [src-file src-files]
      (let [test-file (io/as-file (test-file-path project src-file))]
        (println "FILE:" src-file "->" test-file)
        (when-not (.exists test-file)
          (let [output (generate-test project src-file)]
            (-> test-file (.getParent) (io/as-file) (.mkdirs))
            (spit test-file output)))))))

(comment
  (scaffold
   (leiningen.core.project/read "example/hara/project.clj"))

  (scaffold
   (leiningen.core.project/read "../iroh/project.clj"))

  (println (generate-test "src/leiningen/scholastic/analysis/test.clj"))

  (->> (z/of-file "src/leiningen/scholastic/analysis/test.clj")
       (z/right)
       (z/right)
       (z/right)
       (z/right)
       (z/sexpr)
       )
)
