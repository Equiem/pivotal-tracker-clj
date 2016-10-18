(defproject pivotal-tracker-clj "0.1.0-SNAPSHOT"
  :description "Abstracts the Pivotal Tracker API a bit without implementing every endpoint."
  :url "https://github.com/thedavidmeister/pivotal-tracker-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [ [org.clojure/clojure "1.8.0"]
                  [cheshire "5.6.3"]
                  [environ "1.1.0"]
                  [com.taoensso/timbre "4.7.4"]
                  [http-kit "2.2.0"]]
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.17.0"]]}})
