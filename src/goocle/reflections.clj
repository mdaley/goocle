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
  [clazz method-name]
  (try
    (->> clazz
         .getMethods
         (filter #(= (.getName %) method-name))
         seq)
    (catch ClassNotFoundException e)))

(defn has-method
  [type-name method-name]
  (not (nil? (get-methods (Class/forName type-name) method-name))))

(defn get-classes-with-method
  [reflector name]
  (->> (.getAllTypes reflector)
       (filter #(has-method % name))
       (map #(Class/forName %))
       (filter #(Modifier/isPublic (.getModifiers %)))))

(defn get-types-with-new-builder-methods
  [reflector]
  (get-classes-with-method reflector "newBuilder"))

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
  (map (fn [v] {:name (.getName v)
               :type (coerce-if-primitive (.getTypeName (.getType v)))}) (seq (.getParameters method))))

(defn get-ordered-methods-args
  [class-name method-name]
  (->> (get-methods (Class/forName class-name) method-name)
       (map get-method-args)
       (sort-by #(count (keys %)))
       reverse))

;; (defn generate-fn
;;   "Generate functions for method with the given name in the chosen class."
;;   [clazz method-name]
;;   (when-let [methods (get-methods clazz method-name)]
;;     (map )))

;; (defn make-fn-name
;;   [prefix class]
;;   (-> (.getName class)
;;       (split #"\.")
;;       last
;;       ->kebab-case
;;       ((partial str prefix))))

(defn intern-fn
  [ns {:keys [name fn] :as fn-def}]
  (let [ns-sym (symbol ns)
        name-sym (symbol name)]
    (create-ns ns-sym)
    (intern ns-sym name-sym (eval (read-string fn)))))

;; (defn make-creators
;;   [namespace]
;;   (let [classes (get-classes-with-method (get-reflector-for-namespace namespace) "newBuilder")]
;;     (map (partial make-fn-name "create-") classes)))
