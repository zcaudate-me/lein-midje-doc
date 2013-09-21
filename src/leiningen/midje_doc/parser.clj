(ns leiningen.midje-doc.parser
  (:require [rewrite-clj.parser :as p]
            [rewrite-clj.zip :as z]
            [stencil.core :as stc]
            [hiccup.util :refer [escape-html]]))

(def new-current
  {:chapter 0
   :section 0
   :subsection 0
   :subsubsection 0
   :example 0
   :image 0
   :fact-level 0})

(defn double-vector?
  ([fzip] (double-vector? fzip nil))
  ([fzip pred]
     (let [sfzip (-> fzip z/down)]
       (and (= :vector
               (z/tag fzip)
               (z/tag sfzip))
            (cond (nil? pred) true
                  :else
                  (let [v (-> sfzip z/down z/node)]
                    (if (fn? pred)
                      (pred v)
                      (= v pred))))))))

(defn file-element? [fzip]
  (double-vector? fzip [:token :file]))

(defn ns-element? [fzip]
  (double-vector? fzip [:token :ns]))

(defn chapter-element? [fzip]
  (double-vector? fzip [:token :chapter]))

(defn include-element? [fzip]
  (double-vector? fzip [:token :include]))

(defn section-element? [fzip]
  (double-vector? fzip [:token :section]))

(defn subsection-element? [fzip]
  (double-vector? fzip [:token :subsection]))

(defn subsubsection-element? [fzip]
  (double-vector? fzip [:token :subsubsection]))

(defn image-element? [fzip]
  (double-vector? fzip [:token :image]))

(defn paragraph-element? [fzip]
  (double-vector? fzip [:token :paragraph]))

(defn code-element? [fzip]
  (double-vector? fzip [:token :code]))

(defn paragraph? [fzip]
  (and (= :token (z/tag fzip))
       (string? (z/value fzip))))

(defn multiline? [fzip]
  (= :multi-line (z/tag fzip)))

