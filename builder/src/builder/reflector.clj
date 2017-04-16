(ns builder.reflector
  (:import [java.lang ClassLoader]
           [java.net URLClassLoader]
           [org.reflections Reflections]
           [org.reflections.util ClasspathHelper ConfigurationBuilder FilterBuilder]
           [org.reflections.scanners Scanner SubTypesScanner TypeAnnotationsScanner]))

(def cgc-target-dir (java.io.File. (str (System/getProperty"user.dir") "/../cgc/target")))

(def  cgc-jar-urls
  (->> (.listFiles cgc-target-dir)
       seq
       (filter #(re-matches #".*WithParameters.jar$" (.getName %)))
       (map #(.toURL (.toURI %)))))

(def cgc-class-loader (URLClassLoader. (into-array java.net.URL cgc-jar-urls)
                                       (ClassLoader/getSystemClassLoader)))

(def cb (doto (ConfigurationBuilder.)
          (.setUrls (ClasspathHelper/forPackage "com.google.cloud"
                                                (into-array ClassLoader [cgc-class-loader])
                                                ))
          (.setScanners (into-array Scanner [(SubTypesScanner. false)
                                             (TypeAnnotationsScanner.)]))
          (.filterInputsBy (doto (FilterBuilder.)
                             (.includePackage (into-array String ["com.google.cloud"]))))
          (.addClassLoader cgc-class-loader)))

(def reflector (Reflections. cb))
