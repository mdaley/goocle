(defproject goocle "0.1.0-SNAPSHOT"
  :description "Wrapper for the google cloud Java client library"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [camel-snake-kebab "0.2.5" :exclusions [org.clojure/clojure]]
                 [com.google.cloud/google-cloud "0.7.0-WithParameters" :exclusions [io.grpc/grpc-core
                                                                                    io.netty/netty-codec-http2]
                  ]
                 [io.grpc/grpc-core "1.1.2"]
                 [io.netty/netty-codec-http2 "4.1.8.Final"]
                 [org.reflections/reflections "0.9.11"]])
