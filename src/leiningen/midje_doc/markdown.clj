(ns leiningen.midje-doc.markdown
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.midje-doc.common :refer :all]))

(defn filter-clojure-forms
  ([lines] (filter-clojure-forms lines "" {} []))
  ([[[num current] & more :as lines] prev state output]
     (cond  (empty? lines)
            output

           (or (nil? current)
               (all-whitespace? current))
           (recur more current state output)

           (:in-clj state)
           (cond (end-block? current)
                 (recur more current (dissoc state :in-clj) output)

                 :else
                 (recur more current state (conj output [num current])))

           (all-whitespace? current)
           (recur more current  state output)

           (and (or (all-whitespace? prev)
                    (has-spacing? prev 4))
                (has-spacing? current 4))
           (recur more current state (conj output [num (subs current 4)]))

           (has-quote? current "clojure")
           (recur more current (assoc state :in-clj true)  output)

           :else
           (recur more current state output))))

(defn merge-line-numbers [[num line]]
  (format "^{:line %s} %s"
          num line))

(defn parse-clojure-forms [path]
  (let [lines (->> (io/as-file path)
                   (io/reader)
                   (line-seq)
                   (map-indexed (fn [i v] [i v])))]
    (->> lines
         (filter-clojure-forms)
         (map merge-line-numbers)
         (map uncomment-arrows)
         (string/join "\n")
         (#(str "[\n" % "\n]"))
         (read-string))))

(defn make-fact-form [filename f1 f2 f3]
  (list 'fact
        (format "EXPRESSION `%s` AT LINE %s IN FILE `%s`"
                f1
                (-> f1 meta :line)
                filename)
        f1 f2 f3))

(defn wrap-fact-forms
  ([forms filename] (wrap-fact-forms forms filename []))
  ([[f1 f2 f3 & more :as forms] filename output]
     (cond (empty? forms) output

           (= f2 '=>)
           (recur more filename (conj output (make-fact-form filename f1 f2 f3)))

           :else
           (recur (cons f2 (cons f3 more)) filename (conj output f1)))))

(defmacro with-ns [ns & forms]
  `(binding [*ns* (the-ns ~ns)]
     ~@(map (fn [form] `(eval '~form)) forms)))

(defmacro with-tmp-ns [& forms]
  `(try
     (create-ns 'sym#)
     (let [res# (with-ns 'sym#
                            (clojure.core/refer-clojure)
                            ~@forms)]
       res#)
     (finally (remove-ns 'sym#))))

(defn test-markdown [path]
  (-> (parse-clojure-forms path)
      (wrap-fact-forms path)
      (->> (cons '(use 'midje.sweet))
           (apply list `with-tmp-ns))
      (prn)
      (with-out-str)
      (read-string)
      (eval)))


(comment
  (filter-clojure-forms [[1 "```clojure"]
                         [2 "(use 'hello.core)"]
                         [3 "(hello \"world\") ;;=> world"]
                         [4 "(+ 1 2)"]
                         [5 "```"]
                         ])

  (read-string "[(use 'hello.core)\n(hello \"world\") ;;=> world\n]")

  (filter-clojure-forms [[1 "hello there"]
                         [2 ""]
                         [3 "    (+ 1 2)"]
                         [4 "    (+ 1 2)"]
                         ])

  ;;(>pst)
  (-> (parse-clojure-forms "README.md")
      (wrap-fact-forms "README.md")
      (->> (cons '(use 'midje.sweet))))

  (require '[midje.data.fact :as f])

  (use 'midje.repl)
  (meta (first (fetch-facts :all)))

  (do (test-markdown "README.md"))

  (leiningen.scholastic.markdown/with-tmp-ns (use (quote midje.sweet)) (+ 1 2) (fact "README.md, line 21: (+ 1 2)" (+ 1 2) => 3) (fact "README.md, line 22: (+ 5 6)" (+ 5 6) => 7))





  ;;(>pst)
  ;;(parse-clojure-forms "README.md")
)
