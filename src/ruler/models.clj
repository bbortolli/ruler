(ns ruler.models)

(defn type->types [i]
  (cond
    (= Integer i)
    #{Integer Long Short Byte}
    :else
    #{i}))

(defn ->validation-error [err k p]
  (when err
    {:key k :pred p}))

(defmulti data-key-validation
  (fn [k _rule _data] k))

(defmethod data-key-validation :type
  [k rule data]
  (let [rule-type (:type rule)
        key (:key rule)
        val (get data key)
        val? (some? val)
        types (type->types rule-type)
        no-instance? (not (some #(instance? % val) types))
        err (and val? no-instance?)]
    (->validation-error err key k)))

(defmethod data-key-validation :req
  [k rule data]
  (let [key (:key rule)
        err (and (:req rule) (nil? (get data key)))]
    (->validation-error err key k)))

(defmethod data-key-validation :req-depends
  [k rule data]
  (let [key (:key rule)
        values (vals (select-keys data (:req-depends rule)))
        every-deps? (every? some? values)
        err (and every-deps? (nil? (get data key)))]
    (->validation-error err key k)))

(defmethod data-key-validation :req-fn
  [k rule data]
  (let [key (:key rule)
        req-fn (:req-fn rule)
        req? (req-fn data)
        err (and req? (nil? (get data key)))]
    (->validation-error err key k)))

(defn- limits-validation-error [min max val]
  (not (>= max val min)))

(defmethod data-key-validation :min
  [k {:keys [key min]} data]
  (when-let [val (get data key)]
    (when (number? val)
      (let [max Double/POSITIVE_INFINITY
            err (limits-validation-error min max val)]
        (->validation-error err key k)))))

(defmethod data-key-validation :max
  [k {:keys [key max]} data]
  (when-let [val (get data key)]
    (when (number? val)
      (let [min Double/NEGATIVE_INFINITY
            err (limits-validation-error min max val)]
        (->validation-error err key k)))))

(defmethod data-key-validation :min-length
  [k {:keys [key min-length]} data]
  (when-let [val (get data key)]
    (when (string? val)
      (let [max Integer/MAX_VALUE
            length (count (get data key))
            err (limits-validation-error min-length max length)]
        (->validation-error err key k)))))

(defmethod data-key-validation :max-length
  [k {:keys [key max-length]} data]
  (when-let [val (get data key)]
    (when (string? val)
      (let [min -1
            length (count (get data key))
            err (limits-validation-error min max-length length)]
        (->validation-error err key k)))))

(defmethod data-key-validation :length
  [k rule data]
  (let [key (:key rule)
        val  (get data key)
        length (:length rule)
        val' (if (string? val) (count val) val)
        err  (limits-validation-error length length val')]
    (->validation-error err key k)))

(defmethod data-key-validation :contains
  [k rule data]
  (let [key (:key rule)
        val (get data key)
        values (:contains rule)
        err (not (contains? (set values) val))]
    (->validation-error err key k)))

(defn format-validation-error [fn val]
  (not (fn val)))

(defmethod data-key-validation :format
  [k rule data]
  (let [key (:key rule)
        fmt (:format rule)
        partial-match (partial re-matches fmt)
        err (format-validation-error partial-match (get data key))]
    (->validation-error err key k)))

(defmethod data-key-validation :format-fn
  [k rule data]
  (let [key (:key rule)
        format-fn (:format-fn rule)
        err (format-validation-error format-fn (get data key))]
    (->validation-error err key k)))
