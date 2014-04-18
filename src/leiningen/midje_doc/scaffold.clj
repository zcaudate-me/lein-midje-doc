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


(defn create-fact-meta [namespace name attrs]
  (format "^{:refer %s/%s%s}"
          namespace name
          (if (empty? attrs) ""
              (apply str (map (fn [[k v]]
                                (-> (str " " (with-out-str (prn k v)))
                                    (.replace "\n" ""))) attrs)))))

(defn create-fact-form [namespace doc]
  (let [lines (->> (string/split (str "(fact \"" doc "\")") #"\n")
                   (map #(.replaceAll % "^  " "")))]
    (string/join "\n" lines)))

(defn create-fact-entry [namespace funcloc]
  (let [[name doc attrs] (func-info funcloc)
        meta  (create-fact-meta namespace name attrs)
        fact  (create-fact-form namespace
                                (-> (or doc (str name))
                                    (.replaceAll "\\\"" "\\\\\"")))]
    (str meta "\n" fact)))

(defn generate-fact-forms
  ([namespace zloc] (generate-fact-forms namespace zloc []))
  ([namespace zloc output]
     (cond (nil? zloc) output

           (is-func-form? zloc)
           (recur namespace (z/right zloc) (conj output (create-fact-entry namespace zloc)))

           :else
           (recur namespace (z/right zloc) output))))

(defn generate-test [source]
  (let [zloc  (z/of-file source)
        nsloc (->> (iterate z/right zloc)
                   (filter #(is-func-form? % #{'ns}))
                   (first))
        namespace (-> nsloc z/down z/right z/sexpr)]
    (->> (generate-fact-forms namespace nsloc)
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
        (when-not (.exists test-file)
          (let [output (generate-test src-file)]
            (-> test-file (.getParent) (io/as-file) (.mkdirs))
            (spit test-file output)))))))

(comment
  (scaffold
   (leiningen.core.project/read "example/hara/project.clj"))

  (println (generate-test "src/leiningen/scholastic/analysis/test.clj"))

  (->> (z/of-file "src/leiningen/scholastic/analysis/test.clj")
       (z/right)
       (z/right)
       (z/right)
       (z/right)
       (z/sexpr)
       )
)
