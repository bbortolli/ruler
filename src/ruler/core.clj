(ns ruler.core
  (:require [ruler.rules :as rules]
            [ruler.models :as models]))

;; defs
(def allowed-config-keys #{:required-msg :invalid-type-msg})

;; context
(defn custom-required-msg [rule _data]
  (format "Required field: %s" (:key rule)))

(defn custom-type-msg [rule _data]
  (format "Invalid type for field: %s" (:key rule)))

(def models* (atom {}))
(def config (atom {:required-msg custom-required-msg
                   :invalid-type-msg custom-type-msg}))

(defn create-model! [k m]
  (swap! models* assoc k m))

(defn valid-config? [k _value]
  (contains? allowed-config-keys k))

(defn set-config! [k value]
  (when (valid-config? k value)
    (swap! config assoc k value)))

(defn data->rule-errors [rule data]
  (let [keys (keys (dissoc rule :key))
        validate-fn #(models/data-key-validation % rule data)]
    (remove nil? (map validate-fn keys))))

(defn data->model-errors [model data]
  (let [validate-fn #(data->rule-errors % data)]
    (remove nil? (flatten (map validate-fn model)))))

(defn data->valid-model? [model data]
  (let [errors (data->model-errors model data)]
    (empty? errors)))

;; ! Internal API. Do not use
(defn validate* [model data]
  (data->valid-model? model data))

;; ! Core API for usage
(defn defmodel [kw model]
  (assert (keyword? kw) "Model identifier should be a keyword.")
  (assert (seq model) "Model can not be empty.")
  (rules/validate-all-rules model)
  (create-model! kw model))

;; Example usage:
;; (defmodel
;;   :pessoa
;;   [{:key :name :type String :required true}
;;    {:key :age :type Integer :required true}
;;    {:key :driver-license :type String :req-fn (fn [data] (>= (:age data) 21))}])
;; => nil

(defn validate [k data]
  (when-let [model (k @models*)]
    (validate* model data)))

(defn describe [k data]
  (when-let [model (k @models*)]
    (let [err (data->model-errors model data)
          grouped (group-by :key err)]
      grouped)))
