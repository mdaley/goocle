(ns builder.core
  (:import [java.lang.reflect Modifier]
           [org.reflections Reflections]
           [org.reflections.util ClasspathHelper ConfigurationBuilder FilterBuilder]
           [org.reflections.scanners Scanner SubTypesScanner TypeAnnotationsScanner]))

(defn -main [& args]
  (println "Goocle Builder")
  (if-let [version (first args)]
    (println (str "Building with Google Cloud Java version " version "."))
    (println "Must specify version of Google Cloud Java to build as first argument.")))

(def f (java.io.File. (str "/Users/matt.daley/workspace/goocle/cgc/target")))

(def fs (->> (.listFiles f)
             seq
             (filter #(re-matches #".*WithParameters.jar$" (.getName %)))))

(def urls (map #(.toURL (.toURI %)) fs))

(def cl (java.net.URLClassLoader. (into-array java.net.URL urls) (java.lang.ClassLoader/getSystemClassLoader)))

(def sc (into-array Scanner [(SubTypesScanner. false) (TypeAnnotationsScanner.)]))

(def cb (doto (ConfigurationBuilder.)
          (.setUrls (ClasspathHelper/forPackage "com.google.cloud" (into-array java.lang.ClassLoader [cl])))
          (.setScanners (into-array Scanner [(SubTypesScanner. false) (TypeAnnotationsScanner.)]))

          (.filterInputsBy (doto (FilterBuilder.) (.includePackage (into-array String ["com.google.cloud"]))))
          (.addClassLoader cl)))

(def r (Reflections. cb))
