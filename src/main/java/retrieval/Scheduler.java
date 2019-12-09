package retrieval;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.tomcat.jni.Time;

class Scheduler {
    private ExecutorService threadpool;
    private static Set<String> documents;
    private static ConcurrentHashMap<String, ArrayList<LDA.Index>> vectorSpace;
    private List<String> docTokens;
    private static CountDownLatch counter;

    Scheduler(Set<String> documents, List<String> docTokens) {
        this.threadpool = new ThreadPoolExecutor(32, 64, Time.APR_MSEC_PER_USEC, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>());
        Scheduler.documents = documents;
        this.docTokens = docTokens;
        Scheduler.counter = new CountDownLatch(documents.size());
        vectorSpace = new ConcurrentHashMap<>();
    }

    ConcurrentHashMap<String, ArrayList<LDA.Index>> executeParse() throws InterruptedException {

        documents.parallelStream().forEach(document -> {
            this.threadpool.submit(new DocumentParser(document, this.docTokens));
        });

        Scheduler.counter.await();
        return Scheduler.vectorSpace;
    }

    private class DocumentParser implements Runnable {
        private String document;
        private List<String> docTokens;

        DocumentParser(String document, List<String> docTokens) {
            this.document = document;
            this.docTokens = docTokens;
        }

        @Override
        public void run() {
            ArrayList<LDA.Index> columns = new ArrayList<>();
            for (int i = 0; i < docTokens.size(); i++) {
                double value = Models.tfidf(docTokens.get(i), document);
                if (value != 0) {
                    columns.add(new LDA.Index(i, value));
                }
            }
            Scheduler.vectorSpace.put(document, columns);
            Scheduler.counter.countDown();
        }

    }
}