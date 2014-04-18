(ns leiningen.midje-doc.import
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.printer :as p]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.midje-doc.common :refer :all])
  (:refer-clojure :exclude [import]))

(defn fact-attr [zloc]
  (and (= :meta (z/tag zloc))
       (-> zloc z/value)
       (-> zloc z/value p/->string read-string)))

(defn fact-form [zloc]
  (if (= :meta (z/tag zloc))
    (fact-form (-> zloc z/down z/right))
    (and (= :list (z/tag zloc))
         (= 'fact (-> zloc z/down z/sexpr))
         zloc)))

(defn string-form? [zloc]
  (condp = (z/tag zloc)
    :token   (-> zloc z/value string?)
    :multi-line true
    false))

(defn arrow-form? [zloc]
  (condp = (z/tag zloc)
    :token (-> zloc z/value (= '=>))
    false))

(defn fact-arrow-rep [zloc]
  (loop [[tloc output] [zloc []]]
    (cond (nil? tloc) [tloc output]

          (= :newline (z/tag tloc))
          [(z/right* tloc) (conj output (z/node tloc))]

          (= :comment (z/tag tloc))
          [(z/right* tloc) [(z/node zloc)
                           [:whitespace " "]
                           [:newline
                            (.replaceAll (z/value tloc) "^;*" "")]
                           [:newline "\n"]]]

          :else
          (recur [(z/right* tloc) (conj output (z/node tloc))]))))

(defn fact-to-doc-nodes
  ([zloc]
     (fact-to-doc-nodes zloc []))
  ([zloc output]
     (cond (nil? zloc) output

           (and (= :meta (z/tag zloc))
                (= [:token :hidden] (z/value zloc)))
           output

           (string-form? zloc)
           (recur (z/right* zloc)
                  (conj output
                        [:newline (string/triml (z/sexpr zloc))]))

           (arrow-form? zloc)
           (let [[nloc nodes] (fact-arrow-rep zloc)]
             (recur nloc (-> output (concat nodes) (vec))))

           :else
           (recur (z/right* zloc) (conj output (z/node zloc))))))

(defn compose [& fs]
  (fn comp-fn
    ([i] (comp-fn i fs))
    ([i [f & more]]
       (cond (nil? i) nil
             (nil? f) i
             :else (recur (f i) more)))))

(defn fact-doc [zloc]
  (->> ((compose z/down z/right z/down z/right) zloc)
       (fact-to-doc-nodes)
       (map p/->string)
       (apply str)))

(defn fact-pair [zloc]
  (when-let [_ (fact-form zloc)]
    [(fact-attr zloc) (fact-doc zloc)]))

(defn function-index [test-file]
  (->> (z/of-file test-file)
       (iterate #(if % (z/right %)))
       (take-while identity)
       (map fact-pair)
       (filter identity)
       (map (fn [[attrs docs]]
              [(-> attrs :refer name) {:attrs (dissoc attrs :refer)
                                       :docs  docs}]))
       (into {})))

(defn insert-docstring [zloc idx]
  (let [nloc (-> zloc z/down z/right)
        nstr (str (z/value nloc))]
    (if-let [{:keys [attrs docs]} (get idx nstr)]
      (let [dloc (-> nloc
                     (fast-zip.core/insert-right
                      [:newline "\n"])
                     (z/right*)
                     (fast-zip.core/insert-right
                      [:whitespace "  "])
                     (z/right*)
                     (fast-zip.core/insert-right
                      (apply vector :multi-line
                             (string/split-lines docs)))
                     (z/right*))
            attrloc (if (empty? attrs)
                      dloc
                      (-> dloc
                          (fast-zip.core/insert-right
                           [:newline "\n"])
                          (z/right*)
                          (fast-zip.core/insert-right
                           [:whitespace " "])
                          (z/right*)
                          (z/insert-right attrs)))]
        (z/up attrloc))
      zloc)))

(defn import-documentation [source-file test-file]
  (let [zloc (z/of-file source-file)
        idx  (function-index test-file)
        ;;_    (prn idx)
        nloc (loop [ploc zloc]

               (let [nloc (z/right ploc)]
                 (cond (nil? nloc) ploc

                       (is-func-form? nloc)
                       (recur
                        (-> nloc
                            minus-docstring
                            (insert-docstring idx)))

                       :else (recur nloc)
                       )))]
    (spit source-file (with-out-str (z/print-root nloc)))))


(defn import [project]
  (let [src-dir   (-> project :source-paths first)
        src-files (->> (io/as-file src-dir)
                       (file-seq)
                       (filter #(->> % (.getName) (re-find #"\.cljs?$"))))]
    (doseq [src-file src-files]
      (let [test-file (io/as-file (test-file-path project src-file))]
        (when (.exists test-file)
          (import-documentation src-file test-file))))))

(comment

  (import
   (leiningen.core.project/read "../hara/project.clj"))

  (function-index )
  (import-documentation
   "example/hara/src/hara/collection/data_map.clj"
   "example/hara/test/hara/collection/data_map_test.clj")


  (def idx (function-index "example/hara/test/hara/collection/data_map_test.clj"))
  (-> (z/of-file "example/hara/src/hara/collection/data_map.clj")
      z/right
      minus-docstring
      (insert-docstring idx)
      (z/node)
      (p/->string)
      (println))


  {"combine-obj" {:attrs {}, :docs "Looks for the value within the set `s` that matches `v` when\n \n  (combine-obj #{1 2 3} 2 identity)\n  => 2\n\n  (combine-obj #{{:id 1}} {:id 1 :val 1} :id)\n  => {:id 1}\n"}, "combine-to-set" {:attrs {}, :docs "Returns `s` with either v added or combined to an existing set member.\n  (combine-to-set #{{:id 1 :a 1} {:id 2}}\n                  {:id 1 :b 1}\n                  :id merge)\n  => #{{:id 1 :a 1 :b} {:id 2}}"}}


  (fact-pair)

  (fact-form)

  (clojure.repl/pst)
  ((juxt ))



  (def example
  (z/of-string
"^{:refer hara.common.collection/index-by}
 (fact \"Returns a hash-map `m`, with the the values of `m` being the items within
       the collection and keys of `m` constructed by mapping `f` to `coll`.\"

    (defn lst [{:id 1} {:id 2}])

    (index-by :id lst)
    => {1 {:id 1} 2 {:id 2}} ;; <Something else>

    \"This is used to turn a collection into a lookup for better search performance.\"

    (index-by #(-> % :id (* 10)) lst)
    => {10 {:id 1} 20 {:id 2}}

  ^:hidden
  (+ 1 1) => 3)"))

  (->> (->> example fact-form z/down z/right fact-to-docstring)
       (map p/->string)
       (apply str)
       (println))








)
