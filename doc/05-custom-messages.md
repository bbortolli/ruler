# Custom messages
It is possible to define custom messages for each predicate existing in a rule.

## Defining messages
Using the 'set-config!' function, you can define a key called :custom-messages in a map where the keys represent the predicates, and the values are your custom functions. These custom functions should take two parameters: the rule itself and the data.

## Examples
```clj
;; Here the key ':req' makes reference to the rule key ':req'
(def custom-messages
  {:req (fn [rule data] (str "Where is this key?" (:key data)))})

(ruler.core/set-config! :custom-messages custom-messages)

(ruler.core/defmodel
  :test
  {:custom-test {:type String :req true}})

(ruler.core/messages :test {})

=> ["Where is this key? :custom-test"]
```

## Attention
The configuration setting :custom-messages is a complete value. If you define it multiple times, the last definition will take precedence and persist. We recommend defining it once and using it consistently throughout your code.
