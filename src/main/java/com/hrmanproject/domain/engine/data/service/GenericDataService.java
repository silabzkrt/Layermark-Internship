package com.hrmanproject.domain.engine.data.service;

import com.hrmanproject.domain.engine.data.repository.GenericDataRepository;
import com.hrmanproject.domain.engine.metadata.MetadataCatalogService;
import com.hrmanproject.domain.engine.metadata.TableMetadata;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GenericDataService {

    private final GenericDataRepository genericDataRepository;
    private final MetadataCatalogService metadataCatalogService;

    public GenericDataService(GenericDataRepository genericDataRepository, MetadataCatalogService metadataCatalogService) {
        this.genericDataRepository = genericDataRepository;
        this.metadataCatalogService = metadataCatalogService;
    }

    public void createData(String tableName, Map<String, Object> data) {
        validateTableExists(tableName);
        // Here we could add column validation based on metadata (e.g. required fields, type checks)
        genericDataRepository.insert(tableName, data);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllData(String tableName) {
        validateTableExists(tableName);
        return genericDataRepository.findAll(tableName);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDataById(String tableName, Long id) {
        validateTableExists(tableName);
        return genericDataRepository.findById(tableName, id);
    }

    public void deleteData(String tableName, Long id) {
        validateTableExists(tableName);
        genericDataRepository.delete(tableName, id);
    }

    private void validateTableExists(String tableName) {
        TableMetadata metadata = metadataCatalogService.getTableMetadata(tableName).orElse(null);
        if (metadata == null) {
            throw new IllegalArgumentException("Table metadata not found for table: " + tableName);
        }
    }
}
