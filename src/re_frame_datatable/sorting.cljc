(ns re-frame-datatable.sorting
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.db :as db]))

(re-frame/reg-event-db
 ::set-sort-key
 [trim-v]
 (fn [db [db-id sort-key comp-fn]]
   (let [curr (db/sorting db db-id)]
     (db/set-sorting db db-id {:sort-key sort-key
                               :sort-fn comp-fn
                               ;; Toggle sorting direction
                               :sort-comp (if (= sort-key (:sort-key curr))
                                            (get {::sort-asc  ::sort-desc
                                                  ::sort-desc ::sort-asc} (:sort-comp curr))
                                            ::sort-asc)}))))
