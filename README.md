
![Test](https://github.com/bbortolli/clj-ruler/actions/workflows/clojure-test.yaml/badge.svg) [![Clojars Project](https://img.shields.io/clojars/v/org.clojars.bbortolli/ruler.svg)](https://clojars.org/org.clojars.bbortolli/ruler)


# Ruler

A Clojure library designed to validate input data defining map rules.

## Usage

### Defining a model, a set of rules
See [rules](./doc/02-rules.md) for more info

In the example a model with identifier :model-test is being created
```clj
(ruler.core/defmodel
  :model-test
  [{:key :name :type String :req true :min-length 1 :max-length 256}
   {:key :document :type String :req true :format  #"\d{8}"}
   {:key :document-code :type Integer :req-depends [:document]}
   {:key :age :type Integer :req true :min 12}
   {:key :driver-license :type String :req-fn (fn [data] (>= (:age data) 21))}])
```

### Validating a model
To validate a model you should call this function 'valid?'.

Here, a valid data:
```clj
(ruler.core/valid?
  :model-test
  {:name "Foo" :document "12341234" :document-code 11
   :age 25 :driver-license "blebliblu"})

=> true
```

Now, a invalid data. Missing required :driver-license because age is >= 21.
```clj
(ruler.core/valid?
  :model-test
  {:name "Foo" :document "12341234" :document-code 11 :age 22})

=> false
```

In real world, many times we depends of external sources. The function 'valid?' can be called with a 3rd param with inject values to data, so it can be used later.

See example:

```clj
;; a key :ruler/injection will be added to data
(defn custom-validation [data]
  (when-let [injection (:ruler/injection data)]
    (= "premium" (:account-type injection))))

(ruler.core/defmodel
  :injection-test
  [{:key :account :type String :req true}
   {:key :verification :type String :format #"[0-9]{1}x[A-Z]{3}#" :req-fn custom-validation}])

(defn select-something [args]
  {:account-type "premium"})

(defn your-program [args]
  ;; something ...
  (let [fake-db-result (select-something args)]
    (ruler.core/valid? :injection-test
      {:account "premium-account" :verification "0xABC#"}
      fake-db-result)))
```

### Describing validation
```clj
(ruler.core/defmodel
  :model-test
  [{:key :name :type String :req true :min-length 2 :max-length 10}
   {:key :document :type String :req true :format #"\d{8}"}])

;; Missing required keys
(ruler.core/describe :model-test {})
=> {:name [:req] :document [:req]}

;; Invalid type and format
(ruler.core/describe :model-test {:name 123 :document "aX1!-a@"})
=> {:name [:type] :document [:format]}

;; Invalid length
(ruler.core/describe :model-test {:name "fz" :document "11112222"})
=> {:name [:min-length]}

(ruler.core/describe :model-test {:name "fuzzyfuzzyxd" :document "11112222"})
=> {:name [:max-length]}
```

## License

Eclipse Public License 2.0

Copyright © 2023
