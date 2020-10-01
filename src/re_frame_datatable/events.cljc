(ns re-frame-datatable.events
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.paths :refer :all]
            [re-frame-datatable.defaults :refer :all]))

(re-frame/reg-event-db
 ;; Called before the component is mounted into the DOM, this sets the configuration
 ;; and default state in the re-frame database.
 ::on-will-mount
 [trim-v]
 (fn [db [db-id data-sub columns-def options]]
   (-> db
       (assoc-in (columns-def-db-path db-id)
                 columns-def)
       (assoc-in (options-db-path db-id)
                 options)
       (assoc-in (state-db-path db-id)
                 {::pagination  (merge {::per-page default-per-page
                                        ::cur-page 0}
                                       (select-keys (::pagination options) [::per-page ::enabled?]))
                  ::total-items (count @(re-frame/subscribe data-sub))
                  ::selection   (merge {::selected-indexes (if (get-in options [::selection ::enabled?])
                                                             (or (get-in options [::selection ::selected-indexes]) #{})
                                                             #{})}
                                       (select-keys (::selection options) [::enabled?]))}))))

(re-frame/reg-event-db
 ::on-did-update
 [trim-v]
 (fn [db [db-id data-sub columns-def options]]
   (-> db
       (assoc-in (columns-def-db-path db-id)
                 columns-def)
       (assoc-in (options-db-path db-id)
                 options)

       (assoc-in (conj (state-db-path db-id) ::total-items) (count @(re-frame/subscribe data-sub))))))

(re-frame/reg-event-db
 ;; Called before the component is removed from the dom tree.  This will remove the information from
 ;; the re-frame db.
 ::on-will-unmount
 [trim-v]
 (fn [db [db-id]]
   (update-in db root-db-path dissoc db-id)))

(re-frame/reg-event-db
 ;; Updates the db state with given value at given path
 ::change-state-value
 [trim-v]
 (fn [db [db-id state-path new-val]]
   (assoc-in db (vec (concat (state-db-path db-id) state-path)) new-val)))
