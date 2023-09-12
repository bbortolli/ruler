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

(deftest core-public-api-test

  (testing "Setting a config"
    (is (= {:extra-keys? true}
           (:global-opts (core/set-config! :global-opts {:extra-keys? true})))))

  (testing "Defining valid models"
    (is (= {:test {:model [{:key :text :type java.lang.String}] :opts nil}}
           (core/defmodel :test [{:key :text :type String}])))
    (is (= {:test {:model [{:key :text :type java.lang.String}] :opts {:extra-keys? true}}}
           (core/defmodel :test [{:key :text :type String}] {:extra-keys? true}))))

  (testing "Defining invalid models"
    (is (thrown? java.lang.AssertionError (core/defmodel "not-a-keyword" [{:key :text :type String :req true}])))
    (is (thrown? java.lang.AssertionError (core/defmodel :empty-rules [])))
    (is (thrown? java.lang.AssertionError (core/defmodel :invalid-rules [{:key "text" :type []}]))))

  (testing "Validating models without extra keys config"
    (core/defmodel :test [{:key :text :type String :req true}])
    (is (true? (core/valid? :test {:text "My String xD"})))
    (is (true? (core/valid? :test {:text "My String xD" :another-key true})))
    (is (false? (core/valid? :test {:not-text-key "My String xD"})))
    (is (false? (core/valid? :test {}))))

  (testing "Validating models with extra keys config"
    (core/set-config! :global-opts {:extra-keys? false})
    (core/defmodel :test [{:key :text :type String :req true}])
    (is (true? (core/valid? :test {:text "My String xD"})))
    (is (true? (core/valid? :test {:text "My String xD"} {:injection "data"})))
    (is (false? (core/valid? :test {:text "My String xD" :another-key true})))
    (is (false? (core/valid? :test {:text "My String xD" :another-key true} {:injection "data"}))))

  (testing "Validating not defined model"
    (is (nil? (core/valid? :i-dont-exist {})))
    (is (nil? (core/valid? :i-dont-exist {:text 1})))
    (is (nil? (core/valid? :i-dont-exist {:text "My String xD"}))))

  (testing "Describing models"
    (core/defmodel :test [{:key :text :type String :req true}
                          {:key :number :type Integer}
                          {:key :from-fn :type String :req-fn (fn [data] (true? (some-> data :ruler/injection :value)))}])

    (is (nil? (core/describe :test {:text "1" :number 10})))
    (is (nil? (core/describe :test {:text "1" :number 10} {:value false})))
    (is (nil? (core/describe :test {:text "1" :number 10 :from-fn "im-here"} {:value true})))
    (is (= {:text [:type] :number [:type]}
           (core/describe :test {:text 1 :number "not-a-number"})))
    (is (= {:from-fn [:req-fn]}
           (core/describe :test {:text "1" :number 10} {:value true})))))
