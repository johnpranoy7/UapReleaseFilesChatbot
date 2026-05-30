package com.jy.uap.uapreleasefileschatbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentLoaderService {

    private static final Logger log = LoggerFactory.getLogger(DocumentLoaderService.class);
    private static final String DOCUMENTS_LOCATION = "classpath:uapDocuments/**/*";

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public DocumentLoaderService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        // Keep chunks well below OpenAI's ~8k embedding limit (batching reserves 10%).
        this.textSplitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(0)
                .withMinChunkLengthToEmbed(1)
                .withMaxNumChunks(10_000)
                .build();
    }

    /**
     * Reads PDF and other supported files under {@code resources/uapDocuments}, splits
     * them into token-sized chunks, embeds them with the configured OpenAI embedding
     * model, and stores them in the pgvector-backed {@link VectorStore}.
     *
     * @return number of document chunks written to the vector store
     */
    public int loadDocuments() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(DOCUMENTS_LOCATION);

        List<Document> documents = new ArrayList<>();
        int filesLoaded = 0;
        for (Resource resource : resources) {
            if (!resource.isReadable() || resource.getFilename() == null) {
                continue;
            }
            if (resource.getFilename().startsWith(".")) {
                continue;
            }

            log.info("Reading {}", resource.getFilename());
            TikaDocumentReader reader = new TikaDocumentReader(resource);

            reader.read().forEach( (item) -> {
                item.getMetadata().put("fileName", resource.getFilename());
                documents.add(item);
            });

            filesLoaded++;
        }

        if (documents.isEmpty()) {
            log.warn("No documents found under resources/uapDocuments");
            return 0;
        }

        List<Document> chunks = textSplitter.apply(documents);
        log.info("Split {} source document(s) into {} chunk(s)", documents.size(), chunks.size());
        vectorStore.add(chunks);

        log.info("Stored {} chunk(s) from {} source file(s) in the vector store",
                chunks.size(), filesLoaded);
        return chunks.size();
    }

}
