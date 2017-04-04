(ns goocle.core
  (:require [goocle
             [reflections :as r]
             [builder :as b]
             [utils :refer [fixup-dash-number]]]
            [clojure
                [set :refer [map-invert]]
             [string :refer [ends-with?]]]
            [camel-snake-kebab.core :refer [->kebab-case]
             ])
  (:import [java.lang.reflect Modifier]))

(defn intern-fn
  [{:keys [ns name ftn] :as fn-def}]
  (let [ns-sym (symbol ns)
        name-sym (symbol name)]
    (println ns-sym name-sym)
    (create-ns ns-sym)
    (intern ns-sym name-sym (eval (read-string ftn)))))

(defn- ftn-name
  [clazz]
  (-> (.getSimpleName clazz)
      ->kebab-case
      fixup-dash-number)
  (->kebab-case (.getSimpleName clazz)))

(defn- local-ns
  [clazz]
  (-> (.getPackage clazz)
      .getName
      (clojure.string/replace #"com.google.cloud." "")
      (clojure.string/replace #"\." "-")
      ->kebab-case
      fixup-dash-number
      ((partial str "goocle."))))

(defn create-all-builder-fns
  []
  (let [r (r/get-reflector-for-namespace "com.google.cloud")
        builder-classes (r/get-classes-with-method r #"new.*Builder")
        builder-fns (reduce
                     (fn [s bc]
                       (let [ftns (b/build-fn (.getName bc) #"new.*Builder")
                             ]
                         (concat s
                                 (map (fn [f] {:ns (local-ns bc)
                                              :ftn (:fn f)
                                              :name (-> (if (= (:name f) "newBuilder")
                                                          (str "create-" (ftn-name bc) "-builder")
                                                          (-> (:name f)
                                                              (clojure.string/replace #"^new" "")
                                                              (->kebab-case))))})
                                      ftns))))
                     '()
                     builder-classes)]
    builder-fns))

(defn intern-all-builders
  []
  (map intern-fn (create-all-builder-fns)))
