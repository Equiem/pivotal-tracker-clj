(ns pivotal-tracker-clj.core-test
  (:require [clojure.test :refer :all]
            [pivotal-tracker-clj.core :refer :all]))

(deftest api-calls

  (prn (pivotal-tracker-clj.core/api! (pivotal-tracker-clj.core/token)
                                      ["me"])))
