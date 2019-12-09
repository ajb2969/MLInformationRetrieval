package retrieval;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.tomcat.jni.Time;

class Scheduler {
    private ExecutorService threadpool;
    private static Set<String> documents;
    private static ConcurrentHashMap<String, List<LDA.Index>> vectorSpace;
    private List<String> docTokens;
    private static CountDownLatch counter;
    private static BufferedWriter writer;

    Scheduler(Set<String> documents, List<String> docTokens, File file) throws IOException {
        this.threadpool = new ThreadPoolExecutor(32, 64, Time.APR_MSEC_PER_USEC, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>());
        Scheduler.documents = documents;
        this.docTokens = docTokens;
        Scheduler.counter = new CountDownLatch(documents.size());
        vectorSpace = new ConcurrentHashMap<>();
        Scheduler.writer = new BufferedWriter(new FileWriter(file));
    }

    ConcurrentHashMap<String, List<LDA.Index>> executeParse() throws InterruptedException {
        //Processed documents in parallel and stores each row with the document -> a row in CSR format
        AtomicInteger counter = new AtomicInteger(0);
        documents.parallelStream().forEach(document -> {
            this.threadpool.submit(new DocumentParser(document, this.docTokens, counter.addAndGet(1)));
        }); 
        Scheduler.counter.await();

        try {
            System.out.println("Writing matrix to file");
            for(String document: Scheduler.vectorSpace.keySet()) {
                List<LDA.Index> tfidfValues = Scheduler.vectorSpace.get(document);
                Scheduler.writer.write(document + "\t");
                for(int i = 0; i < tfidfValues.size(); i++) {
                    Scheduler.writer.write(tfidfValues.get(i).toString() + "\t");
                }
                Scheduler.writer.write("\n");
                Scheduler.writer.flush();
            }
            System.out.println("Done writing");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Scheduler.vectorSpace;
    }

    private class DocumentParser implements Runnable {
        private String document;
        private List<String> docTokens;
        private int num;

        DocumentParser(String document, List<String> docTokens, int number) {
            this.document = document;
            this.docTokens = docTokens;
            this.num = number;
        }

        @Override
        public void run() {
            if(this.num % 1000 == 0) {
                synchronized(System.out) {
                    System.out.println("Starting process " + this.num);
                }
            }
            List<LDA.Index> columns = IntStream.range(0, this.docTokens.size())
                .parallel()
                .mapToObj(index -> computeTFIDF(index))
                .filter(value -> value.isPresent())
                .map(value -> value.get())
                .collect(Collectors.toList());
            Scheduler.vectorSpace.put(document, columns);
            Scheduler.counter.countDown();
        }

        Optional<LDA.Index> computeTFIDF(int index) {
            double tfidfValue = Models.tfidf(docTokens.get(index), document);
            if(tfidfValue == 0) {
                return Optional.empty();
            } else {
                return Optional.of(new LDA.Index(index, tfidfValue));
            }
        }
    }
}