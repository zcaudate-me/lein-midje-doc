(ns leiningen.midje-doc.purge
  (:require [rewrite-clj.zip :as z]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.midje-doc.common :refer :all]
            [leiningen.midje-doc.import :refer [function-index]]))

(defn purge-documentation [source-file test-file]
  (let [zloc (z/of-file source-file)
        idx  (function-index test-file)
        nloc (loop [ploc zloc]

               (let [nloc (z/right ploc)]

                 (cond (nil? nloc) ploc

                       (and (is-func-form? nloc)
                            (get idx (-> nloc z/down z/right z/sexpr str)))
                       (recur
                        (-> nloc minus-docstring))

                       :else (recur nloc))))]
    (spit source-file (with-out-str (z/print-root nloc)))))

(defn purge [project]
  (let [src-dir   (-> project :source-paths first)
        src-files (->> (io/as-file src-dir)
                       (file-seq)
                       (filter #(->> % (.getName) (re-find #"\.cljs?$"))))]
    (doseq [src-file src-files]
      (let [test-file (io/as-file (test-file-path project src-file))]
        (when (.exists test-file)
          (purge-documentation src-file test-file))))))


(comment
  (purge
   (leiningen.core.project/read "../hara/project.clj"))

  )
