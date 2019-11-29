import os

DATA_PATH = "data/NTCIR-12_MathIR_Wikipedia_Corpus/MathTagArticles"
EXTRACT_PATH = "temp/"
DOCUMENTS_PATH = "documents/"




def parse_compressed_files():
    """
    for sub_dir, dir, files in os.walk(DATA_PATH):
        for file in files:
            if file.endswith('.tar.bz2'):
                os.system("tar jxf " + DATA_PATH + "/" +file + " --directory " + EXTRACT_PATH)
    """
    counter = 0
    for sub_dir, dir, files in os.walk(EXTRACT_PATH):
        for file in os.listdir(sub_dir):
            if file.endswith('.html'):
                print("cp " + sub_dir + "/" + file + " " + DOCUMENTS_PATH)
                counter += 1
    print("Counter = ", counter)



if __name__ == "__main__":
    parse_compressed_files()
