(ns leiningen.midje-doc
  (:require [leiningen.midje-doc.renderer :refer [render-html-doc]]
            [leiningen.midje-doc.parser :refer [parse-content]]
            [rewrite-clj.zip :as z]
            [watchtower.core :refer [watcher file-filter
                                     ignore-dotfiles extensions
                                     rate on-add on-modify on-delete]]
            [me.raynes.conch :refer [programs]]))

(programs which)

(defn process-doc [k doc]
  (let [infile (:input doc)
        outfile (str k ".html")]
    (println "Generating" outfile)
    (try
      (render-html-doc outfile
                       doc
                       (parse-content (z/of-file infile)))
      (println "...... DONE!")
      (catch Throwable t
        (println t)
        (println "Error Generating" outfile)))))

(defn process-once [project]
  (let [dmap (-> project :documentation :files)
        attrs (select-keys project [:version :url])
        ks (keys dmap)]
    (binding [leiningen.midje-doc.renderer/*plain*
              (-> project :documentation :plain)]
      (doseq [k ks]
        (let [doc (get dmap k)]
          (process-doc k (merge attrs doc)))))))

(defn process-watch [project]
  (let [p-once (fn [_] (process-once project))]
    (p-once nil)
    (watcher [(:root project)]
             (rate 50) ;; poll every 50ms
             (file-filter ignore-dotfiles) ;; add a filter for the files we care about
             (file-filter (extensions :clj :cljs)) ;; filter by extensions
             (on-modify  p-once); Optional
             (on-delete  p-once); Optional
             (on-add     p-once))))

(defn check-pygmentize []
  (try (not (= "" (which "pygmentize")))
       (catch Throwable t)))

(defn midje-doc
  "I don't do a lot."
  [project & args]
  (let [opts (set args)
        project (if (or (opts "plain") (not (check-pygmentize)))
                  (assoc-in project [:documentation :plain] true)
                  project)]
    (if (opts "once")
      (process-once project)
      (do
        (process-watch project)
        (Thread/sleep 100000000000000)))))
