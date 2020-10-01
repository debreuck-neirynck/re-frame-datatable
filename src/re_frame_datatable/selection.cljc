(ns re-frame-datatable.selection
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.paths :refer :all]))

(re-frame/reg-sub
  :re-frame-datatable.core/selected-items
  (fn [[_ db-id data-sub]]
    [(re-frame/subscribe data-sub)
     (re-frame/subscribe [::state db-id])])

  (fn [[items state]]
    (->> items
         (map-indexed vector)
         (filter (fn [[idx _]] (contains? (get-in state [::selection ::selected-indexes]) idx)))
         (map second))))


(re-frame/reg-event-db
  :re-frame-datatable.core/change-row-selection
  [trim-v]
  (fn [db [db-id row-index selected?]]
    (update-in db (vec (concat (state-db-path db-id) [::selection ::selected-indexes]))
               (if selected? conj disj) row-index)))


(re-frame/reg-event-db
  :re-frame-datatable.core/change-table-selection
  [trim-v]
  (fn [db [db-id indexes selected?]]
    (let [selection-indexes-path (vec (concat (state-db-path db-id) [::selection ::selected-indexes]))
          selection (get-in db selection-indexes-path)]
      (assoc-in db selection-indexes-path
                (if selected?
                  (clojure.set/union (set indexes) selection)
                  (clojure.set/difference selection (set indexes)))))))


(re-frame/reg-event-db
  :re-frame-datatable.core/unselect-all-rows
  [trim-v]
  (fn [db [db-id]]
    (assoc-in db (vec (concat (state-db-path db-id) [::selection ::selected-indexes])) #{})))

