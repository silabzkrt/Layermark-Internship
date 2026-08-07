package com.hrmanproject.domain.engine.controller;

import com.hrmanproject.domain.engine.relation.RelationshipManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/relations")
public class RelationshipController {

    private final RelationshipManagerService relationshipManagerService;

    public RelationshipController(RelationshipManagerService relationshipManagerService) {
        this.relationshipManagerService = relationshipManagerService;
    }

    @PostMapping("/many-to-one")
    public ResponseEntity<Void> createManyToOne(@RequestBody Map<String, String> request) {
        String sourceTable = request.get("sourceTable");
        String targetTable = request.get("targetTable");
        String foreignKeyColumn = request.get("foreignKeyColumn");
        
        relationshipManagerService.createManyToOneRelation(sourceTable, targetTable, foreignKeyColumn);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/many-to-many")
    public ResponseEntity<Void> createManyToMany(@RequestBody Map<String, String> request) {
        String table1 = request.get("table1");
        String table2 = request.get("table2");
        String junctionTableName = request.get("junctionTableName");
        
        relationshipManagerService.createManyToManyRelation(table1, table2, junctionTableName);
        return ResponseEntity.ok().build();
    }
}
