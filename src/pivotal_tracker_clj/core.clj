(ns pivotal-tracker-clj.core)

(def base-url "https://www.pivotaltracker.com/services/v5/")

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

(defn endpoint->url
  [endpoint]
  (str base-url endpoint))

(def auth-headers {"X-TrackerToken" api-token})
(defn with-auth-headers
  [params key]
  (update-in options [:headers] merge auth-headers))

(def with-options
  [params options]
  (update-in options [:query-params] merge options))

(defn get-page!
  ([url] (get-page! url {}))
  ([url options]
   (let [options-a (with-auth-headers options)]
    (http/get url options-a))))

(defn api-error!
  [headers]
  (pprint/pprint headers)
  (throw (Exception. "Failed response from Pivotal Tracker API. See headers above for debug info.")))

; Options handling.

(def page-limit 500)
(defn pager-params
  "Query parameters for page number X. Pages are 1 indexed."
  [page-number]
  {:limit page-limit
    ; We have to dec the page number. E.g. "Page 1" is offset 0, etc.
    :offset (* page-limit (dec page-number))})

(defn with-pager-params
  [options page-number]
  (with-options (pager-params page-number) options))

; Response dereffing.

(defn response-body
  [promise]
  (walk/keywordize-keys (json/read-str (:body @promise))))

(defn response-header
  [promise]
  (:headers @promise)))

(defn get-all-pages!
  ([key endpoint] (get-all-pages! url {}))
  ([key endpoint options]
   (let [ url (endpoint->url endpoint)
          params {}
          options             (-> {} (with-auth-headers key) (with-options options))
          first-request       (get-page! url (with-pager-params options 1))
          first-response      (response-body first-request)
          first-headers       (response-header first-request)
          first-error         (if-not (= "200 OK" (:status first-headers))
                                      (api-error! first-headers))
          ; Ultimately the page size is what PT will return, not what we ask for.
          page-size           (:x-tracker-pagination-returned first-headers)
          results-count       (:x-tracker-pagination-total first-headers)
          number-of-pages     (number-of-pages (str->int page-size) (str->int results-count))
          other-page-numbers  (range 2 (inc number-of-pages))
          other-requests      (if (< 1 number-of-pages)
                                  (vec (map #(get-page! url (with-pager-params options %))
                                            other-page-numbers)))
          other-responses     (vec (map response-body other-requests))
          other-headers       (vec (map response-header other-requests))
          all-responses       (into first-response other-responses)
          all-headers         (into [first-headers] other-headers)
          errors              (remove #(= "200 OK" (:status %))
                                      all-headers)]
      (if (not-empty errors) (api-error! errors))
      (vec (flatten all-responses)))))
