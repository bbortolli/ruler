# Introduction to ruler

## Rule

### What is a rule?
Rule is data. A map with keys and values which defines a field.

### Allowed keys

- Required keys for a rule

| Param       | Type       | Description                           |
| :---------- | :--------- | :---------------------------------- |
| `:key` | `keyword` | Reference for the field to be checked |
| `:type` | `<allowed-types>` | Defines the type to this field |

- Optional keys

| Param       | Type       | Description                           |
| :---------- | :--------- | :---------------------------------- |
| `:req` | `boolean` | If the field is required or not |
| `:req-depends` | `collection` | A collection of other fields that |
| `:req-fn` | `function` | Custom function to define if required |
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
  [{:key :document :type String :req false}
   {:key :document-type :type String :req-depends [:document]}])
```

- :req-fn: If you need a custom validation, this field allows you to create a function which receives the data as param and return a boolean.

Example: Required field :driver-license only if :age is >= 21
```clj
(defn custom-validator [data]
  (>= (:age data) 21))

(def my-rules2
  [{:key :age :type Integer :req false}
   {:key :driver-license :type String :req-fn custom-validator}])
```

### Examples
```clj
(def my-rules3
 [{:key :first-name :type String :required true :min-length 1 :max-length 256}
  {:key :last-name :type String :required true :min-length 1 :max-length 256}
  {:key :date-of-birth :type String :required true :length 10}
  {:key :other-date-of-birth :type String :format #"\d{4}-\d{2}-\d{2}"}
  {:key :protocol-number :type Integer :required true}
  {:key :weight :type Double :min 0.001}
  {:key :classification :type String :contains ["A" "B" "C"]}])
```
