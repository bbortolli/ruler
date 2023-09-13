(ns ruler.messages-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [ruler.messages :as messages]))

(deftest pred-msg-test
  (testing "Returning messages for every :pred"
    (is (nil? (messages/pred-msg :blablabla {} {})))
    (is (= "Invalid type for field :f. Expected Integer, received String."
           (messages/pred-msg :type        {:key :f :type Integer}              {:f "123"})))
    (is (= "Missing required field :f."
           (messages/pred-msg :req         {:key :f :req true}                  {:f "123"})))
    (is (= "Missing required field :f."
           (messages/pred-msg :req-depends {:key :f :req-depends [:f2]}         {:f2 "123"})))
    (is (= "Missing required field :f."
           (messages/pred-msg :req-fn      {:key :f :req-fn (fn [_] true)}      {:f "123"})))
    (is (= "Disallowed field :f."
           (messages/pred-msg :excluded    {:key :f :excluded [:f2]}            {:f "123" :f2 "123"})))
    (is (= "Disallowed field :f."
           (messages/pred-msg :excluded-fn {:key :f :excluded-fn (fn [_] true)} {:f "123"})))
    (is (= "Invalid minimun value for :f. Minimun is 1, received 0."
           (messages/pred-msg :min         {:key :f :min 1}                     {:f 0})))
    (is (= "Invalid maximun value for :f. Maximun is 5, received 6."
           (messages/pred-msg :max         {:key :f :max 5}                     {:f 6})))
    (is (= "Invalid minimun length for :f. Minimun is 2, received 1."
           (messages/pred-msg :min-length  {:key :f :min-length 2}              {:f "1"})))
    (is (= "Invalid maximun length for :f. Maximun is 4, received 6."
           (messages/pred-msg :max-length  {:key :f :max-length 4}              {:f "123456"})))
    (is (= "Invalid possible values for :f. Expected bla, ble, received 123."
           (messages/pred-msg :contains    {:key :f :contains ["bla" "ble"]}    {:f "123"})))
    (is (= "Invalid format for :f."
           (messages/pred-msg :format      {:key :f :format #""}                {:f "123"})))
    (is (= "Invalid format function for :f."
           (messages/pred-msg :format-fn   {:key :f :format-fn (fn [_] false)}  {:f "123"})))))
