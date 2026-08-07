package com.hrmanproject.domain.engine.controller;

import com.hrmanproject.domain.engine.index.IndexManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/indexes")
public class IndexManagementController {

    private final IndexManagerService indexManagerService;

    public IndexManagementController(IndexManagerService indexManagerService) {
        this.indexManagerService = indexManagerService;
    }

    @PostMapping("/{tableName}/{columnName}")
    public ResponseEntity<Void> createIndex(@PathVariable String tableName, @PathVariable String columnName) {
        indexManagerService.createIndex(tableName, columnName);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{tableName}/{columnName}")
    public ResponseEntity<Void> dropIndex(@PathVariable String tableName, @PathVariable String columnName) {
        indexManagerService.dropIndex(tableName, columnName);
        return ResponseEntity.ok().build();
    }
}
