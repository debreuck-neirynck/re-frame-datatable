(ns re-frame-datatable.events-test
  (:require [re-frame-datatable
             [events :as e]
             [subs]
             [paths :as p]
             [db :as db]]
            [midje.sweet :refer :all]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]))

(facts "about `on-will-mount`"
       (fact "stores config in db"
             (reset! app-db {}) => truthy
             (rf/dispatch-sync [::e/on-will-mount ::test-id [::test-sub] ..cols.. ..opts..])
             (db/configuration @app-db ::test-id) => {:sub [::test-sub]
                                                      :columns ..cols..
                                                      :options ..opts..}))

(facts "about `change-state-value`"
       (fact "sets given value in state"
             (reset! app-db {}) => map?
             (rf/dispatch-sync [::e/change-state-value ::test-id [:some :path] ..value..])
             (db/state @app-db ::test-id) => {:some {:path ..value..}}))
