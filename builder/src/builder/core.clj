(ns builder.core
  (:require [clojure.pprint :refer [pprint]]
            [builder.parser :refer [build-namespaces]]
            [cemerick.pomegranate :refer [add-dependencies]]))

(defn load-cgc-dependencies
  [version]
  (println "Loading dependencies.")
  (pprint (add-dependencies :coordinates [[(symbol "com.google.cloud/google-cloud") (symbol version)]]))
  (println "Dependencies loaded.")
  (println (Class/forName "com.google.cloud.datastore.DatastoreOptions")))

(defn delete-recursively
  [filename]
  (let [func (fn [func f]
               (when (.isDirectory f)
                 (doseq [f2 (.listFiles f)]
                   (func func f2)))
               (clojure.java.io/delete-file f))]
    (func func (clojure.java.io/file filename))))

(defn recreate-dir
  [path]
  (let [f (java.io.File. path)]
    (when (.exists f)
      (delete-recursively path))
    (.mkdir f)))

(defn write-output-file
  [gen-path {file :file contents :contents :as namespace}]
  (let [path (str gen-path "/src/goocle/" file)]
    (println "Generating contents of" path)
    (spit path contents)))

(def project-file-template
  "(defproject goocle \"%s-cgc%s\"
  :description \"Goocle - a clojure wrapper for the Google Cloud Java library\"
  :url \"https://github.com/mdaley/goocle\"
  :license {:name \"Eclipse Public License\"
            :url \"http://www.eclipse.org/legal/epl-v10.html\"}
  :dependencies [[org.clojure/clojure \"1.8.0\"]
                 [com.google.cloud/google-cloud \"%s\"]])")

(def project-version
  (some-> (slurp "project.clj")
          clojure.edn/read-string
          (nth 2)))

(defn write-project-file
  [gen-path version]
  (spit (str gen-path "/project.clj") (format project-file-template
                                              project-version
                                              version
                                              version)))

(defn write-output-files
  [version namespaces]
  (let [gen-path (str (System/getProperty "user.dir") "/../generated")]
    (recreate-dir gen-path)
    (write-project-file gen-path version)
    (.mkdir (java.io.File. (str gen-path "/src")))
    (.mkdir (java.io.File. (str gen-path "/src/goocle")))
    (doall (map #(write-output-file gen-path %) namespaces))))

(defn perform
  [version]
  (println (str "Building with Google Cloud Java version " version "."))
  (println (load-cgc-dependencies (str version "-WithParameters")))
  (write-output-files version (build-namespaces)))

(defn -main [& args]
  (println "Goocle Builder")
  (if-let [version (first args)]
    (perform version)
    (println "Must specify version of Google Cloud Java to build as first argument.")))
