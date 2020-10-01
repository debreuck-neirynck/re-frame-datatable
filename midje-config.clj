(when-not (running-in-repl?)
  (change-defaults :emitter 'midje-junit-formatter.core
                   :print-level :print-facts))
