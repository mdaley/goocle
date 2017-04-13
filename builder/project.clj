(defproject builder "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [camel-snake-kebab "0.4.0" :exclusions [org.clojure/clojure]]
                 [org.reflections/reflections "0.9.11"]
                 [javax.servlet/servlet-api "2.5"]]
  :main builder.core)
