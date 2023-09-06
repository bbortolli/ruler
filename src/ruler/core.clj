(ns ruler.core
  (:require [ruler.rules :as rules]
            [ruler.models :as models]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Const definitions.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def allowed-config-keys #{:required-msg :invalid-type-msg})

(def initial-config
  {:required-msg #(format "Required field: %s" (:key %))
   :invalid-type-msg #(format "Invalid type for field: %s" (:key %))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn valid-config? [k _value]
  (contains? allowed-config-keys k))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Context manipulation.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce ctx-models* (atom {}))
(defonce ctx-config* (atom initial-config))

(defn- create-model! [k m]
  (swap! ctx-models* assoc k m))

(defn- set-config! [k value]
  (when (valid-config? k value)
    (swap! ctx-config* assoc k value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Internal implementations.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- data->rule-errors [rule data injection]
  (let [keys (keys (dissoc rule :key))
        data' (if (map? injection) (assoc data :ruler/injection injection) data)
        validate-fn #(models/key-validation % rule data')]
    (remove nil? (map validate-fn keys))))

(defn- data->errors [model data injection]
  (let [validate-fn #(data->rule-errors % data injection)]
    (remove nil? (flatten (map validate-fn model)))))

(defn- data->valid-model? [model data injection]
  (let [errors (data->errors model data injection)]
    (empty? errors)))

(defn validate* [model data injection]
  (data->valid-model? model data injection))

(defn describe* [model data injection]
  (let [err (data->errors model data injection)
        grouped (group-by :key err)
        preds-reducer (fn [a k v]
                        (assoc a k (mapv :pred v)))]
    (reduce-kv preds-reducer {} grouped)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public api.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn defmodel
  "Define a new model rules with identifier kw to the context"
  {:added "1.0"}
  [kw model]
  (assert (keyword? kw) "Model identifier should be a keyword.")
  (assert (seq model) "Model can not be empty.")
  (rules/validate-all-rules model)
  (create-model! kw model))

(defn valid?
  "Validate input data following the rules defined at model with identifier k"
  {:added "1.0"}
  ([k data]
   (valid? k data nil))
  ([k data injection]
   (when-let [model (k @ctx-models*)]
     (validate* model data injection))))

(defn describe
  "Describe errors from validating input data following the rules defined at model with identifier k"
  {:added "1.0"}
  ([k data]
   (describe k data nil))
  ([k data injection]
  (when-let [model (k @ctx-models*)]
    (describe* model data injection))))
