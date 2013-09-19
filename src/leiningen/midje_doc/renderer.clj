(ns leiningen.midje-doc.renderer
  (:require [hiccup.core :as html]
            [markdown.core :refer [md-to-html-string]]
            [me.raynes.conch :refer [programs]]))

(programs pygmentize)

(def ^:dynamic *pygmentize* true)

(defn render-element [elem]
  (condp = (:type elem)

    :chapter
    [:div
     (if (:tag elem) [:a {:name (:tag elem)}])
     [:h2 [:b (str (:num elem) " &nbsp;&nbsp; " (:title elem))]]]
    :section
    [:div
     (if (:tag elem) [:a {:name (:tag elem)}])
     [:h3 (str (:num elem) " &nbsp;&nbsp; " (:title elem))]]
    :subsection
    [:div
     (if (:tag elem) [:a {:name (:tag elem)}])
     [:h3 [:i (str (:num elem) " &nbsp;&nbsp; " (:title elem))]]]
    :subsubsection
    [:div
     (if (:tag elem) [:a {:name (:tag elem)}])
     [:h3 [:i (str (:num elem) " &nbsp;&nbsp; " (:title elem))]]]
    :paragraph [:div (md-to-html-string (:content elem))]
    :image
    [:div {:class "figure"}
     (if (:tag elem) [:a {:name (:tag elem)}])
     (if (:num elem)
       [:h4 [:i (str "fig." (:num elem)
                     (if-let [t (:title elem)] (str "  &nbsp;-&nbsp; " t)))]])
     [:div {:class "img"} [:img (dissoc elem :num :type :tag)]]
     [:p]]
    :ns
    [:div
     (if *pygmentize*
       (pygmentize  "-f" "html" "-l" "clojure" {:in (:content elem)})
       [:pre (:content elem)])]
    :code
    [:div
     (if (:tag elem) [:a {:name (:tag elem)}])
     (if (:num elem)
       [:h4 [:i (str "e." (:num elem)
                     (if-let [t (:title elem)] (str "  &nbsp;-&nbsp; " t)))]])
     (if *pygmentize*
       (pygmentize  "-f" "html" "-l" "clojure" {:in (.replaceFirst (:content elem) "\n" (apply str (repeat (or (:fact-level elem) 0) "  ")))})
       [:pre (.replaceFirst (:content elem) "\n" (apply str (repeat (or (:fact-level elem) 0) "  ")))])]))

(defn render-toc-element [elem]
  (case (:type elem)
    :chapter [:h4
              [:a {:href (str "#" (:tag elem))} (str (:num elem) " &nbsp; " (:title elem))]]

    :section [:h5 "&nbsp;&nbsp;"
              [:i [:a {:href (str "#" (:tag elem))} (str (:num elem) " &nbsp; " (:title elem))]]]))

(defn render-toc [elems]
  (let [telems (filter #(#{:chapter :section} (:type %)) elems)]
    (map render-toc-element telems)))


(defn render-elements [elems]
  (html/html
   (map render-element elems)))

(defn slurp-res [path]
  (slurp (or (clojure.java.io/resource path)
             (str "resource/" path))))

(defn render-html-doc [output document elems]
  (spit output
        (html/html
         [:html
          [:head
           [:meta {:http-equiv "X-UA-Compatible" :content "chrome=1"}]
           [:meta {:charset "utf-8"}]
           [:meta {:name "viewport" :content "width=device-width, initial-scale=1, user-scalable=no"}]
           [:title (or (:window document) (:title document))]
           [:style
            (str
             (slurp-res "template/stylesheets/styles.css")
             "\n\n"
             (slurp-res "template/stylesheets/pygment_trac.css")
             "\n\n")]]
          [:body
           [:header
            [:h1 (:title document)]
            [:h4 (:sub-title document)]
            [:hr]
            (render-toc elems)
            [:br]]
           [:section
            (map render-element elems)]]
          [:script {:type "text/javascript"}
           (slurp-res "template/javascripts/scale.fix.js")]])))
