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
  (fn [pred _rule _data _cfgs]
    pred))

(defmethod pred-msg :default
  [_pred _rule _data _cfgs]
  nil)

(defmethod pred-msg :type
  [pred rule data cfgs]
  (let [k (:key rule)
        expected-type (remove-prefix-class (:type rule))
        current-type (remove-prefix-class (type (get data k)))]
    (if-let [custom-fn (pred (:custom-messages cfgs))]
      (custom-fn rule data)
      (format "Invalid type for field %s. Expected %s, received %s." k expected-type current-type))))

(defmethod pred-msg :req
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (format "Missing required field %s." (:key rule))))

(defmethod pred-msg :req-depends
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (format "Missing required field %s." (:key rule))))

(defmethod pred-msg :req-fn
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (format "Missing required field %s." (:key rule))))

(defmethod pred-msg :excluded
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (format "Disallowed field %s." (:key rule))))

(defmethod pred-msg :excluded-fn
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (format "Disallowed field %s." (:key rule))))

(defmethod pred-msg :min
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (let [k (:key rule)
          min (:min rule)
          val (get data k)]
      (format "Invalid minimum value for %s. Minimum is %s, received %s." k min val))))

(defmethod pred-msg :max
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (let [k (:key rule)
          max (:max rule)
          val (get data k)]
      (format "Invalid maximum value for %s. Maximum is %s, received %s." k max val))))

(defmethod pred-msg :min-length
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (let [k (:key rule)
          min-length (:min-length rule)
          count-val (count (get data k))]
      (format "Invalid minimum length for %s. Minimum is %s, received %s." k min-length count-val))))

(defmethod pred-msg :max-length
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (let [k (:key rule)
          max-length (:max-length rule)
          count-val (count (get data k))]
      (format "Invalid maximum length for %s. Maximum is %s, received %s." k max-length count-val))))

(defmethod pred-msg :contains
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (let [k (:key rule)
          val (get data k)
          str-vals (cljstr/join ", " (:contains rule))]
      (format "Invalid possible values for %s. Expected %s, received %s." k str-vals val))))

(defmethod pred-msg :format
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (format "Invalid format for %s." (:key rule))))

(defmethod pred-msg :format-fn
  [pred rule data cfgs]
  (if-let [custom-fn (pred (:custom-messages cfgs))]
    (custom-fn rule data)
    (format "Invalid format function for %s." (:key rule))))
