(ns pivotal-tracker-clj.core
  (:require [pivotal-tracker-clj.api]
            [cheshire.core]
            [org.httpkit.client]))

(defn token [] (pivotal-tracker-clj.api/token :pivotal-tracker-token "Pivotal Tracker"))

(defn endpoint->url [endpoint] (pivotal-tracker-clj.api/endpoint->url "https://www.pivotaltracker.com/services/v5/" endpoint))

(defn with-options
  [params options]
  (cond (-> params :method (= :post))
        (assoc params :body (cheshire.core/generate-string options))

        :else
        (update-in params [:query-params] merge options)))

(defn with-auth
  [params token]
  (update-in params [:headers] merge {"X-TrackerToken" token}))

(defn with-url
  [params url]
  (assoc params :url url))

(defn with-page-number
  [params page-number]
  (let [page-size 500]
    (with-options params {:limit page-size
                          :offset (* page-size (dec page-number))})))

(defn response-ok?
  [response]
  (= (and (-> response :status (= 200))
          (-> response :headers :status (= "200 OK")))))

(defn response->data
  [response]
  (-> response
      :body
      (cheshire.core/parse-string true)))

(defn scrub-params
  [params]
  (-> params
      (assoc-in [:headers "X-TrackerToken"] "***HIDDEN**")))

(defn scrub-response
  [response]
  (-> response
      (update-in [:opts] scrub-params)))

(defn str->int [v] (if  (string? v)
                        (Integer. (re-find #"\d+" v))
                        (int v)))

(defn number-of-pages
  [limit pagination-total]
  {:pre [(integer? limit) (integer? pagination-total)]}
  (->
    (/ pagination-total limit)
    Math/ceil
    int))

(defn get-page!
  [params]
  (taoensso.timbre/debug "Requesting Pivotal Tracker endpoint" (scrub-params params))
  (let [request (org.httpkit.client/request params)
        response @request]
    (if (response-ok? response)
        response
        (do
          (taoensso.timbre/error (scrub-response response))
          (throw (Exception. "Bad response from Pivotal Tracker"))))))

(defn api!
 ([token endpoint] (api! token endpoint {}))
 ([token endpoint options & params]
  (let [base-params (-> (apply hash-map params)
                        (with-url (endpoint->url endpoint))
                        (with-options options)
                        (with-auth token))
        first-response (get-page! (with-page-number base-params 1))
        page-size  (-> first-response :headers :x-tracker-pagination-returned)
        results-count (-> first-response :headers :x-tracker-pagination-total)
        number-of-pages     (number-of-pages (str->int page-size) (str->int results-count))
        other-page-numbers  (range 2 (inc number-of-pages))
        other-responses      (if  (< 1 number-of-pages)
                                  (vec (pmap #(get-page! (with-page-number base-params %))
                                              other-page-numbers)))
        all-data (into (response->data first-response) (flatten (map response->data other-responses)))]
    all-data)))
