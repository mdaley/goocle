(ns goocle.reflections
  (:require [camel-snake-kebab.core :refer [->kebab-case]]
            [clojure.string :refer [split]])
  (:import [java.lang.reflect Modifier]
           [org.reflections Reflections]
           [org.reflections.util ClasspathHelper ConfigurationBuilder FilterBuilder]
           [org.reflections.scanners Scanner SubTypesScanner TypeAnnotationsScanner]))

(defn get-reflector-for-namespace
  [namespace]
  (Reflections.
   (doto (ConfigurationBuilder.)
     (.setUrls (ClasspathHelper/forPackage namespace nil))
     (.setScanners (into-array Scanner [(SubTypesScanner. false)
                                        (TypeAnnotationsScanner.)]))
     (.filterInputsBy (doto (FilterBuilder.)
                        (.includePackage (into-array String [namespace])))))))

(defn get-methods
  [clazz method-regex]
  (try
    (->> clazz
         .getMethods
         (filter #(re-matches method-regex (.getName %)))
         (filter #(= (.getDeclaringClass %) clazz))
         seq)
    (catch ClassNotFoundException e)))

(defn has-method
  [type-name method-regex]
  (println "TYPE-NAME" type-name)
  (println "CLASS" (Class/forName type-name))
  (try
    (not (nil? (get-methods (Class/forName type-name) method-regex)))
    (catch NoClassDefFoundError e
      false)))

(defn get-classes-with-method
  [reflector name-regex]
  (->> (.getAllTypes reflector)
       (filter #(has-method % name-regex))
       (map #(Class/forName %))
       (filter #(Modifier/isPublic (.getModifiers %)))))

(defn get-types-with-new-builder-methods
  [reflector]
  (get-classes-with-method reflector #"new.*Builder"))

(defn coerce-if-primitive
  "Coerce to primitive if necessary."
  [p]
  (case p
    "float" "java.lang.Float"
    "int" "java.lang.Integer"
    "long" "java.lang.Long"
    "boolean" "java.lang.Boolean"
    "char" "java.lang.Character"
    "double" "java.lang.Double"
    "byte" "java.lang.Byte"
    "short" "java.lang.Short"
    p))

(defn get-method-args
  [method]
  {:name (.getName method)
   :args
   (map (fn [v] {:name (.getName v)
                :type (coerce-if-primitive (.getTypeName (.getType v)))}) (seq (.getParameters method)))})

(defn get-ordered-methods-args
  [class-name method-regex]
  (->> (get-methods (Class/forName class-name) method-regex)
       (map get-method-args)
       (sort-by #(count (keys (:args %))))
       reverse))
