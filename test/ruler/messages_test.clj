(ns ruler.messages-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [ruler.messages :as messages]))

(defn custom-message [_rule _data]
  "Custom xD")

(def custom-messages
  {:custom-messages
   (reduce #(assoc %1 %2 custom-message)
           {}
           [:type :req :req-depends :req-fn
            :excluded :excluded-fn
            :min :max :min-length :max-length
            :contains :format :format-fn])})

(deftest pred-msg-test
  (testing "Returning messages for every :pred"
    (is (nil? (messages/pred-msg :blablabla {} {} nil)))
    (is (= "Invalid type for field :f. Expected Integer, received String."
           (messages/pred-msg :type        {:key :f :type Integer}              {:f "123"} nil)))
    (is (= "Missing required field :f."
           (messages/pred-msg :req         {:key :f :req true}                  {:f "123"} nil)))
    (is (= "Missing required field :f."
           (messages/pred-msg :req-depends {:key :f :req-depends [:f2]}         {:f2 "123"} nil)))
    (is (= "Missing required field :f."
           (messages/pred-msg :req-fn      {:key :f :req-fn (fn [_] true)}      {:f "123"} nil)))
    (is (= "Disallowed field :f."
           (messages/pred-msg :excluded    {:key :f :excluded [:f2]}            {:f "123" :f2 "123"} nil)))
    (is (= "Disallowed field :f."
           (messages/pred-msg :excluded-fn {:key :f :excluded-fn (fn [_] true)} {:f "123"} nil)))
    (is (= "Invalid minimum value for :f. Minimum is 1, received 0."
           (messages/pred-msg :min         {:key :f :min 1}                     {:f 0} nil)))
    (is (= "Invalid maximum value for :f. Maximum is 5, received 6."
           (messages/pred-msg :max         {:key :f :max 5}                     {:f 6} nil)))
    (is (= "Invalid minimum length for :f. Minimum is 2, received 1."
           (messages/pred-msg :min-length  {:key :f :min-length 2}              {:f "1"} nil)))
    (is (= "Invalid maximum length for :f. Maximum is 4, received 6."
           (messages/pred-msg :max-length  {:key :f :max-length 4}              {:f "123456"} nil)))
    (is (= "Invalid possible values for :f. Expected bla, ble, received 123."
           (messages/pred-msg :contains    {:key :f :contains ["bla" "ble"]}    {:f "123"} nil)))
    (is (= "Invalid format for :f."
           (messages/pred-msg :format      {:key :f :format #""}                {:f "123"} nil)))
    (is (= "Invalid format function for :f."
           (messages/pred-msg :format-fn   {:key :f :format-fn (fn [_] false)}  {:f "123"} nil))))


  (testing "With custom functions for every :pred"
    (is (= "Custom xD"
           (messages/pred-msg :type        {:key :f :type Integer}              {:f "123"} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :req         {:key :f :req true}                  {:f "123"} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :req-depends {:key :f :req-depends [:f2]}         {:f2 "123"} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :req-fn      {:key :f :req-fn (fn [_] true)}      {:f "123"} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :excluded    {:key :f :excluded [:f2]}            {:f "123" :f2 "123"} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :excluded-fn {:key :f :excluded-fn (fn [_] true)} {:f "123"} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :min         {:key :f :min 1}                     {:f 0} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :max         {:key :f :max 5}                     {:f 6} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :min-length  {:key :f :min-length 2}              {:f "1"} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :max-length  {:key :f :max-length 4}              {:f "123456"} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :contains    {:key :f :contains ["bla" "ble"]}    {:f "123"} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :format      {:key :f :format #""}                {:f "123"} custom-messages)))
    (is (= "Custom xD"
           (messages/pred-msg :format-fn   {:key :f :format-fn (fn [_] false)}  {:f "123"} custom-messages)))))
