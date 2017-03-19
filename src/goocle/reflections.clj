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

(defn get-method-args
  [method]
  (map (fn [v] {:name (.getName v)
               :type (.getTypeName (.getType v))}) (seq (.getParameters method))))

(defn method->fn-str
  [clazz mthd]
  (let [args (get-method-args mthd)]
    (str "(fn "
         "["
         (reduce str (interpose " " (map #(str "^" (:type %) " " (:name %)) args)))
         "] (."
         (.getName mthd)
         " "
         (.getName clazz)
         " "
         (reduce str (interpose " " (map :name args)))
         ")")))

(defn generate-fn
  "Generate functions for method with the given name in the chosen class."
  [clazz method-name]
  (when-let [methods (get-methods clazz method-name)]
    (map )))

(defn make-fn-name
  [prefix class]
  (-> (.getName class)
      (split #"\.")
      last
      ->kebab-case
      ((partial str prefix))))

(defn intern-fn
  [ns {:keys [name fn] :as fn-def}]
  (let [ns-sym (symbol ns)
        name-sym (symbol name)]
    (create-ns ns-sym)
    (intern ns-sym name-sym (eval (read-string fn)))))

(defn build-cond-clause
  [clazz method-name args]
  (str "(and "
       (reduce str (interpose " " (map #(->kebab-case (:name %)) args)))
       " "
       (reduce str (interpose " " (map #(str "(instance? " (:type %) " " (->kebab-case (:name %)) ")") args)))
       ") (. "
       method-name " "(.getName clazz) " "
       (reduce str (interpose " " (map #(->kebab-case (:name %)) args)))
       ")"))

(defn build-fn-dispatcher
  [clazz method-name]
  (let [methods (get-methods clazz method-name)
        methods-args (map get-method-args methods)
        ordered-methods-args (reverse (sort-by #(count (keys %)) methods-args))
        arg-names (distinct (map :name (flatten methods-args)))]

    (str "(cond"
         (map #() ordered-methods-args)
         ":else \"error\")")

    ;; (str "(fn [{:keys ["
    ;;      (->> (map #(->kebab-case %) arg-names)
    ;;            (interpose " ")
    ;;            (reduce str))
    ;;      "] :as __args}]"
    ;;      "(let [__size (count __args)]"
    ;;      "__args"
    ;;      ")"
    ;;      ")")
    ))

(defn make-creators
  [namespace]
  (let [classes (get-classes-with-method (get-reflector-for-namespace namespace) "newBuilder")]
    (map (partial make-fn-name "create-") classes)))
