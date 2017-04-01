(ns goocle.builder
  (:require [goocle
             [reflections :as r]
             [utils :refer [fixup-dash-number]]]
            [camel-snake-kebab.core :refer [->kebab-case]]))

(defn- kebabed
  "Create a space separated set of kebab case values."
  [vals]
  (->> vals
       (map #(->kebab-case %))
       (map fixup-dash-number)
       (interpose " ")
       (reduce str)))

(defn- build-cond-part
  [class-name method-name args]
  (if (seq args)
    (let [kebabed-args (kebabed (map :name args))
          type-tests (->> args
                          (map #(str "(instance? " (:type %) " " (->kebab-case (:name %)) ")"))
                          (map fixup-dash-number)
                          (interpose " ")
                          (reduce str))]
      (format "(and %s %s) (%s/%s %s)" kebabed-args type-tests class-name method-name kebabed-args))
    (format "true (%s/%s)", class-name method-name)))

(defn build-zero-args-fn
  [class-name method-name]
  (format "(fn [] (%s/%s))" class-name method-name))

(defn build-multi-arg-fn
  [class-name method-name args]
  (let [arg-names (kebabed (distinct (map :name (flatten args))))
        no-zero-args-overload? (seq (last args))
        cond-clause  (str "(cond "
                          (reduce str (interpose " " (map (partial build-cond-part class-name method-name) args)))
                          (when no-zero-args-overload? (str " :else (throw (IllegalArgumentException. \"Please use valid combination of arguments from the set '" arg-names "'\"))"))
                          ")")]
        (format "(fn [{:keys [%s]}] %s)" arg-names cond-clause)))

(defn build-fn
  "Build seq of functions to call methods on class based on relevant incoming parameters."
  [class-name method-regex]
  (let [ordered-methods-args (r/get-ordered-methods-args class-name method-regex)]
    (if (and (= 1 (count ordered-methods-args)) (empty? (:args (first ordered-methods-args))))
      (seq [{:name (:name (first ordered-methods-args))
              :fn (build-zero-args-fn class-name (:name (first ordered-methods-args)))}])
      (->> (group-by :name ordered-methods-args)
           (reduce-kv (fn [m k v] (assoc m k (map :args v))) {})
           seq
           (map (fn [v] {:name (first v)
                        :fn (build-multi-arg-fn class-name (first v) (second v))}))))))
