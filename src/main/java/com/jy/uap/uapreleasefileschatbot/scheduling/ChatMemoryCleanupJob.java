package com.jy.uap.uapreleasefileschatbot.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "chat.memory.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class ChatMemoryCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ChatMemoryCleanupJob.class);

    private final JdbcTemplate jdbcTemplate;
    private final int retentionDays;

    public ChatMemoryCleanupJob(
            JdbcTemplate jdbcTemplate,
            @Value("${chat.memory.cleanup.retention-days:2}") int retentionDays) {
        this.jdbcTemplate = jdbcTemplate;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "${chat.memory.cleanup.cron:0 0 23 * * *}")
    public void purgeStaleChatMemory() {
        String sql = """
                DELETE FROM SPRING_AI_CHAT_MEMORY
                WHERE "timestamp" < NOW() - (? * INTERVAL '1 day')
                """;

        int deleted = jdbcTemplate.update(sql, retentionDays);
        log.info("Chat memory cleanup removed {} record(s) older than {} day(s)", deleted, retentionDays);
    }
}
