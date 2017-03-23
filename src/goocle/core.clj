(ns goocle.core
  (:require [goocle
             [reflections :as r]
             [builder :as b]]
            [clojure
                [set :refer [map-invert]]
             [string :refer [ends-with? replace]]]
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

(defn- fixup-dash-number
  "Fixup any instances of -[0-9] to be [0-9] so that numbers aren't separated from their preceding text (or numbers)."
  [s]
  (replace s #"\-([0-9])" "$1"))

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

(defn intern-all-builders
  []
  (let [r (r/get-reflector-for-namespace "com.google.cloud")
        builder-classes (r/get-classes-with-method r "newBuilder")
        fns (map (fn [bc] {:ns (local-ns bc) :name (str (ftn-name bc) "-builder") :ftn (b/build-fn (.getName bc) "newBuilder")}) builder-classes)]
    (map intern-fn fns)))
