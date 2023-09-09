(defproject ruler "0.1.0-SNAPSHOT"
  :description "Data validation in pure functionally library for Clojure"
  :author "Bruno Fernandes Bortolli"
  :url "https://github.com/bbortolli/ruler"
  :license {:name "Eclipse Public License - v 1.0"
            :url  "https://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :plugins [[lein-cloverage "1.2.2"]]
  :scm {:name "git" :url "https://github.com/bbortolli/ruler"}
  :repl-options {:init-ns ruler.core}
  :profiles {:dev {:dependencies [[lambdaisland/kaocha "1.86.1355"]]}}
  :aliases {"kaocha"   ["run" "-m" "kaocha.runner"]}
  :cloverage {:output-dir "coverage"
              :coverage-threshold [80 90]})
