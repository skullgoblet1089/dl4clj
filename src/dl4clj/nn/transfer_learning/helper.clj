(ns ^{:doc "Often times transfer learning models have frozen layers where parameters are held constant during training For ease of training and quick turn around times, the dataset to be trained on can be featurized and saved to disk.

 Featurizing in this case refers to conducting a forward pass on the network and saving the activations from the output of the frozen layers.

 During training the forward pass and the backward pass through the frozen layers can be skipped entirely and the featurized dataset can be fit with the smaller unfrozen part of the computation graph which allows for quicker iterations.

 The class internally traverses the computation graph/MLN and builds an instance of the computation graph/MLN that is equivalent to the unfrozen subset.

 Currently, computation graphs are not supported by dl4clj

 see: https://deeplearning4j.org/doc/org/deeplearning4j/nn/transferlearning/TransferLearningHelper.html" }
    dl4clj.nn.transfer-learning.helper
  (:import [org.deeplearning4j.nn.transferlearning TransferLearningHelper]
           [org.nd4j.linalg.api.ndarray INDArray])
  (:require [dl4clj.helpers :refer [reset-iterator!]]
            [dl4clj.utils :refer [array-of]]
            [nd4clj.linalg.factory.nd4j :refer [vec-or-matrix->indarray]]
            [clojure.core.match :refer [match]]))

(defn new-helper
  "creates a new instance of TransferLearningHelper with the supplied opts

  :mln (multi-layer-network) a model with multiple layers
   - see: dl4clj.nn.conf.builders.multi-layer-builders

  :frozen-til (int), indicates the index of the layer and below to freeze

  if :frozen-til is supplied, Will modify the given MLN (in place!
  to freeze layers (hold params constant during training) specified and below
  otherwise expects a mln where some layers are already frozen"
  [& {:keys [mln frozen-til]
      :as opts}]
  (if frozen-til
    (TransferLearningHelper. mln frozen-til)
    (TransferLearningHelper. mln)))

(defn featurize
  "During training frozen vertices/layers can be treated as featurizing the input
  The forward pass through these frozen layer/vertices can be done in advance
  and the dataset saved to disk to iterate quickly on the smaller unfrozen
  part of the model Currently does not support datasets with feature masks

  :helper (TransferLearningHelper), created by new-helper

  :data-set (dataset or multi-dataset), a dataset
   - can be a single or multi dataset
   - see: nd4clj.linalg.dataset.data-set (under construction)

  warning, this can crash if the dataset is too large"
  [& {:keys [helper data-set]}]
  (.featurize helper data-set))

(defn fit-featurized!
  "Fit from a featurized dataset

  :helper (TransferLearningHelper), created by new-helper

  :data-set (dataset or multi-dataset), a dataset
   - can be a single or multi dataset
   - see: nd4clj.linalg.dataset.data-set (under construction)

  :iter (dataset-iterator or multi-dataset-iterator) a ds iterator
   - can be built based on a single or multi dataset
   - see: dl4clj.datasets.iterator.iterators (double check this through testing)

  returns the helper"
  [& {:keys [helper data-set iter]
   :as opts}]
  (match [opts]
         [{:helper _ :data-set _}] (doto helper (.fitFeaturized data-set))
         [{:helper _ :iter _}] (doto helper (.fitFeaturized (reset-iterator! iter)))
         :else
         (assert false "you must supply either a data-set or a dat-set iterator")))

(defn output-from-featurized
  "Use to get the output from a featurized input

  :helper (TransferLearningHelper), created by new-helper

  :featurized-input (INDArray or vec), featurized data

  :array-of-featurized-input (coll of INDArrays), multiple featurized inputs"
  [& {:keys [helper featurized-input array-of-featurized-input]
      :as opts}]
  (if array-of-featurized-input
    (.outputFromFeaturized helper (array-of :data array-of-featurized-input
                                            :java-type INDArray))
    (.outputFromFeaturized helper (vec-or-matrix->indarray featurized-input))))

(defn unfrozen-mln
  "Returns the unfrozen layers of the MultiLayerNetwork as a multilayernetwork
   Note that with each call to featurizedFit the parameters to the original MLN are also updated

  need to test if this returns the mutated og network with all layers or only the frozen layers
   - if its just the previously frozen layers, will need to merge back into og model
     - og model may have been mutated"
  [helper]
  (.unfrozenMLN helper))
