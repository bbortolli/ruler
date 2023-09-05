(ns ruler.core-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [ruler.core :as core]))

(deftest valid-config?-test
  (testing "Valid configs"
    (is (true? (core/valid-config? :required-msg nil)))
    (is (true? (core/valid-config? :invalid-type-msg nil))))
  (testing "Valid configs"
    (is (false? (core/valid-config? :invalid nil)))))

(def test-model [{:key :number :type Integer :req true :min 1 :max 10}
                 {:key :string :type String :req-depends [:number] :min-length 2 :max-length 4}
                 {:key :opt :type String :req-fn (fn [d] (= 10 (:number d)))}])

(deftest validate*-test
  (testing "Valid models"
    (is (true? (core/validate* test-model {:number 1 :string "ab"})))
    (is (true? (core/validate* test-model {:number 5 :string "abc"})))
    (is (true? (core/validate* test-model {:number 10 :string "abcd" :opt "siiiiii"}))))
  (testing "Invalid models"
    (is (false? (core/validate* test-model {:number "a"})) "Invalid type for :number")
    (is (false? (core/validate* test-model {:number -9})) "Invalid min value for :number")
    (is (false? (core/validate* test-model {:number 0})) "Invalid min value for :number")
    (is (false? (core/validate* test-model {:number 10})) "Missing required key :opt when :number equals 10")
    (is (false? (core/validate* test-model {:number 11})) "Invalid max value for :number")
    (is (false? (core/validate* test-model {:number 5 :string "a"})) "Invalid min-length for :string")
    (is (false? (core/validate* test-model {:number 5 :string "abcdef"})) "Invalid max-length for :string")))

(deftest describe-test
  (testing "Described valid models")
  (testing "Described invalid models"))
