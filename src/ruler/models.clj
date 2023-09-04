(ns ruler.models)

(defmulti data-key-validation
  (fn [k _rule _data] k))

(defmethod data-key-validation :type
  [k rule data]
  (let [rule-type (:type rule)
        val (get data (:key rule))
        err (not (instance? rule-type val))]
    (when err "Erro :type")))

(defmethod data-key-validation :req
 [k rule data]
 (let [err (and (:req rule) (nil? (get data (:key rule))))]
   (when err "Erro :req")))

(defmethod data-key-validation :req-depends
 [k rule data]
 (let [every-deps? (every? some? (vals (select-keys data (:req-depends rule))))
       err (and (not every-deps?) (nil? (get data (:key rule))))]
   (when err "Erro :req-depends")))

(defmethod data-key-validation :req-fn
 [k rule data]
 (let [req-fn (:req-fn rule)
       err (and (req-fn data) (nil? (get data (:key rule))))]
   (when err "Erro :req-fn")))

(defn- limits-validation-error [min max val]
  (not (>= max val min)))

(defmethod data-key-validation :min
  [k rule data]
  (let [err  (limits-validation-error (:min rule) Double/POSITIVE_INFINITY (get data (:key rule)))]
    (when err "Erro :min")))

(defmethod data-key-validation :max
  [k rule data]
  (let [err  (limits-validation-error Double/NEGATIVE_INFINITY (:max rule) (get data (:key rule)))]
    (when err "Erro :max")))

(defmethod data-key-validation :min-length
  [k rule data]
  (let [err  (limits-validation-error (:min-length rule) Integer/MAX_VALUE (count (get data (:key rule))))]
    (when err "Erro :min-length")))

(defmethod data-key-validation :max-length
  [k rule data]
  (let [err  (limits-validation-error -1 (:max-length rule) (count (get data (:key rule))))]
    (when err "Erro :max-length")))

(defmethod data-key-validation :length
  [k rule data]
  (let [val  (get data (:key rule))
        length (:length rule)
        val' (if (string? val) (count val) val)
        err  (limits-validation-error length length val')]
    (when err "Erro :length")))

(defmethod data-key-validation :contains
  [k rule data]
  (let [val (get data (:key rule))
        values (:contains rule)
        err (not (contains? (set values) val))]
    (when err "Erro :contains")))

(defn format-validation-error [fn val]
  (not (fn val)))

(defmethod data-key-validation :format
  [k rule data]
  (let [fmt (:format rule)
        err (format-validation-error (partial re-matches fmt) (get data (:key rule)))]
    (when err "Erro :format")))

(defmethod data-key-validation :format-fn
  [k rule data]
  (let [format-fn (:format-fn rule)
        err (format-validation-error format-fn (get data (:key rule)))]
    (when err "Erro :format-fn")))