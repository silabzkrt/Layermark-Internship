package com.hrmanproject.domain.engine.data.controller;

import com.hrmanproject.domain.engine.data.service.GenericDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data/{tableName}")
public class GenericDataController {

    private final GenericDataService genericDataService;

    public GenericDataController(GenericDataService genericDataService) {
        this.genericDataService = genericDataService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@PathVariable String tableName, @RequestBody Map<String, Object> data) {
        genericDataService.createData(tableName, data);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(@PathVariable String tableName) {
        List<Map<String, Object>> data = genericDataService.getAllData(tableName);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable String tableName, @PathVariable Long id) {
        Map<String, Object> data = genericDataService.getDataById(tableName, id);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String tableName, @PathVariable Long id) {
        genericDataService.deleteData(tableName, id);
        return ResponseEntity.ok().build();
    }
}
