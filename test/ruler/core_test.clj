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

(defn validator-with-injection [data]
  (let [injection (:ruler/injection data)
        account-type (:account-type injection)]
    (= "premium" account-type)))

(def test-model
  {:model [{:key :number :type Integer :req true :min 1 :max 10}
           {:key :string :type String :req-depends [:number] :min-length 2 :max-length 4}
           {:key :opt :type String :req-fn (fn [d] (= 10 (:number d))) :format #"[a-z]{2}"}
           {:key :card :type String :req-fn validator-with-injection :format #"\d{4}-\d{4}-\d{4}-\d{4}"}]})

(def test-model-with-opts
  (assoc test-model :opts {:extra-keys? false}))

(deftest validate*-test

  (let [db-injection {:account-type "premium"}]
    (testing "Valid models"
      (is (true? (core/validate* test-model {:number 1 :string "ab"} nil)))
      (is (true? (core/validate* test-model {:number 5 :string "abc"} nil)))
      (is (true? (core/validate* test-model {:number 10 :string "abcd" :opt "si"} nil)))
      (is (true? (core/validate* test-model {:number 7 :string "abcd" :card "1234-1234-1234-1234"} db-injection)))
      (is (true? (core/validate* test-model {:number 10 :string "abcd" :opt "si" :extra "ok"} nil))))

    (testing "Invalid models"
      (is (false? (core/validate* test-model {:number "a"} nil)) "Invalid type for :number")
      (is (false? (core/validate* test-model {:number -9} nil)) "Invalid min value for :number")
      (is (false? (core/validate* test-model {:number 0} nil)) "Invalid min value for :number")
      (is (false? (core/validate* test-model {:number 10} nil)) "Missing required key :opt when :number equals 10")
      (is (false? (core/validate* test-model {:number 11} nil)) "Invalid max value for :number")
      (is (false? (core/validate* test-model {:number 5 :string "a"} nil)) "Invalid min-length for :string")
      (is (false? (core/validate* test-model {:number 5 :opt "a"} nil)) "Invalid format for :opt")
      (is (false? (core/validate* test-model {:number 5 :opt "abcd"} nil)) "Invalid format for :opt")
      (is (false? (core/validate* test-model {:number 6 :string "ab"} db-injection)) "Missing required key :card-number req-fn with injection")
      (is (false? (core/validate* test-model-with-opts {:number 6 :string "ab" :extra 12345} nil)) "Extra key"))))

(deftest describe*-test

  (testing "Described valid models"
    (is (= {:number [:req] :string [:req-depends]} (core/describe* test-model {} nil)) "number required")
    (is (= {:number [:type] :string [:req-depends]} (core/describe* test-model {:number "123"} nil)) "invalid number type")
    (is (= {:number [:min] :string [:req-depends]} (core/describe* test-model {:number -1} nil)) "invalid number limit min")
    (is (= {:number [:max] :string [:req-depends]} (core/describe* test-model {:number 999} nil)) "invalid number limit max")
    (is (= {:string [:type]} (core/describe* test-model {:number 7 :string 123} nil)) "invalid string type")
    (is (= {:string [:min-length]} (core/describe* test-model {:number 7 :string "1"} nil)) "invalid min-length")
    (is (= {:string [:max-length]} (core/describe* test-model {:number 7 :string "1234567890"} nil)) "invalid max-length")
    (is (= {:ruler/extra-keys? [:extra]} (core/describe* test-model-with-opts {:number 6 :string "ab" :extra 12345} nil)) "invalid extra key"))

  (testing "Described invalid models"))
