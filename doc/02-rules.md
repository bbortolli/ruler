# Introduction to rules

## Rule

### What is a rule?
Rule is data. A map with keys and values which defines a field.

### Allowed keys

- Required keys for a rule

| Param       | Type       | Description                           |
| :---------- | :--------- | :---------------------------------- |
| `:key` | `keyword` | Reference for the field to be checked. See note (1) |
| `:type` | `<allowed-types>` | Defines the type to this field |

#### (1) If you use map structure, the ':key' is already the keyword to reference a rule

- Optional keys

| Param       | Type       | Description                           |
| :---------- | :--------- | :---------------------------------- |
| `:req` | `boolean` | If the field is required or not |
| `:req-depends` | `collection` | A collection of fields that implies to be required |
| `:req-fn` | `function` | Custom function to define if required |
| `:excluded` | `collection` | A collection of fields mutually excluded by |
| `:excluded-fn` | `function` | Custom function to make this invalidated by |
| `:name` | `string` | Custom name to the field |
| `:min` | `number` | Minimum value for a number |
| `:max` | `number` | Max value for a number |
| `:length` | `integer` | A fixed length for a string |
| `:min-length` | `integer` | Minimum length of a string |
| `:max-length` | `integer` | Max length of a string |
| `:contains` | `vector or set` | Set of values allowed to the field |
| `:format` | `regex` | A regex to validate value format |
| `:format-fn` | `function` | Custom function to validate the value |

### Explaining some keys

#### Required fields
- :req is trivial.
- :req-depends: create dependencies between fields.

Example, Required field :document-type only when :document exists
```clj
(def my-rules
  {:document      {:type String :req false}
   :document-type {:type String :req-depends [:document]}})

;; OR

(def my-rules
  [{:key :document :type String :req false}
   {:key :document-type :type String :req-depends [:document]}])
```

- :req-fn: If you need a custom validation, this field allows you to create a function which receives the data as param and return a boolean.

Example: Required field :driver-license only if :age is >= 21
```clj
(defn custom-validator [data]
  (>= (:age data) 21))

(def my-rules2
  {:age            {:type Integer :req false}
   :driver-license {:type String  :req-fn custom-validator}})

;; OR

(def my-rules2
  [{:key :age :type Integer :req false}
   {:key :driver-license :type String :req-fn custom-validator}])
```

Example: :passport: and :driver-license mutually exclusive
```clj
(def my-rules3
  [{:key :age :type Integer :req false}
   {:key :passport :type String :excluded [:driver-license]}
   {:key :driver-license :type String :excluded [:passport]}])

```
Example: :driver-license should not be present if age < 21
```clj
(def my-rules3
  [{:key :age :type Integer :req true}
   {:key :driver-license :type String :excluded-fn (fn [data] (< (:age data) 21))}])

;; Another way is defining a function to be used at required-fn and used
(defn maiority-age? [data]
  (>= (:age data) 21))

;; define its negative
(def not-maiority-age? (comp not maiority-age?)

;; Now it is required if age >= 21 and invalid if not
(def my-rules3
  [{:key :age :type Integer :req true}
   {:key :driver-license :type String :req-fn maiority-age? :excluded-fn not-maiority-age?}])
```

### Examples
```clj
(def my-rules3
  {:first-name          {:type String :required true :min-length 1 :max-length 256}
   :last-name           {:type String :required true :min-length 1 :max-length 256}
   :date-of-birth       {:type String :required true :length 10}
   :other-date-of-birth {:type String :format #"\d{4}-\d{2}-\d{2}"}
   :protocol-number     {:type Integer :required true}
   :weight              {:type Double :min 0.001}
   :classification      {:type String :contains ["A" "B" "C"]}})

;; OR

(def my-rules3
 [{:key :first-name :type String :required true :min-length 1 :max-length 256}
  {:key :last-name :type String :required true :min-length 1 :max-length 256}
  {:key :date-of-birth :type String :required true :length 10}
  {:key :other-date-of-birth :type String :format #"\d{4}-\d{2}-\d{2}"}
  {:key :protocol-number :type Integer :required true}
  {:key :weight :type Double :min 0.001}
  {:key :classification :type String :contains ["A" "B" "C"]}])
```
