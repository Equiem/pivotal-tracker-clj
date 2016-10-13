# pivotal-tracker-clj

A Clojure library designed to wrap Pivotal Tracker's API.

This library doesn't do anything for any specific endpoints.

We figure that you will appreciate help with:
- Authentication
- Pagination of responses (handles parallelisation)
- Error handling (such as hitting an API rate limit)

We figure that you don't need help with:
- Building a string that matches an endpoint documented at https://www.pivotaltracker.com/help/api/rest/v5
- Making dates and times

## Dependencies

Uses `http-kit` and `clj-time` under the hood so options should be treated like `http-kit` parameters and dates should be `clj-time` dates.

Optionally all the date handling can be done outside this library, just pass in strings as per the Pivotal Tracker API documentation.

## Authentication

This library requires that one environment variable `PT_API_TOKEN` be set in order to authenticate with the Pivotal Tracker API.

## Usage

The main function is `get-all-pages!` and takes two arguments, an endpoint string and options map.

The options map will be used for the parameters documented for the given endpoint.

Example, get all stories created after 2016-01-01:

```clojure
(require '[pivotal-tracker-clj.core :as pt])
(require '[clj-time.core :as t])

(def endpoint (str "/projects/" project-id "/stories"))
(def options {:created_after (t/date-time 2016 1 1)})
(pt/get-all-pages! endpoint options)
```

## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
