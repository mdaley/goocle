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

(defn perform
  [version]
  (println (str "Building with Google Cloud Java version " version "."))
  (println (load-cgc-dependencies version))
  (pprint (build-namespaces))
  )

(defn -main [& args]
  (println "Goocle Builder")
  (if-let [version (first args)]
    (perform version)
    (println "Must specify version of Google Cloud Java to build as first argument.")))
