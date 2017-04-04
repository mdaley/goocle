(defproject goocle "0.1.0-SNAPSHOT"
  :description "Wrapper for the google cloud Java client library"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [camel-snake-kebab "0.4.0" :exclusions [org.clojure/clojure]]
                 [com.google.cloud/google-cloud "0.11.1-WithParameters-alpha"
                  :exclusions [io.grpc/grpc-core
                               io.netty/netty-codec-http2
                               com.google.http-client/google-http-client-jackson2
                               com.google.http-client/google-http-client
                               com.google.api.grpc/grpc-google-common-protos
                               com.google.protobuf/protobuf-java-util
                               com.google.protobuf/protobuf-java
                               com.google.api-client/google-api-client]]
                 [com.google.api.grpc/grpc-google-cloud-language-v1 "0.1.5"
                  :exclusions [com.google.auto.value/auto-value
                               io.grpc/grpc-protobuf-lite
                               io.grpc/grpc-core
                               io.netty/netty-codec-http2]]
                 [io.grpc/grpc-core "1.2.0"]
                 [io.netty/netty-codec-http2 "4.1.9.Final"]
                 [com.google.api-client/google-api-client "1.22.0"]
                 [org.reflections/reflections "0.9.11"
                  :exclusions [com.google.guava/guava]]
                 [javax.servlet/servlet-api "2.5"]])
