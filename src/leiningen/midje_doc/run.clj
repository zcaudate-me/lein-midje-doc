(ns leiningen.midje-doc.run
  (:require [leiningen.midje-doc.run.renderer :refer [render-html-doc]]
            [leiningen.midje-doc.run.parser :refer [parse-content]]
            [rewrite-clj.zip :as z]
            [watchtower.core :refer [watcher file-filter
                                     ignore-dotfiles extensions
                                     rate on-add on-modify on-delete]]
            [me.raynes.conch :refer [programs]] :reload))

(programs which)

(defn run-doc [k doc project]
  (let [infile (:input doc)
        outfile (str k ".html")]
    (println "Generating" outfile)
    (try
      (render-html-doc outfile
                       doc
                       (parse-content (z/of-file infile) project))
      (println "...... DONE!")
      (catch Throwable t
        (println t)
        (println "Error Generating" outfile)))))

(defn run-once [project]
  (let [dmap (-> project :documentation :files)
        attrs (select-keys project [:version :url])
        ks (keys dmap)]
    (binding [leiningen.midje-doc.renderer/*plain*
              (-> project :documentation :plain)]
      (doseq [k ks]
        (let [doc (get dmap k)]
          (run-doc k (merge attrs doc) project))))))

(defn run-watch [project]
  (let [p-once (fn [_] (run-once project))]
    (p-once nil)
    (watcher [(:root project)]
             (rate 200) ;; poll every 200ms
             (file-filter ignore-dotfiles) ;; add a filter for the files we care about
             (file-filter (extensions :clj :cljs)) ;; filter by extensions
             (on-modify  p-once); Optional
             (on-delete  p-once); Optional
             (on-add     p-once))))

(defn check-pygmentize []
  (try (not (= "" (which "pygmentize")))
       (catch Throwable t)))

(defn run [project & args]
  (let [opts (set args)
        project (if (or (opts "plain") (not (check-pygmentize)))
                  (assoc-in project [:documentation :plain] true)
                  project)]
    (if (opts "once")
      (run-once project)
      (do
        (run-watch project)
        (Thread/sleep 100000000000000)))))
