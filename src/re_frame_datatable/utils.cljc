(ns re-frame-datatable.utils
  (:require [clojure.string :refer [lower-case]]))

(defn case-insensitive-filtering-fn
  "Table row filtering function that filters case insensitive.
  Searching for `john` would also return `John`.

  The function accepts a search string and returns a single-argument function that takes in an input.
  Input should be coerceable to a string."
  [search-string]
  (fn [input]
    (re-find
      (re-pattern (lower-case search-string))
      (lower-case (str input)))))

(defn case-sensitive-filtering-fn
  "Table row filtering function that filters case sensitive.
  Searching for `john` would NOT return `John`.

  The function accepts a search string and returns a single-argument function that takes in an input.
  Input should be coerceable to a string."
  [search-string]
  (fn [input]
    (re-find (re-pattern search-string) (str input))))