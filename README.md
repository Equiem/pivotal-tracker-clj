# pivotal-tracker-clj

A Clojure library designed to wrap Pivotal Tracker's API.

This library doesn't do anything for any specific endpoints.

We figure that you will appreciate help with:
- Authentication
- Handling request vs. API parameters
- Pagination and parallelisation of API calls
- Error handling (such as hitting an API rate limit)
- Logging (and scrubbing sensitive authentication details from the logs)

We figure that you don't need help with:
- Reading documentation on endpoints and parameters https://www.pivotaltracker.com/help/api/rest/v5
- Making dates and times (Highly recommend the `clj-time` library for this)

## Usage

Leingingen:

`[pivotal-tracker-clj "0.1.0"]`

Namespace:

`(require '[pivotal-tracker-clj.core :as pt])`

The main function is `api!` and takes two required arguments, one optional argument and unlimited named parameters.

### API token

The first argument (to `api!` but not `api!!`) is an API token for Pivotal tracker.

There is a utility function `pivotal-tracker-clj.core/token` that will attempt to extract the token from the environment variable `PIVOTAL_TRACKER_TOKEN`.

There is also a convenience function `api!!` that passes the result of `pivotal-tracker-clj.core/token` into `api!`.

If you're happy to rely on the environment variable for all your API calls (pretty common) then you can just use this instead.

For example:

```clojure
(pivotal-tracker-clj.core/api! (pivotal-tracker-clj.core/token) "me")
```

is identical to:

```clojure
(pivotal-tracker-clj.core/api!! "me")
```

### Endpoint

The endpoint can be provided as either:

- An absolute URL e.g. `https://www.pivotaltracker.com/help/api/rest/v5/projects/12345/stories`
- A relative path e.g. `projects/12345/stories`
- A vector of path components e.g. `["projects" 12345 "stories"]`

### Options

A hash map of options to be passed to the Pivotal Tracker API as per the documentation.

The options map will be treated differently depending on the HTTP method used, as per Pivotal Tracker's requirements.

For example, GET requests must use query parameters whereas POST requests are expected to provide JSON in the body.

No validation is attempted to ensure that the options passed are sensible for the given endpoint - please reference the documentation for this.

The options map is an optional argument to `api!`.

Example, get all stories created after 2016-01-01:

```clojure
(pivotal-tracker-clj.core/api!  (pivotal-tracker-clj.core/token)
                                ["projects" 123456 "stories"]
                                {:created_after "2016-01-01T00:00:00Z"})
```

### Params

Additional parameters to `http-kit` can be passed in as named parameters.

Notably, this allows for control over the HTTP method used.

Example, create a new story named "test post":

```clojure
(pivotal-tracker-clj.core/api!  (pivotal-tracker-clj.core/token)
                                ["projects" 123456 "stories"]
                                {:name "test post"}
                                :method :post))
```

## License

Copyright © 2016 David Meister

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
