(ns re-frame-datatable.sorting-test
  (:require [midje.sweet :refer :all]
            [re-frame
             [core :as rf]
             [db :refer [app-db]]]
            [re-frame-datatable
             [core :as dt]
             [db :as db]
             [sorting :as s]]))

(facts "about `:set-sort-key`"
       (fact "sets sort key in db state"
             (reset! app-db {}) => empty?
             (rf/dispatch-sync [::s/set-sort-key ..id.. ..key.. nil])
             (db/sorting @app-db ..id..) => (contains {:sort-key ..key..}))

       (fact "sets comp-fn"
             (reset! app-db {}) => empty?
             (rf/dispatch-sync [::s/set-sort-key ..id.. ..key.. ..fn..])
             (db/sorting @app-db ..id..) => (contains {:sort-fn ..fn..}))

       (fact "sets direction to ascending when key changed"
             (reset! app-db {}) => empty?
             (rf/dispatch-sync [::s/set-sort-key ..id.. ..key.. ..fn..])
             (db/sorting @app-db ..id..) => (contains {:sort-comp ::s/sort-asc}))

       (fact "toggles direction when key unchanged"
             (reset! app-db {}) => empty?
             (rf/dispatch-sync [::s/set-sort-key ..id.. ..key.. ..fn..])
             (db/sorting @app-db ..id..) => (contains {:sort-comp ::s/sort-asc})
             (rf/dispatch-sync [::s/set-sort-key ..id.. ..key.. ..fn..])
             (db/sorting @app-db ..id..) => (contains {:sort-comp ::s/sort-desc})))
