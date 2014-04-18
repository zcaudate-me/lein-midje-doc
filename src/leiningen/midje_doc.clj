(ns leiningen.midje-doc
  (:require [leiningen.midje-doc.common :refer [printv]]
            [leiningen.midje-doc.import :as import]
            [leiningen.midje-doc.purge :as purge]
            [leiningen.midje-doc.run :as run]
            [leiningen.midje-doc.markdown :as markdown]
            [leiningen.midje-doc.scaffold :as scaffold]
            [clojure.string :as string]
            [hara.common.error :refer [suppress]]))

(defn midje-doc-help [project]
  (printv ["Subcommands for `lein midje-doc`"
           ""
           "<default>           generates project documentation"
           "help                shows this message"
           "import              import source docstrings from tests"
           "markdown            test markdown files   -  options `once`, `plain`"
           "purge               removes imported source docstrings"
           "run                 generates documentation"
           "scaffold            generate test files from source"]))

(defn midje-doc-import [project & args]
  (import/import project)
  (println "The source documentation has been imported."))

(defn midje-doc-markdown [project]
  (let [files (-> project :documentation :markdown)
        results (->> (mapv (juxt #(-> % (markdown/test-markdown)
                                      (suppress (str "Cannot Load File"))) identity) files)
                     (group-by (comp not true? first)))]
    (printv [""
             " ------------------------------------------------------------"
             "                      MARKDOWN SUMMARY"
             " ------------------------------------------------------------"
             ""
             (if-let [failures (-> results (get true))]
               (->> failures
                    (map (fn [[reason filename]]
                           (format "   FAILED: `%s`\t REASON: %s" filename (if (false? reason)
                                                                             "Tests Failed"
                                                                             reason))))
                    (string/join "\n"))
               "   SUCCESS")
             ""
             " ------------------------------------------------------------"])))

(defn midje-doc-run [project & args]
  (apply run/run project args))

(defn midje-doc-purge [project & args]
  (purge/purge project)
  (println "The docstrings have been purged"))

(defn midje-doc-scaffold [project & args]
  (scaffold/scaffold project)
  (println "The Test Scaffolding has been generated"))

(defn midje-doc [project & [sub & args]]
 (condp = sub
   nil         (apply midje-doc-run project args)
   "help"      (midje-doc-help project)
   "import"    (midje-doc-import project)
   "markdown"  (midje-doc-markdown project)
   "run"       (apply midje-doc-run project args)
   "purge"     (midje-doc-purge project)
   "scaffold"  (midje-doc-scaffold project)))
