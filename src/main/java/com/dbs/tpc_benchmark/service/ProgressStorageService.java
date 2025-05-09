package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.typings.vo.ProgressVO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProgressStorageService {
    private static final ConcurrentHashMap<String, ProgressVO> progressMap = new ConcurrentHashMap<>();

    @Transactional
    public String createTask(String tableName) {
        String taskId = UUID.randomUUID().toString();
        progressMap.put(taskId, ProgressVO.builder()
                .taskId(taskId)
                .tableName(tableName)
                .status("PROCESSING")
                .build());
        return taskId;
    }

    @Transactional
    public String removeTask(String taskId) {
        ProgressVO progressVO = progressMap.remove(taskId);
        if (progressVO != null) {
            return progressVO.getTableName();
        } else {
            return null;
        }
    }

    @Transactional
    public void updateProgress(String taskId, long totalLines, long processedLines, long totalBytes, long processedBytes) {
        ProgressVO progressVO = progressMap.get(taskId);
        if (progressVO != null) {
            progressVO.setTotalLines(totalLines);
            progressVO.setProcessedLines(processedLines);
            progressVO.setTotalBytes(totalBytes);
            progressVO.setProcessedBytes(processedBytes);
            progressVO.setPercentage(Math.ceil((double) processedBytes / totalBytes * 100));
        }
    }

    @Transactional
    public ProgressVO getProgress(String taskId) {
        return progressMap.getOrDefault(taskId, null);
    }

    public ProgressVO findProcessingProgressByTableName(String tableName) {
        tableName = tableName.toUpperCase();
        String finalTableName = tableName;
        return progressMap.values().stream()
                .filter(vo -> vo != null && finalTableName.equals(vo.getTableName()))
                .findFirst()
                .orElse(null);
    }
}
