(ns goocle.core-test
  (:require [clojure.test :refer :all]
            [goocle.core :refer :all]))

(deftest create-builder-is-successful
  (testing "creates builder where there are no parameters"
    (is (instance? com.google.cloud.datastore.DatastoreOptions$Builder
                   (create-builder com.google.cloud.datastore.DatastoreOptions))))
  (testing "creates builder where there is one parameter"
    (is (instance? com.google.cloud.datastore.StringValue$Builder
                   (create-builder com.google.cloud.datastore.StringValue "s"))))
  (testing "creates builder where there is one primitive parameter"
    (is (instance? com.google.cloud.datastore.LongValue$Builder
                   (create-builder com.google.cloud.datastore.LongValue 2))))
  (testing "creates builder where there are several arguments"
    (is (instance? com.google.cloud.datastore.Key$Builder
                   (create-builder com.google.cloud.datastore.Key "s" "s" "s"))))
  (testing "creates builder where there are several arguments of different types"
    (is (instance? com.google.cloud.datastore.Key$Builder
                   (create-builder com.google.cloud.datastore.Key "s" "s" 2)))))
