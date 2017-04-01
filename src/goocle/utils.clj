(ns goocle.utils)

(defn fixup-dash-number
  "Fixup any instances of -[0-9] to be [0-9] so that numbers aren't separated from their preceding text (or numbers)."
  [s]
  (clojure.string/replace s #"\-([0-9])" "$1"))
