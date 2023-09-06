(ns ^:no-doc ruler.rules)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Const definitions.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def Map clojure.lang.IPersistentMap)
(def Vector clojure.lang.IPersistentVector)
(def Regex java.util.regex.Pattern)
(def allowed-rule-keys-req #{:key :type})
(def allowed-types #{Integer Float Double String Boolean Map Vector})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- allowed-type? [type]
  (contains? allowed-types type))

(defn- vec-or-set [v]
  (or (vector? v)
      (set? v)))

(defn- regex? [r]
  (instance? Regex r))

(defn- key-type-error [k text]
  (format "Expected type for '%s' is %s" k text))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Lookups.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def key->fn
  {:key         keyword?
   :type        allowed-type?
   :req         boolean?
   :req-depends coll?
   :req-fn      fn?
   :name        string?
   :min         number?
   :max         number?
   :min-length  integer?
   :max-length  integer?
   :contains    vec-or-set
   :format      regex?
   :format-fn   fn?})

(def key->type-text
  {:key         "Keyword"
   :type        "'allowed-types'. Check docs for more information"
   :req         "Boolean"
   :req-depends "Collection"
   :req-fn      "Function"
   :name        "String"
   :min         "Number"
   :max         "Number"
   :min-length  "Integer"
   :max-length  "Integer"
   :contains    "Vector or Set"})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Rules.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- check-rule-fields [rule]
  (let [reqs (vec allowed-rule-keys-req)
        req-vals (map (fn req-vals-map [k] (k rule)) reqs)]
    (assert (every? some? req-vals) "Missing required keys for rule.")))

(defn- check-rule-vals* [key rule]
  (if-let [func (key key->fn)]
    (let [val (key rule)
          text (key key->type-text)]
      (assert (func val) (key-type-error key text)))
    (assert false (format "Unknow key provided: %s" key))))

(defn- check-rule-vals [rule]
  (doseq [key (keys rule)]
    (check-rule-vals* key rule)))

(defn check-rule [rule]
  (check-rule-fields rule)
  (check-rule-vals rule))

(defn validate-all-rules [model]
  (doseq [rule model]
    (check-rule rule)))
