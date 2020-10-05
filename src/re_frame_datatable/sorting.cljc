(ns re-frame-datatable.sorting
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.paths :as p]))

(re-frame/reg-event-db
 ::set-sort-key
 [trim-v]
 (fn [db [db-id sort-key comp-fn]]
   (let [comp-fn (or comp-fn <)
         cur-sort-key (get-in db (p/sort-key-db-path db-id))
         cur-sort-comp (get-in db (p/sort-comp-order-db-path db-id) ::sort-asc)]
     (if (= cur-sort-key sort-key)
       (assoc-in db (p/sort-comp-order-db-path db-id)
                 (get {::sort-asc  ::sort-desc
                       ::sort-desc ::sort-asc} cur-sort-comp))
       (-> db
           (assoc-in (p/sort-key-db-path db-id) sort-key)
           (assoc-in (p/sort-comp-fn-db-path db-id) comp-fn)
           (assoc-in (p/sort-comp-order-db-path db-id) cur-sort-comp))))))
