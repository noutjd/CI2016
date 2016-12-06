import tensorflow as tf
import numpy as np

TRAIN_SET = "alpine-2.csv"
TEST_SET = "aalborg.csv"

train_set = tf.contrib.learn.datasets.base.load_csv_with_header(filename = TRAIN_SET, target_dtype = np.float32, features_dtype = np.float32, target_column = [1])
test_set = tf.contrib.learn.datasets.base.load_csv_with_header(filename = TEST_SET, target_dtype = np.float32, features_dtype = np.float32, target_column = [1])

#feature_columns = [tf.contrib.layers.real_valued_column("", dimension=4)]