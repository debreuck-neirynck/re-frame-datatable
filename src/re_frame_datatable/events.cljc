(ns re-frame-datatable.events
  (:require [re-frame.core :as re-frame :refer [trim-v]]
            [re-frame-datatable.paths :as p]
            [re-frame-datatable.defaults :as d]
            [re-frame-datatable.db :as db]))

(re-frame/reg-event-db
 ;; Called before the component is mounted into the DOM, this sets the configuration
 ;; and default state in the re-frame database.
 ::on-will-mount
 [trim-v]
 (fn [db [db-id data-sub columns-def options]]
   (db/set-configuration db db-id {:sub data-sub
                                   :columns columns-def
                                   :options options})))

(re-frame/reg-event-db
 ;; Updates the db state with given value at given path
 ::change-state-value
 [trim-v]
 (fn [db [db-id state-path new-val]]
   (db/update-state db db-id assoc-in state-path new-val)))
