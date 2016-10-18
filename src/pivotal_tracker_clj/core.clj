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

(defn response-ok?
  [response]
  (= (and (-> response :status (= 200))
          (-> response :headers :status (= "200 OK")))))

(defn response->data
  [response]
  (-> response
      :body
      (cheshire.core/parse-string true)))

(defn scrub-response
  [response]
  (-> response
      (assoc-in [:opts :headers "X-TrackerToken"] "***HIDDEN***")))

(defn api!
 ([token endpoint] (api! token endpoint {}))
 ([token endpoint options & params]
  (taoensso.timbre/debug "Requesting Pivotal Tracker endpoint" endpoint)
  (let [request (org.httpkit.client/request (-> (apply hash-map params)
                                                (with-url (endpoint->url endpoint))
                                                (with-options options)
                                                (with-auth token)))
        response @request]
    (if (response-ok? response)
        (response->data response)
        (do
          (taoensso.timbre/error (scrub-response response))
          (throw (Exception. "Bad response from Pivotal Tracker")))))))

(defn api-all-pages!
  ([token endpoint] (api-all-pages! token endpoint {}))
  ([token endpoint options & params]
   ()))





; This should be a protocol...
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

; (defn get-page!
;   ([url] (get-page! url {}))
;   ([url options]
;    (let [options-a (with-auth options)]
;     (http/get url options-a))))

; (defn api-error!
;   [headers]
;   (pprint/pprint headers)
;   (throw (Exception. "Failed response from Pivotal Tracker API. See headers above for debug info.")))

; Options handling.

; (def page-limit 500)
; (defn pager-params
;   "Query parameters for page number X. Pages are 1 indexed."
;   [page-number]
;   {:limit page-limit
;     ; We have to dec the page number. E.g. "Page 1" is offset 0, etc.
;     :offset (* page-limit (dec page-number))})
;
; (defn with-pager-params
;   [options page-number]
;   (with-options (pager-params page-number) options))
;
; ; Response dereffing.
;
; (defn response-body
;   [promise]
;   (walk/keywordize-keys (json/read-str (:body @promise))))
;
; (defn response-header
;   [promise]
;   (:headers @promise))
;
; (defn get-all-pages!
;   ([token endpoint] (get-all-pages! url {}))
;   ([token endpoint options]
;    (let [ url (endpoint->url endpoint)
;           params {}
;           options             (-> {} (with-auth-headers key) (with-options options))
;           first-request       (get-page! url (with-pager-params options 1))
;           first-response      (response-body first-request)
;           first-headers       (response-header first-request)
;           first-error         (if-not (= "200 OK" (:status first-headers))
;                                       (api-error! first-headers))
;           ; Ultimately the page size is what PT will return, not what we ask for.
;           page-size           (:x-tracker-pagination-returned first-headers)
;           results-count       (:x-tracker-pagination-total first-headers)
;           number-of-pages     (number-of-pages (str->int page-size) (str->int results-count))
;           other-page-numbers  (range 2 (inc number-of-pages))
;           other-requests      (if (< 1 number-of-pages)
;                                   (vec (map #(get-page! url (with-pager-params options %))
;                                             other-page-numbers)))
;           other-responses     (vec (map response-body other-requests))
;           other-headers       (vec (map response-header other-requests))
;           all-responses       (into first-response other-responses)
;           all-headers         (into [first-headers] other-headers)
;           errors              (remove #(= "200 OK" (:status %))
;                                       all-headers)]
;       (if (not-empty errors) (api-error! errors))
;       (vec (flatten all-responses)))))
