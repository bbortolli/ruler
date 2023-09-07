# Models

## What is it?
Model is a collection of rules identified by a keyword.

## Example
```clj
;; Defining a model name, a keyword (You dont need to do this in pratical)
(def model-name :model-test)

;; Defining a collection of rules.
;; In the following documentation, you will find examples for practical use.
(def rules
  [{:key :name :type String :req true :min-length 1 :max-length 256}
   {:key :document :type String :req true :format  #"\d{8}"}
   {:key :document-code :type Integer :req-depends [:document]}
   {:key :age :type Integer :req true :min 12}
   {:key :driver-license :type String :req-fn (fn [data] (>= (:age data) 21))}])
```
