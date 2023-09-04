(ns ruler.models-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [ruler.models :as models]))

(def test-rule
  {:key :test
   :type String
   :req true
   :req-depends [:bla]
   :req-fn (fn [_] true)
   :min-length 0
   :max-length 10
   :length 6
   :contains ["string"]
   :format #"[a-z]{6}"
   :format-fn (fn [_] true)})

(def test-rule2
  {:key :test
   :type Integer
   :req true
   :req-depends [:bla]
   :req-fn (fn [_] true)
   :min -9999
   :max 9999})

(def test-data
  {:test "string"})

(def test-data2
  {:test 12})

(deftest data-key-validation-test
  (testing "Valid data value"
    (is (nil? (models/data-key-validation :type test-rule test-data )) ":type")
    (is (nil? (models/data-key-validation :req test-rule test-data )) ":req")
    (is (nil? (models/data-key-validation :req-depends test-rule test-data )) ":req-depends")
    (is (nil? (models/data-key-validation :req-fn test-rule test-data )) ":req-fn")
    (is (nil? (models/data-key-validation :min-length test-rule test-data )) ":min-length")
    (is (nil? (models/data-key-validation :max-length test-rule test-data )) ":max-length")
    (is (nil? (models/data-key-validation :min test-rule2 test-data2)) ":min")
    (is (nil? (models/data-key-validation :max test-rule2 test-data2)) ":max")
    (is (nil? (models/data-key-validation :length test-rule test-data )) ":length")
    (is (nil? (models/data-key-validation :contains test-rule test-data )) ":contains")
    (is (nil? (models/data-key-validation :format test-rule test-data )) ":format")
    (is (nil? (models/data-key-validation :format-fn test-rule test-data )) ":format-fn"))
  (testing "Invalid data value"))
