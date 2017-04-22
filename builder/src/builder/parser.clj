(ns builder.parser
  (:require [builder.reflector :as r]
            [clojure.string :refer [starts-with? includes? split join replace-first]]
            [camel-snake-kebab.core :refer [->kebab-case]]
            [medley.core :refer [map-kv]])
  (:import [java.lang.reflect Modifier]))

(defn get-methods
  [clazz method-regex]
  (try
    (->> clazz
         .getMethods
         (filter #(re-matches method-regex (.getName %)))
         (filter #(= (.getDeclaringClass %) clazz))
         seq)
    (catch ClassNotFoundException e)))

(defn get-static-methods
  [clazz method-regex]
  (->> (get-methods clazz method-regex)
       (filter #(Modifier/isStatic (.getModifiers %)))))

(defn has-method
  [clazz method-regex]
  (not (nil? (get-methods clazz method-regex))))

(defn extract-namespace
  [s]
  (->> (split s #"\.")
      drop-last
      (join ".")))

(defn get-namespaces
  []
  (->> (.getAllTypes r/reflector)
       seq
       (filter #(starts-with? % "com.google.cloud"))
       (filter #(not (includes? % "examples")))
       (group-by extract-namespace)
       seq
       (map (fn [v] {:jns (first v) :types (second v)}))))

(defn generate-default-instance-methods
 [types]
 (reduce
  (fn [s t]
    (let [clazz (Class/forName t false r/cgc-class-loader)]
        (if-let [methods (->> (get-static-methods clazz #"getDefault.*")
                              (filter #(= 0 (count (.getParameterTypes %)))))]
          (str s (join "" (map (fn [m]
                               (format "(defn %s-%s [] (%s/%s))\r\n"
                                       (-> (.getSimpleName clazz)
                                           (->kebab-case))
                                       (-> (.getName m)
                                           (replace-first #"^get" "")
                                           (->kebab-case))
                                       (.getName clazz)
                                       (.getName m)))
                             methods)))
          s)))
  ""
  types))

(defn build-namespace
  [{jns :jns types :types :as namespace}]
  (let [short-ns (-> (clojure.string/replace jns #"^com.google.cloud\.?" "")
                     (#(if (empty? %) "base" %)))]
    (assoc namespace
           :file (str (clojure.string/replace short-ns #"\." "_") ".clj")
           :contents (str "(ns goocle." (clojure.string/replace short-ns #"\." "-") ")\r\n"
                          (generate-default-instance-methods types)))))

(defn build-namespaces
  []
  (->> (get-namespaces)
       (map build-namespace)))
