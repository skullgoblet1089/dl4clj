(ns dl4clj.indarray
  (:import [org.nd4j.linalg.factory Nd4j])
  (:require [clojure.core.match :refer [match]]
            [dl4clj.utils :refer [obj-or-code?]]))

(defn vec->indarray
  [data]
  (Nd4j/create (double-array data)))

(defn matrix->indarray
  [matrix]
  (as-> (for [each matrix]
          (double-array each))
      data
    (into [] data)
    (into-array data)
    (Nd4j/create data)))

(defn vec-or-matrix->indarray
  [data & {:keys [as-code?]
           :or {as-code? true}}]
  (match [data]
         [(_ :guard vector?)]
         (let [code (if (vector? (first data))
                      `(matrix->indarray ~data)
                      `(vec->indarray ~data))]
           (obj-or-code? as-code? code))
         :else
         data))

(defn indarray-of-zeros
  [& {:keys [rows columns as-code?]
      :or {rows 1
           columns 1
           as-code? true}}]
  (let [code `(Nd4j/zeros (int ~rows) (int ~columns))]
    (obj-or-code? as-code? code)))

(defn indarray-of-ones
  [& {:keys [rows columns as-code?]
      :or {rows 1
           columns 1
           as-code? true}}]
  (let [code `(Nd4j/ones (int ~rows) (int ~columns))]
    (obj-or-code? as-code? code)))

(defn indarray-of-rand
  [& {:keys [rows columns as-code?]
      :or {rows 1
           columns 1
           as-code? false}}]
  (let [code (Nd4j/rand (int rows) (int columns))]
    (obj-or-code? as-code? code)))
