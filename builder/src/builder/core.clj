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
  (let [path (str gen-path "/" file)]
    (println "Generating contents of" path)
    (spit path contents)))

(defn write-output-files
  [namespaces]
  (let [gen-path (str (System/getProperty "user.dir") "/../generated")]
    (recreate-dir gen-path)
    (doall (map #(write-output-file gen-path %) namespaces))))

(defn perform
  [version]
  (println (str "Building with Google Cloud Java version " version "."))
  (println (load-cgc-dependencies version))
  (write-output-files (build-namespaces)))

(defn -main [& args]
  (println "Goocle Builder")
  (if-let [version (first args)]
    (perform version)
    (println "Must specify version of Google Cloud Java to build as first argument.")))
