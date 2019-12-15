import os

import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import scipy.cluster.vq as clustering
import scipy.sparse as smatrix
from sklearn.cluster import KMeans, MiniBatchKMeans

vec_model_file_path = "indicies/vecSpaceModel.tsv"
sparse_matrix_file_path = "indicies/cscmatrix.npz"


def read_sparse_matrix():
    with open(vec_model_file_path) as vs:
        parsedDocs = dict()
        documents = []
        for line in vs.readlines():
            line = line.strip()
            entries = line.split("\t")
            key = entries[0]
            documents.append(key)
            values = []
            for index in range(1, len(entries)):
                column = entries[index].split(",")
                if not column[0] is '':
                    values.append((int(column[0]), float(column[1])))
            parsedDocs[key] = values
        return parsedDocs, documents


if __name__ == "__main__":
    total_rows = sum(1 for line in open("indicies/doc-index.tsv", 'rb'))
    row_headers = []
    for line in open("indicies/doc-index.tsv"):
        row_headers.append(line.split("\t")[0]) 
    row_headers.sort()
    print("Reading in matrix")
    
    matrix, column_headers = read_sparse_matrix()
    print("Getting number of columns")
    total_columns = len(column_headers)
    print("getting sparse matrix built")
    if not os.path.exists(sparse_matrix_file_path):
        # gets the number of terms and the terms themselves before sorting terms in alphabetical order
        print("Creating matrix with shape(", str(total_rows), ", ", str(total_columns), ")")
        dok_matrix = smatrix.dok_matrix((total_rows, total_columns), dtype=np.float32)
        for column, key in enumerate(matrix):
            for document in matrix[key]:
                dok_matrix[[document[0]], [column]] = document[1]
        smatrix.save_npz(sparse_matrix_file_path, dok_matrix.tocsc())
    else:
        print("Reading in sparse matrix")
        sparse_matrix = smatrix.load_npz(sparse_matrix_file_path)
        print("Creating dataframe for pandas")
        sparse_matrix = pd.DataFrame(sparse_matrix.todense()).T
        type(sparse_matrix)
        sparse_matrix.columns = row_headers
        print(sparse_matrix.head())
        # read sparse matrix -> dataframe -> add headers 
        print("Running kmeans clustering")
        kmeans_result = MiniBatchKMeans(n_clusters=10,compute_labels=True, init='k-means++', batch_size=100, verbose=True, random_state=0).fit(sparse_matrix)
        
        centroids = kmeans_result.cluster_centers_
        print(centroids)
    print("done")
    
    
    
    
"""
data_holder = {}
for index, row_header in enumerate(row_headers):
# goes through each document - row header
values = matrix[row_header]
document_values = []
current_values_index = 0
for column_idx in range(total_columns):
if current_values_index < len(values) and column_idx == values[current_values_index][0]:
document_values.append(values[current_values_index][1])
current_values_index+=1
else:
document_values.append(0)
data_holder[row_header] = document_values
data_frame = pd.DataFrame(data_holder)
data_frame.index = row_headers

data_frame.head()










print("Creating matrix with shape(", str(total_rows), ", ", str(total_columns), ")")
dok_matrix = smatrix.dok_matrix((total_rows, total_columns), dtype=np.float32)
for column, key in enumerate(matrix):
print("Processing document ", column)
for document in matrix[key]:
dok_matrix[[document[0]], [column]] = document[1]
smatrix.save_npz(sparse_matrix_file_path, dok_matrix.tocsc())
else:

print("Reading in sparse matrix")
sparse_matrix = smatrix.load_npz(sparse_matrix_file_path)
for k in range(10, 50, 50):
print("Clustering k =", k)
labeler = KMeans(k)
labeler.fit(sparse_matrix)
centers = labeler.cluster_centers_

print("done")
"""