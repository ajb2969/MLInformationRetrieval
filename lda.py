import scipy.sparse as smatrix
vec_model_file_path = "indicies/vecSpaceModel.tsv";


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
    matrix = read_sparse_matrix()
    
    print("done")