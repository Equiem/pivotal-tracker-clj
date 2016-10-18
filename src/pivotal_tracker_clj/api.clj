; Generic utility functions for working with APIs.
(ns pivotal-tracker-clj.api
  (:require [environ.core]
            [taoensso.timbre]))

(defn token
  [key name]
  (if-let [token (environ.core/env key)]
    token
    (throw (Exception. (str name " token not found. Please set environment variable for " key)))))

(defn endpoint->url
  "Standardises endpoints as either sequences, relative paths or absolute URLs to an absolute URL."
  [base-url endpoint]
  {:pre [(or (string? endpoint)
             (coll? endpoint))]
   :post [(string? %) (clojure.string/starts-with? % base-url)]}
  (cond (coll? endpoint)
        (endpoint->url base-url (clojure.string/join "/" endpoint))

        (string? endpoint)
        (if-not (clojure.string/starts-with? endpoint base-url)
                (str base-url endpoint)
                endpoint)))
