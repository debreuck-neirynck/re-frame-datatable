(ns re-frame-datatable.selection
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.paths :as p]
            [re-frame-datatable.db :as db]
            [re-frame-datatable.subs :as subs]))

(re-frame/reg-sub
  :re-frame-datatable.core/selected-items
  (fn [[_ db-id data-sub] _]
    [(re-frame/subscribe data-sub)
     (re-frame/subscribe [::subs/state db-id])])

  (fn [[items state] _]
    (->> items
         (map-indexed vector)
         (filter (fn [[idx _]] (contains? (get-in state [:selection :selected-indexes]) idx)))
         (map second))))

(re-frame/reg-event-db
  :re-frame-datatable.core/change-row-selection
  [trim-v]
  (fn [db [db-id row-index selected?]]
    (db/update-state db db-id 
                     update-in [:selection :selected-indexes]
                     (if selected? (comp set conj) disj) row-index)))


(re-frame/reg-event-db
  :re-frame-datatable.core/change-table-selection
  [trim-v]
  (fn [db [db-id indexes selected?]]
    (db/update-selection db db-id
                         #(if selected?
                            (clojure.set/union (set indexes) %)
                            (clojure.set/difference % (set indexes))))))


(re-frame/reg-event-db
  :re-frame-datatable.core/unselect-all-rows
  [trim-v]
  (fn [db [db-id]]
    (db/set-selection db db-id #{})))

