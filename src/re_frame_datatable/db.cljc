(ns re-frame-datatable.db
  "Functions for manipulating the re-frame database")

(def root ::re-frame-datatable)

(defn configuration
  "Gets the configuration of the given table"
  [db id]
  (get-in db [root id ::config]))

(defn set-configuration [db id c]
  (assoc-in db [root id ::config] c))

(defn state
  "Gets the state of the given table"
  [db id]
  (get-in db [root id ::state]))

(defn set-state [db id s]
  (assoc-in db [root id ::state] s))

(defn update-state
  "Applies `f` with arguments to the state for given table"
  [db id f & args]
  (apply update-in db [root id ::state] f args))

(defn- update-config [db id f & args]
  (apply update-in db [root id ::config] f args))

(defn options [db id]
  (:options (configuration db id)))

(defn set-options [db id o]
  (update-config db id assoc :options o))

(defn set-sorting
  "Sets current sorting configuration in state"
  [db id s]
  (update-state db id assoc :sorting s))

(defn sorting [db id]
  (:sorting (state db id)))

(defn set-pagination
  "Sets current pagination configuration in state"
  [db id s]
  (update-state db id assoc :pagination s))

(defn pagination [db id]
  (:pagination (state db id)))
