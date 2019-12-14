import scipy.sparse as smatrix
import numpy as np
import os
vec_model_file_path = "indicies/vecSpaceModel.tsv"
sparse_matrix_file_path = "indicies/cscmatrix.npz"


def read_sparse_matrix():
    with open(vec_model_file_path) as vs:
        parsedDocs = dict()
        for line in vs.readlines():
            line = line.strip()
            entries = line.split("\t");
            key = entries[0];
            values = []
            for index in range(1, len(entries)) :
                column = entries[index].split(",");
                if not column[0] is '':
                    values.append((int(column[0]), float(column[1])));
            parsedDocs[key] = values
        return parsedDocs


if __name__ == "__main__":
    if not os.exists(sparse_matrix_file_path) :
        print("Parsing total rows")
        total_rows = sum(1 for line in open("indicies/doc-index.tsv", 'rb'))
        print("Reading in matrix")
        matrix = read_sparse_matrix()
        print("Getting number of columns")
        total_columns = len(matrix.keys())
        print("getting sparse matrix built")
        
        print("Creating matrix with shape(", str(total_rows), ", ", str(total_columns), ")")
        dok_matrix = smatrix.dok_matrix((total_rows, total_columns), dtype=np.float32)    
        for column, key in enumerate(matrix):
            print("Processing document ", column)
            for document in matrix[key]:
                dok_matrix[[document[0]], [column]] = document[1]
        smatrix.save_npz(sparse_matrix_file_path, dok_matrix.tocsc())
    else:
        sparse_matrix = smatrix.load_npz(sparse_matrix_file_path)
        
    print("done")