import os

import bs4 as bs
import concurrent.futures as fut
import time

DOCUMENTS_INPUT_PATH = "html_documents/"
DOCUMENTS_OUTPUT_PATH = "documents/"
INDICIES_OUTPUT_PATH = "indicies/"
DOC_NUM_TSV_MAP = "doc-num-map.tsv"
NUMBER_DOC_MAP = dict()
DOC_NUMBER_MAP = dict()
def write_file(filename, document_number, text):
    with open(DOCUMENTS_OUTPUT_PATH + document_number + ".txt", 'w+') as document:
        document.write(text)
        
def write_index():
    with open(INDICIES_OUTPUT_PATH + DOC_NUM_TSV_MAP, 'w+') as index:
        for key in NUMBER_DOC_MAP:
            index.write(key + "\t" + NUMBER_DOC_MAP[key] + "\n")
            
def write_inverted_index():
    with open("indicies/num-doc-map.tsv", 'w+') as writer:
        for line in open("indicies/doc-num-map.tsv").readlines():
            split_line = line.split("\t")
            writer.write(line.split("\t")[1].strip() + "\t" + line.split("\t")[0].strip() + "\n")

def decompose_tags(tags):
    for tag in tags:
        tag.decompose()
        
def process_file(file, file_number):
    contents = bs.BeautifulSoup(" ".join(open(DOCUMENTS_INPUT_PATH + file).readlines()), 'html.parser')
    math_annots = contents.find_all("math")
    decompose_tags(math_annots)
    text = "".join(token for token in contents.findAll(text=True))
    text = text.replace("↩", "")
    write_file(file, file_number, text)

def parse_html():
    """
    data_dir_path = "data/NTCIR-12_MathIR_Wikipedia_Corpus/MathTagArticles/"
    # iterate through math tag articles
    for file in os.listdir(data_dir_path):
        print("File",file)
        # if compressed zip file
        if(file.endswith(".tar.bz2")):
            # pull extracted directory name
            folder_name = file.split(".")[0] + "/"
            # get directory number
            prefix = int(folder_name[6:-1])
            # extract directory from compressed zip
            os.system("tar jxf " + data_dir_path + file)
            # iterate through each html file in directory
            print("processing folder: ", folder_name)
            for html_file in os.listdir(folder_name):
                #print("processing file " + html_file)
                if html_file.endswith(".html"):
                    ignore_tags = set(["math"])
                    contents = bs.BeautifulSoup(" ".join(open(folder_name + "/" + html_file).readlines()), 'html.parser')
                    # beautiful soup pulls offset number from html file
                    offset = contents.findAll('title')[0].attrs['offset']
                    NUMBER_DOC_MAP[str(prefix) + "-" + str(offset)] = html_file
                    DOC_NUMBER_MAP[html_file] = str(prefix) + "-" + str(offset)
                    if "(" in html_file:
                        html_file = html_file.replace("(", "\(")
                    if ")" in html_file:
                        html_file = html_file.replace(")", "\)")
                    if "\'" in html_file:
                        html_file = html_file.replace("\'", "\\'")
                    # moves html file in directory to html_documents folder with prefix and offset as file name prefix
                    #os.system("mv " + folder_name + html_file + " " + DOCUMENTS_INPUT_PATH + html_file)
            print("removing folder: ", folder_name)
            os.system("rm -rf " + folder_name) 
    write_index()
    write_inverted_index()
    """
    
    for line in open("indicies/num-doc-map.tsv").readlines():
        split_line = line.strip().split("\t")
        DOC_NUMBER_MAP[split_line[0]] = split_line[1]
    
    with fut.ThreadPoolExecutor(max_workers=16) as tpool:
        for file in os.listdir(DOCUMENTS_INPUT_PATH):
            tpool.submit(process_file, file, DOC_NUMBER_MAP[file])
    
        
if __name__ == "__main__":
    parse_html()
