(ns ruler.core
  (:require [ruler.rules :as rules]
            [ruler.models :as models]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Const definitions.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private allowed-config-keys #{:required-msg :invalid-type-msg :global-opts})

(def ^:private initial-config
  {:required-msg #(format "Required field: %s" (:key %))
   :invalid-type-msg #(format "Invalid type for field: %s" (:key %))
   :global-opts {}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ^:no-doc valid-config? [k _value]
  (contains? allowed-config-keys k))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Context manipulation.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce ^:private ctx-models* (atom {}))
(defonce ^:private ctx-config* (atom initial-config))

(defn- create-model! [k m]
  (swap! ctx-models* assoc k m))

(defn set-config! [k value]
  (when (valid-config? k value)
    (swap! ctx-config* assoc k value)))

(defn- merge-opts [model]
  (if-let [global-opts (:global-opts @ctx-config*)]
    (update model :opts merge global-opts)
    model))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Internal implementations.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- data->rule-errors [rule data injection]
  (let [keys (keys (dissoc rule :key))
        data' (if injection (assoc data :ruler/injection injection) data)
        validate-fn #(models/key-validation % rule data')]
    (remove nil? (map validate-fn keys))))

(defn data->opts-errors [{:keys [model opts]} data]
  (when-let [opt-keys (keys opts)]
    (let [validate-fn #(models/opt-key-validation % model opts data)]
      (remove nil? (map validate-fn opt-keys)))))

(defn- data->errors [model data injection]
  (let [validate-fn #(data->rule-errors % data injection)
        rule-errors (remove nil? (flatten (map validate-fn (:model model))))
        opts-errors (remove nil? (data->opts-errors model data))]
    {:rule-errors rule-errors
     :opts-errors (apply merge opts-errors)}))

(defn- data->valid-model? [model data injection]
  (let [model' (merge-opts model)
        errors (data->errors model' data injection)]
    (and (empty? (:rule-errors errors))
         (empty? (:opts-errors errors)))))

(defn ^:no-doc validate* [model data injection]
  (data->valid-model? model data injection))

(defn ^:no-doc describe* [model data injection]
  (let [model' (merge-opts model)
        err (data->errors model' data injection)
        rule-errors (:rule-errors err)
        opts-errors (:opts-errors err)
        grouped (group-by :key rule-errors)
        preds-reducer (fn [a k v]
                        (assoc a k (mapv :pred v)))]
    (reduce-kv preds-reducer opts-errors grouped)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public api.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn defmodel
  "Define a new model with a set of rules identified as 'kw'."
  {:added "1.0"}
  ([kw model]
   (defmodel kw model nil))
  ([kw model opts]
   (assert (keyword? kw) "Model identifier should be a keyword.")
   (assert (seq model) "Model can not be empty.")
   (rules/validate-all-rules model)
   (create-model! kw {:model model :opts opts})))

(defn valid?
  "Validate input data following the rules defined in the model identified as 'k'."
  {:added "1.0"}
  ([k data]
   (valid? k data nil))
  ([k data injection]
   (when-let [model (k @ctx-models*)]
     (validate* model data injection))))

(defn describe
  "Describe errors encountered when validating input data
    according to the rules defined in the model identified as 'k'."
  {:added "1.0"}
  ([k data]
   (describe k data nil))
  ([k data injection]
  (when-let [model (k @ctx-models*)]
    (describe* model data injection))))
