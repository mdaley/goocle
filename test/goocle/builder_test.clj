(ns goocle.builder-test
  (:require [goocle
             [builder :refer :all]]
            [clojure.test :refer :all]))

(defn- test-function-build
  [{:keys [methods-args expected]}]
  (with-redefs [goocle.reflections/get-ordered-methods-args (fn [_ _] methods-args)]
    (is (= expected (build-fn "the.Class" "theFunction")))))

(deftest build-function
  (testing "Zero argument non-overloaded static function definition built correctly"
    (test-function-build {:methods-args '()
                          :expected "(fn [] (the.Class/theFunction))"}))

  (testing "Non-overloaded static function with single argument definition built correctly"
    (test-function-build {:methods-args '(({:name "name"
                                            :type "the.Type"}))
                          :expected "(fn [{:keys [name]}] (cond (and name (instance? the.Type name)) (the.Class/theFunction name) :else (throw IllegalArgumentException.)))"}))

  (testing "Non-overloaded multi-argument static function definition built correctly"
    (test-function-build {:methods-args '(({:name "name"
                                            :type "the.Type"}
                                           {:name "other-name"
                                            :type "the.OtherType"}))
                          :expected "(fn [{:keys [name other-name]}] (cond (and name other-name (instance? the.Type name) (instance? the.OtherType other-name)) (the.Class/theFunction name other-name) :else (throw IllegalArgumentException.)))"}))

  (testing "Overloaded static function with range of arguments definition built correctly"
    (test-function-build {:methods-args '(({:name "name"
                                            :type "the.Type"}
                                           {:name "other"
                                            :type "the.Other"})
                                          ({:name "name"
                                            :type "the.Type"}))
                          :expected "(fn [{:keys [name other]}] (cond (and name other (instance? the.Type name) (instance? the.Other other)) (the.Class/theFunction name other) (and name (instance? the.Type name)) (the.Class/theFunction name) :else (throw IllegalArgumentException.)))"}))

  (testing "Overloaded range of arguments static function definition built correctly when there is a zero args overload"
    (test-function-build {:methods-args '(({:name "name"
                                            :type "the.Type"}
                                           {:name "other"
                                            :type "the.Other"})
                                          ({:name "name"
                                            :type "the.Type"})
                                          ())
                          :expected "(fn [{:keys [name other]}] (cond (and name other (instance? the.Type name) (instance? the.Other other)) (the.Class/theFunction name other) (and name (instance? the.Type name)) (the.Class/theFunction name) true (the.Class/theFunction)))"})))
