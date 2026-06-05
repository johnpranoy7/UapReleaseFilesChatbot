package com.jy.uap.uapreleasefileschatbot.service;

import com.jy.uap.uapreleasefileschatbot.dto.DocumentLoadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentLoaderService {

    private static final Logger log = LoggerFactory.getLogger(DocumentLoaderService.class);
    private static final String DOCUMENTS_LOCATION = "classpath:uapDocuments/**/*";

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final TokenTextSplitter textSplitter;
    private final String vectorStoreTableName;

    public DocumentLoaderService(
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate,
            @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}") String vectorStoreTableName) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.vectorStoreTableName = vectorStoreTableName;
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
     * Loads documents only when the vector store table is empty.
     *
     * @return load outcome including chunk count and whether indexing was skipped
     */
    public DocumentLoadResult loadDocuments() throws IOException {
        if (!isVectorStoreEmpty()) {
            int existingChunks = countVectorStoreRecords();
            log.info("Vector store already contains {} chunk(s); skipping document load", existingChunks);
            return DocumentLoadResult.skipped(existingChunks);
        }

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
            return DocumentLoadResult.loaded(0);
        }

        List<Document> chunks = textSplitter.apply(documents);
        log.info("Split {} source document(s) into {} chunk(s)", documents.size(), chunks.size());
        vectorStore.add(chunks);

        log.info("Stored {} chunk(s) from {} source file(s) in the vector store",
                chunks.size(), filesLoaded);
        return DocumentLoadResult.loaded(chunks.size());
    }

    private boolean isVectorStoreEmpty() {
        return countVectorStoreRecords() == 0;
    }

    private int countVectorStoreRecords() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + vectorStoreTableName,
                Integer.class);
        return count == null ? 0 : count;
    }

}
