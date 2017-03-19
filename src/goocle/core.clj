(ns goocle.core
  (:require [clojure
             [set :refer [map-invert]]
             [string :refer [ends-with?]]])
  (:import [java.lang.reflect Modifier]))

;; (defn is-gc-class?
;;   [clazz]
;;   (->> (.getName clazz)
;;        (re-find #"com\.google\.cloud")
;;        (some?)))

;; (defn matching-methods
;;   [methods name]
;;   (reduce (fn [l v]
;;             (if (= (.getName v) name)
;;               (conj l v)
;;               l))
;;           []
;;           methods))

;; (defn find-methods
;;   [pojo name]
;;   (-> (.getClass pojo)
;;       (.getMethods)
;;       (matching-methods name)))

(defn builder-fns
  "Obtain the public static builder functions of the class or an empty sequence if it doesn't have any."
  [clazz]
  (->> (.getMethods clazz)
       (filter #(and (= (.getName %) "newBuilder")
                     (Modifier/isStatic (.getModifiers %))
                     (Modifier/isPublic (.getModifiers %))))))

(def primitive-mapping
  {Boolean/TYPE Boolean
   Byte/TYPE Byte
   Character/TYPE Character
   Double/TYPE Double
   Float/TYPE Float
   Integer/TYPE Integer
   Long/TYPE Long
   Short/TYPE Short})

(defn replace-primitive-type
  [p]
  (or (get primitive-mapping p) p))

(defn matching-args?
  [java-args cl-args]
  (= (seq (map replace-primitive-type java-args)) cl-args))

(defn matching-builder-fn
  "Obtain the public static builder function that has a signature that matches the given argument classes."
  [clazz arg-classes]
  (->> (builder-fns clazz)
       (filter #(matching-args? (seq (.getParameterTypes %)) arg-classes))
       first))

(defn create-builder
  [clazz & args]
  (let [arg-classes (seq (map #(.getClass %) args))]
    (when-let [new-builder-fn (matching-builder-fn clazz arg-classes)]
      (.invoke new-builder-fn nil (when args (into-array Object args))))))
