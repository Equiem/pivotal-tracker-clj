(ns pivotal-tracker-clj.core)

(def base-url "https://www.pivotaltracker.com/services/v5/")

(defn endpoint->url
  [endpoint]
  (str base-url endpoint))

(defn get-all-pages!
  ([key endpoint] (get-all-pages! url {}))
  ([key endpoint options]
   (let [ url (endpoint->url endpoint)
          options             (with-auth-headers options)
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
