package com.dbs.tpc_benchmark.service;

import com.dbs.tpc_benchmark.typings.vo.ProgressVO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProgressStorageService {
    @Autowired
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
    public void updateProgress(String taskId, long totalLines, long processedLines) {
        ProgressVO progressVO = progressMap.get(taskId);
        if (progressVO != null) {
            progressVO.setTotalLines(totalLines);
            progressVO.setProcessedLines(processedLines);
            progressVO.setPercentage(Math.ceil((double) processedLines / totalLines * 100));
        }
    }

    @Transactional
    public ProgressVO getProgress(String taskId) {
        return progressMap.getOrDefault(taskId, null);
    }
}
