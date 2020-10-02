(ns re-frame-datatable.events-test
  (:require [re-frame-datatable
             [events :as e]
             [subs]
             [paths :as p]]
            [midje.sweet :refer :all]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]))

(facts "about `on-will-mount`"
       (fact "stores config in db"
             (rf/reg-sub ::test-sub (constantly []))
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::e/on-will-mount ::test-id [::test-sub] ..cols.. ..opts..])
             (get-in @app-db (p/state-db-path ::test-id)) => map?
             (get-in @app-db (p/columns-def-db-path ::test-id)) => ..cols..
             (get-in @app-db (p/options-db-path ::test-id)) => ..opts..))