(defn attribute? [fzip]
  (double-vector? fzip #(-> % first (= :map))))

(defn whitespace? [fzip]
  (= :whitespace (z/tag fzip)))

(defn newline? [fzip]
  (= :newline (z/tag fzip)))

(defn comment? [fzip]
  (= :comment (z/tag fzip)))

(defn parse-attribute-form [fzip]
  (-> fzip z/down z/next z/sexpr))

(defn parse-element-attribute [fzip]
  (-> fzip z/down z/next z/next z/sexpr))

(defn ns-form? [fzip]
  (and (= :list (z/tag fzip))
       (= 'ns
          (-> fzip z/down z/value))))

(defn fact-form? [fzip]
  (and (= :list (z/tag fzip))
       (= 'fact
          (-> fzip z/down z/value))))

(defn comment-form? [fzip]
  (and (= :list (z/tag fzip))
       (= 'comment
          (-> fzip z/down z/value))))

(defn fact-arrow? [fzip]
  (= [:token '=>] (z/node fzip)))

(defn merge-element-attribute [fzip current]
  (let [attrs (:attrs current)]
    [(merge attrs (parse-element-attribute fzip))
     (dissoc current :attrs)]))

(defn create-slug [title]
  (-> title
      (.replaceAll "[\\.\\'\\`]" "")
      (.toLowerCase)
      (.trim)
      (.split "\\s+")
      (->> (clojure.string/join "-"))))

(defn update-tags [tags attrs current ks]
  (let [tag    (:tag attrs)
        _      (if (get tags tag) (throw (Exception. (str "There is already a tag " tag " used"))))
        tag    (if (and (nil? tag)
                        (:title attrs))
                 (create-slug (:title attrs))
                 tag)
        num    (clojure.string/join "." (map #(get current %) ks))
        tags   (if tag (assoc tags tag num) tags)]
    [num tags tag]))

(defn create-code [current tags]
  (if-let [code (:code current)]
    (let [attrs (:attrs current)]
      (cond (:hide attrs)
            [[] (dissoc current :code :attrs) tags]

            (false? (:numbered attrs))
            [[(assoc attrs :type :code :content code)]
             (dissoc current :code :attrs) tags]

            :else
            (let [current (-> current
                              (update-in [:example] inc))
                  [num tags tag] (update-tags tags attrs current [:chapter :example])]
              [[(assoc attrs :type :code :content code :num num :tag tag
                       :fact-level (or (:fact-level current) 0))]
               (dissoc current :code :attrs) tags])))
    [[] current tags]))

(defn parse-code-trailing-comments [fzip]
  (let [ws   (z/right* fzip)
        cmnt (if ws (z/right* ws))]
    (cond (comment? ws) (z/->string ws)
          (and (comment? cmnt)
               (whitespace? ws))
          (str (z/->string ws) (z/->string cmnt))
          :else "")))

(defn parse-code [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        elems (update-in elems [0 :content]
                         str " " (z/->string fzip)
                         (parse-code-trailing-comments fzip))]
    [elems (dissoc current :fact-arrow) tags]))

(defn parse-comment-form-contents [fzip]
  (let [ini fzip]
    (let [s (z/->string ini)]
      (-> (.substring s 1 (dec (.length s)))
          (.replaceFirst "comment(\\s+)?" "")))))

(comment-form?
 (z/of-string "(comment   \n  (+ 1 2 3) (+ 3 4 5))"))

(comment (parse-comment-form-contents
          (z/of-string "(comment   \n  (+ 1 2 3) (+ 3 4 5))")))

(defn parse-comment-form [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        [attrs current]
        (let [attrs (:attrs current)]
          [attrs (dissoc current :attrs)])
        code (parse-comment-form-contents fzip)
        [nelems current tags]
        (cond
         (:hide attrs)
         [[] (dissoc current :code :attrs) tags]

         (false? (:numbered attrs))
         [[(assoc attrs :type :code :content code)]
          (dissoc current :code :attrs) tags]

         :else
         (let [current (-> current
                           (update-in [:example] inc))
               [num tags tag] (update-tags tags attrs current [:chapter :example])]
           [[(assoc attrs :type :code :content code :num num :tag tag
                    :fact-level (or (:fact-level current) 0))]
            (dissoc current :code :attrs) tags]))]
    [(concat
      elems
      nelems) current tags]))

(defn parse-chapter-element [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        [attrs current]  (merge-element-attribute fzip current)
        chp-rst (dissoc new-current :chapter :image)
        current (-> current
                    (merge chp-rst)
                    (update-in [:chapter] inc))
        [num tags tag]  (update-tags tags attrs current [:chapter])]
    [(concat
      elems
      [(assoc attrs :type :chapter :num num :tag tag)]) current tags]))

(defn parse-section-element [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        [attrs current]  (merge-element-attribute fzip current)
        current (-> current
                    (assoc :subsection 0 :subsubsection 0)
                    (update-in [:section] inc))
        [num tags tag]  (update-tags tags attrs current [:chapter :section])]
    [(concat
      elems
      [(assoc attrs :type :section :num num :tag tag)]) current tags]))

(defn parse-subsection-element [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        [attrs current]  (merge-element-attribute fzip current)
        current (-> current
                    (assoc :subsubsection 0)
                    (update-in [:subsection] inc))
        [num tags tag]    (update-tags tags attrs current
                                       [:chapter :section :subsection])]
    [(concat
      elems
      [(assoc attrs :type :subsection :num num :tag tag)]) current tags]))

(defn parse-subsubsection-element [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        [attrs current]  (merge-element-attribute fzip current)
        current (-> current
                    (update-in [:subsubsection] inc))
        [num tags tag] (update-tags tags attrs current [:chapter :section :subsection :subsubsection])]
    [(concat
      elems
      [(assoc attrs :type :subsubsection :num num :tag tag)]) current tags]))

(defn parse-image-element [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        [attrs current] (merge-element-attribute fzip current)
        current (-> current
                    (update-in [:image] inc))

        [num tags] (update-tags tags attrs current [:image])]
    [(concat
      elems
      [(assoc attrs :type :image :num num)]) current tags]))

(defn parse-paragraph-element [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        [attrs current]  (merge-element-attribute fzip current)]
    [(concat
      elems
      [(assoc attrs :type :paragraph)]) current tags]))

(declare parse-file-element)

(defn parse-paragraph [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        [attrs current] [(:attrs current) (dissoc current :attrs)]]
    [(concat
      elems
      [(assoc attrs :type :paragraph :content (z/value fzip))]) current tags]))

(defn parse-multiline [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        [attrs current] [(:attrs current) (dissoc current :attrs)]]
    [(concat
      elems
      [(assoc attrs :type :paragraph :content
              (clojure.string/join
               "\n" (-> fzip z/node rest)))]) current tags]))

