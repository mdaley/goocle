(ns goocle.builder
  (:require [goocle.reflections :as r]
            [camel-snake-kebab.core :refer [->kebab-case]]))

(defn- kebabed
  "Create a space separated set of kebab case values."
  [vals]
  (->> vals
       (map #(->kebab-case %))
       (interpose " ")
       (reduce str)))

(defn- build-cond-part
  [class-name method-name args]
  (if (seq args)
    (let [kebabed-args (kebabed (map :name args))
          type-tests (->> args
                          (map #(str "(instance? " (:type %) " " (->kebab-case (:name %)) ")"))
                          (interpose " ")
                          (reduce str))]
      (format "(and %s %s) (%s/%s %s)" kebabed-args type-tests class-name method-name kebabed-args))
    (format "true (%s/%s)", class-name method-name)))

(defn build-zero-args-fn
  [class-name method-name]
  (format "(fn [] (%s/%s))" class-name method-name))

(defn build-single-arg-fn
  [class-name method-name {n :name t :type}]
  (format "(fn [%s] (if (instance? %s %s) (%s/%s %s) (throw IllegalArgumentException.)))"
          n t n class-name method-name n))

(defn build-multi-arg-fn
  [class-name method-name args]
  (let [arg-names (reduce str (interpose " " (distinct (map :name (flatten args)))))
        no-zero-args-overload? (seq (last args))
        cond-clause  (str "(cond "
                          (reduce str (interpose " " (map (partial build-cond-part class-name method-name) args)))
                          (when no-zero-args-overload? " :else (throw IllegalArgumentException.)")
                          ")")]
        (format "(fn [{:keys [%s]}] %s)" arg-names cond-clause)))

(defn build-fn
  "Build definition of function to call method on class based on relevant incoming parameters."
  [class-name method-name]
  (let [ordered-methods-args (r/get-ordered-methods-args class-name method-name)
        method-count (count ordered-methods-args)]
    (cond
      (zero? method-count) (build-zero-args-fn class-name method-name)
      ;(= 1 method-count) (build-single-arg-fn class-name method-name (first ordered-methods-args))
      :else (build-multi-arg-fn class-name method-name ordered-methods-args))))