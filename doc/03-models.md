# Models

## What is it?
Model is a collection of rules identified by a keyword.
It can be defined as a map or collection of maps. It's all about you.
After you define a model with 'defmodel' it is all the same.

## Example
```clj
;; Defining a model name, a keyword (You dont need to do this in pratical)
(def model-name :model-test)

;; Definig model as map
(def rules
  {:name           {:type String :req true :min-length 1 :max-length 256}
   :document       {:type String :req true :format #"\d{8}"}
   :document-code  {:type Integer :req-depends [:document]}
   :age            {:type Integer :req true :min 12}
   :driver-license {:type String :req-fn (fn [data] (>= (:age data) 21))}})

;; Defining a collection of rules.
(def rules
  [{:key :name           :type String :req true :min-length 1 :max-length 256}
   {:key :document       :type String :req true :format  #"\d{8}"}
   {:key :document-code  :type Integer :req-depends [:document]}
   {:key :age            :type Integer :req true :min 12}
   {:key :driver-license :type String :req-fn (fn [data] (>= (:age data) 21))}])
```

- In the following documentation, you will find examples for practical use.

## Opts
A config map used to define additional validation to data map

| Param       | Type       | Description                           |
| :---------- | :--------- | :---------------------------------- |
| `:extra-keys?` | `boolean` | If false, do not allow extra fields in data map (default is true) |

### Examples
```clj
;; In the next page, you will find examples for practical use.
(def my-opts {:extra-keys? false})
```
