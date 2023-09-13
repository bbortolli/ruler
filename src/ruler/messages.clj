(ns ^:no-doc ruler.messages
    (:require [clojure.string :as cljstr]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- remove-prefix-class [class]
  (cljstr/replace (str class) #"class java.lang." ""))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Messages.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti pred-msg
  (fn [pred _rule _data]
    pred))

(defmethod pred-msg :default
  [_pred _rule _data]
  nil)

(defmethod pred-msg :type
  [_pred rule data]
  (let [k (:key rule)
        expected-type (remove-prefix-class (:type rule))
        current-type (remove-prefix-class (type (get data k)))]
    (format "Invalid type for field %s. Expected %s, received %s." k expected-type current-type)))

(defmethod pred-msg :req
  [_pred rule _data]
  (format "Missing required field %s." (:key rule)))

(defmethod pred-msg :req-depends
  [_pred rule _data]
  (format "Missing required field %s." (:key rule)))

(defmethod pred-msg :req-fn
  [_pred rule _data]
  (format "Missing required field %s." (:key rule)))

(defmethod pred-msg :excluded
  [_pred rule _data]
  (format "Disallowed field %s." (:key rule)))

(defmethod pred-msg :excluded-fn
  [_pred rule _data]
  (format "Disallowed field %s." (:key rule)))

(defmethod pred-msg :min
  [_pred rule data]
  (let [k (:key rule)
        min (:min rule)
        val (get data k)]
    (format "Invalid minimun value for %s. Minimun is %s, received %s." k min val)))

(defmethod pred-msg :max
  [_pred rule data]
  (let [k (:key rule)
        max (:max rule)
        val (get data k)]
    (format "Invalid maximun value for %s. Maximun is %s, received %s." k max val)))

(defmethod pred-msg :min-length
  [_pred rule data]
  (let [k (:key rule)
        min-length (:min-length rule)
        count-val (count (get data k))]
    (format "Invalid minimun length for %s. Minimun is %s, received %s." k min-length count-val)))

(defmethod pred-msg :max-length
  [_pred rule data]
  (let [k (:key rule)
        max-length (:max-length rule)
        count-val (count (get data k))]
    (format "Invalid maximun length for %s. Maximun is %s, received %s." k max-length count-val)))

(defmethod pred-msg :contains
  [_pred rule data]
  (let [k (:key rule)
        val (get data k)
        str-vals (cljstr/join ", " (:contains rule))]
    (format "Invalid possible values for %s. Expected %s, received %s." k str-vals val)))

(defmethod pred-msg :format
  [_pred rule _data]
  (format "Invalid format for %s." (:key rule)))

(defmethod pred-msg :format-fn
  [_pred rule _data]
  (format "Invalid format function for %s." (:key rule)))
