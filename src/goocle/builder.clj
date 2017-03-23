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

(defn build-multi-arg-fn
  [class-name method-name args]
  (let [arg-names (kebabed (distinct (map :name (flatten args)))) ;(reduce str (interpose " " (distinct (map :name (flatten args)))))
        no-zero-args-overload? (seq (last args))
        cond-clause  (str "(cond "
                          (reduce str (interpose " " (map (partial build-cond-part class-name method-name) args)))
                          (when no-zero-args-overload? (str " :else (throw (IllegalArgumentException. \"Please use valid combination of arguments from the set '" arg-names "'\"))"))
                          ")")]
        (format "(fn [{:keys [%s]}] %s)" arg-names cond-clause)))

(defn build-fn
  "Build definition of function to call method on class based on relevant incoming parameters."
  [class-name method-name]
  (let [ordered-methods-args (r/get-ordered-methods-args class-name method-name)]
    (if (and (= 1 (count ordered-methods-args)) (empty? (first ordered-methods-args)))
      (build-zero-args-fn class-name method-name)
      (build-multi-arg-fn class-name method-name ordered-methods-args))))
