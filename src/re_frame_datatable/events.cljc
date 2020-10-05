(ns re-frame-datatable.events
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.paths :as p]
            [re-frame-datatable.defaults :as d]))

(re-frame/reg-event-db
 ;; Called before the component is mounted into the DOM, this sets the configuration
 ;; and default state in the re-frame database.
 ::on-will-mount
 [trim-v]
 (fn [db [db-id data-sub columns-def options]]
   (-> db
       (assoc-in (p/columns-def-db-path db-id)
                 columns-def)
       (assoc-in (p/options-db-path db-id)
                 options)
       (assoc-in (p/state-db-path db-id)
                 {::p/pagination  (merge {:re-frame-datatable.core/per-page d/default-per-page
                                          ::p/cur-page 0}
                                         (select-keys (:re-frame-datatable.core/pagination options)
                                                      [:re-frame-datatable.core/per-page
                                                       :re-frame-datatable.core/enabled?]))
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
       (assoc-in (p/columns-def-db-path db-id)
                 columns-def)
       (assoc-in (p/options-db-path db-id)
                 options)

       (assoc-in (conj (p/state-db-path db-id) ::total-items) (count @(re-frame/subscribe data-sub))))))

(re-frame/reg-event-db
 ;; Called before the component is removed from the dom tree.  This will remove the information from
 ;; the re-frame db.
 ::on-will-unmount
 [trim-v]
 (fn [db [db-id]]
   (update-in db p/root-db-path dissoc db-id)))

(re-frame/reg-event-db
 ;; Updates the db state with given value at given path
 ::change-state-value
 [trim-v]
 (fn [db [db-id state-path new-val]]
   (assoc-in db (vec (concat (p/state-db-path db-id) state-path)) new-val)))
