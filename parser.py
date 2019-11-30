import os

import bs4 as bs
import concurrent.futures as fut
import progressbar
import time

DOCUMENTS_INPUT_PATH = "html_documents/"
DOCUMENTS_OUTPUT_PATH = "documents/"

def write_file(document_number, text):
    with open(DOCUMENTS_OUTPUT_PATH + document_number + ".txt", 'w+') as document:
        document.write(text)
        
def process_file(file):
    ignore_tags = set(["math"])
    contents = bs.BeautifulSoup(" ".join(open(DOCUMENTS_INPUT_PATH + file).readlines()), 'html.parser')
    document_number = contents.findAll('title')[0].attrs['offset']
    document_text = filter(lambda e: e not in ignore_tags, contents.findAll(text=True))
    write_file(document_number, " ".join(token.strip() for token in document_text))

def parse_html():
    
    with fut.ThreadPoolExecutor(max_workers=16) as tpool:
        for file in progressbar.progressbar(os.listdir(DOCUMENTS_INPUT_PATH)):
            tpool.submit(process_file, file)
        while(tpool._work_queue.qsize() > 0):
            print("Jobs remaining", tpool._work_queue.qsize())
            time.sleep(5)
if __name__ == "__main__":
    parse_html()
