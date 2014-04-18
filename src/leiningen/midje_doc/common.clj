(ns leiningen.midje-doc.common
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [rewrite-clj.zip :as z]))

(defn printv [v & args]
  (println
   (apply format
          (string/join "\n" (flatten v))
          args)))

(defn end-block? [line]
  (.startsWith line "```"))

(defn has-spacing? [line number]
  (.startsWith line (apply str (repeat number " "))))

(defn all-whitespace? [line]
  (or (= "" line) (re-find #"^[\s\t]+$" line)))

(defn has-quote? [line x]
  (.startsWith line (str "```" x)))

(defn uncomment-arrows [line]
  (.replaceAll line "\\;+=> " "=> "))

;;;; ----

(defn is-func-form?
  ([zloc] (is-func-form? zloc '#{defn defmulti defmacro defn- defmethod}))
  ([zloc forms]
      (if (and (= :list (z/tag zloc))
               (get forms
                    (if-let [ele (-> zloc z/down)]
                      (z/sexpr ele))))
        true
        false)))

(defn minus-docstring [zloc]
  (let [ele (-> zloc z/down z/right z/right)
        ;;_   (println "MINUS-PRE:" (z/sexpr ele))
        ele (if (-> ele z/sexpr string?)
              (z/remove ele)
              ele)
        ele (z/right ele)
        ele (if (-> ele z/tag (= :map))
              (z/remove ele)
              ele)]
    ;;(println "MINUS: "(z/sexpr (z/up ele)))
    (z/up ele)))

;;;; ---- Test/Source Duality

(defn resolve-file-path [file in-dir in-pattern out-dir out-pattern]
  (let [file-path (.getAbsolutePath (io/as-file file))]
    (cond (.contains file-path out-dir) file-path

          (.contains file-path in-dir)
          (-> (subs file-path (count in-dir))
              (.replaceAll in-pattern out-pattern)
              (->> (str out-dir))))))

(defn source-file-path [project file]
  (let [src-dir   (-> project :source-paths first)
        test-dir  (-> project :test-paths first)]
    (resolve-file-path file test-dir "_test\\.(cljs?)$" src-dir ".$1")))

(defn test-file-path [project file]
  (let [src-dir   (-> project :source-paths first)
        test-dir  (-> project :test-paths first)]
    (resolve-file-path file src-dir "\\.(cljs?)$" test-dir "_test.$1")))


(comment
  (def file "project.clj")
  (.replaceAll "hello-test.clj" "_test\\.(cljs?)$" ".$1")

  (defn test-file-path [project file]
    (let [src-dir   (-> project :source-paths first)
          test-dir  (-> project :test-paths first)
          file-path (.getAbsolutePath (io/as-file file))]
      (cond (.contains file-path test-dir) file-path

            (.contains file-path src-dir)
            (-> (subs file-path (count src-dir))
                (.replaceAll "\\.clj$" "-test.clj" )
                (->> (str test-dir))))))


  (source-file-path
   (leiningen.core.project/read "project.clj")
   "src/hello.clj")

  (source-file-path
   (leiningen.core.project/read "project.clj")
   "test/hello-test.clj")
)
