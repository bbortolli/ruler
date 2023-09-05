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
   :min-length 3
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

(deftest key-validation-test
  (testing "Valid data value"
    (is (nil? (models/key-validation :type test-rule test-data )) ":type")
    (is (nil? (models/key-validation :req test-rule test-data )) ":req")
    (is (nil? (models/key-validation :req-depends test-rule test-data )) ":req-depends")
    (is (nil? (models/key-validation :req-fn test-rule test-data )) ":req-fn")
    (is (nil? (models/key-validation :min-length test-rule test-data )) ":min-length")
    (is (nil? (models/key-validation :max-length test-rule test-data )) ":max-length")
    (is (nil? (models/key-validation :min test-rule2 test-data2)) ":min")
    (is (nil? (models/key-validation :max test-rule2 test-data2)) ":max")
    (is (nil? (models/key-validation :length test-rule test-data )) ":length")
    (is (nil? (models/key-validation :contains test-rule test-data )) ":contains")
    (is (nil? (models/key-validation :format test-rule test-data )) ":format")
    (is (nil? (models/key-validation :format-fn test-rule test-data )) ":format-fn"))
  (testing "Invalid data value"
    (is (= {:key :test :pred :type} (models/key-validation :type test-rule {:test 1.1})) ":type")
    (is (= {:key :test :pred :req} (models/key-validation :req test-rule {})) ":req")
    (is (= {:key :test :pred :req-depends} (models/key-validation :req-depends test-rule {:bla true})) ":req-depends")
    (is (= {:key :test :pred :req-fn} (models/key-validation :req-fn test-rule {})) ":req-fn")
    (is (= {:key :test :pred :min-length} (models/key-validation :min-length test-rule {:test "a"})) ":min-length")
    (is (= {:key :test :pred :max-length} (models/key-validation :max-length test-rule {:test "abcdefghijklmnopqrstuvwxyz"})) ":max-length")
    (is (= {:key :test :pred :min} (models/key-validation :min test-rule2 {:test -77777777})) ":min")
    (is (= {:key :test :pred :max} (models/key-validation :max test-rule2 {:test 999999999})) ":max")
    (is (= {:key :test :pred :length} (models/key-validation :length test-rule {:test "ble"})) ":length")
    (is (= {:key :test :pred :contains} (models/key-validation :contains test-rule {:test "bli"})) ":contains")
    (is (= {:key :test :pred :format} (models/key-validation :format test-rule {:test "blu"})) ":format")
    (is (= {:key :test :pred :format-fn} (models/key-validation :format-fn (assoc test-rule :format-fn (fn [_] false)) {:test "teste"})) ":format-fn")))
