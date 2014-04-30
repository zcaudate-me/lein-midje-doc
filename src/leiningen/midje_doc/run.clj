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
    (binding [leiningen.midje-doc.run.renderer/*plain*
              (-> project :documentation :plain)]
      (doseq [k ks]
        (let [doc (get dmap k)]
          (run-doc k (merge attrs doc) project))))))

(require
 '[compojure.core :as compojure]
 '[compojure.route :as route]
 '[org.httpkit.server :as httpkit])

(defn run-server []
  (println "Running server 12")
  (let [user-dir (System/getProperty "user.dir")
        app (compojure/routes
             (route/resources "/assets")
             (compojure/HEAD "/*" {path :uri}
                             (let [f (clojure.java.io/file (str user-dir path))]
                               (println "path" path)
                               (when (.exists f)
                                 (println "watttt" path (.lastModified f))
                                 {:body ""
                                  :headers {"ETag" (str (.lastModified f))
                                            "Content-Length" (.length f)}})))
             (compojure/GET "/*" {path :uri}
                            (let [f (clojure.java.io/file (str user-dir path))]
                              (when (.exists f )
                                (let [[pre-footer footer] (.split ^String (slurp f) "</body>" 2)]
                                  (println "Got here then")
                                  {:headers {"ETag" (str (.lastModified f))
                                             "Content-Length" (.length f)}
                                   :body
                                   (str pre-footer
                                        "<script type=\"text/javascript\" src=\"/assets/live.js#html\" />"
                                        "</body>"
                                        footer)}))))

             (route/not-found "<h1>Page not found</h1>"))]
    (httpkit/run-server app {:port 8282})))

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
        (run-server)
        (run-watch project)
        (Thread/sleep 100000000000000)))))