(defn update-code [fzip current tags]
  [[] (update-in current [:code]
                 str "\n" (z/->string fzip)) tags])

(defn parse-arrow [fzip current tags]
  [[]
   (let [sps (apply str (repeat (or (:fact-level current) 0) "  "))]
     (-> current
               (assoc :fact-arrow true)
               (update-in [:code] str "\n\n" sps "=>"))) tags])

(defn parse-attribute [fzip current tags]
  (let [[elems current tags] (create-code current tags)]
    [elems (update-in current [:attrs] merge (parse-attribute-form fzip)) tags]))

(defn parse-ns-form [fzip current tags]
  (let [[elems current tags] (create-code current tags)]
    [elems (assoc current :ns (z/->string fzip)) tags]))

(defn parse-ns-element [fzip current tags]
  [[(assoc (:attrs current) :type :ns :content (str (:ns current)))]
   (dissoc current :attrs) tags])

(defn parse-comment [fzip current tags]
  [[] current tags]
  (if (:code current)
    (let [pv (z/left* fzip)
          pvstr (if (whitespace? pv)
                  (z/->string pv) "")
          cstr  (z/->string fzip)
          clen  (.length cstr)]
      [[] (update-in current [:code]
                     str pvstr (.substring cstr 0 (- clen 1))) tags])
    [[] current tags]))

(declare parse-content-loop)

(defn parse-fact-form [fzip current tags fns]
  (let [[elems current tags]
        (parse-content-loop
         (-> fzip z/down z/next)
         (update-in current [:fact-level] inc) [] tags fns)]
    [elems (update-in current [:fact-level] dec) tags]))

(defn parse-content-single
  [fzip current tags fns]
  (cond (:fact-arrow current)
        (cond (or (whitespace? fzip)
                  (newline? fzip))
              [[] current tags]

              :else
              (parse-code fzip current tags))

        :else
        (cond (ns-form? fzip)
              (parse-ns-form fzip current tags)

              (comment-form? fzip)
              (parse-comment-form fzip current tags)

              (file-element? fzip)
              (parse-file-element fzip current tags)

              (ns-element? fzip)
              (parse-ns-element fzip current tags)

              (chapter-element? fzip)
              (parse-chapter-element fzip current tags)

              (section-element? fzip)
              (parse-section-element fzip current tags)

              (subsection-element? fzip)
              (parse-subsection-element fzip current tags)

              (subsubsection-element? fzip)
              (parse-subsubsection-element fzip current tags)

              (paragraph-element? fzip)
              (parse-paragraph-element fzip current tags)

              (image-element? fzip)
              (parse-image-element fzip current tags)

              (paragraph? fzip)
              (parse-paragraph fzip current tags)

              (multiline? fzip)
              (parse-multiline fzip current tags)

              (attribute? fzip)
              (parse-attribute fzip current tags)

              (fact-form? fzip)
              (parse-fact-form fzip current tags fns)

              (fact-arrow? fzip)
              (parse-arrow fzip current tags)

              (or (whitespace? fzip)
                  (newline? fzip))
              [[] current tags]

              (comment? fzip)
              (parse-comment fzip current tags)

              :else
              (update-code fzip current tags))))

(defn parse-content-loop
  [fzip current elements tags fns]
  (cond (nil? fzip)
        (let [[elems current tags] (create-code current tags)]
          [(concat elements elems) current tags])
        :else
        (let [[elems current tags]
              ((:parse fns) fzip current tags fns)]
          (recur (z/right* fzip)
                 current
                 (concat elements elems)
                 tags
                 fns))))

(defn parse-file-element [fzip current tags]
  (let [[elems current tags] (create-code current tags)
        [attrs current]  (merge-element-attribute fzip current)]
    (try
      (parse-content-loop (z/of-file (:src attrs))
                          current elems tags {:parse parse-content-single})
         (catch java.io.FileNotFoundException e
           (println "Cannot Find file: " (:src attrs))
           [[] current tags]))))

(defn apply-paragraph-stencil [elem tags]
  (cond (= :paragraph (:type elem))
        (-> elem
            (update-in [:content]
                       stc/render-string tags)
            (update-in [:content]
                       escape-html))

        :else elem))

(defn parse-content
  ([fzip] (parse-content fzip new-current [] {}))
  ([fzip current elements tags]
     (let [[elements current tags]
           (parse-content-loop fzip current elements tags  {:parse parse-content-single})]
       (map #(apply-paragraph-stencil % tags) elements))))
