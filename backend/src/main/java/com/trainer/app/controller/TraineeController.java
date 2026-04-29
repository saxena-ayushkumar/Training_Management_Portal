package com.trainer.app.controller;

import com.trainer.app.dto.TraineeUpdateDto;
import com.trainer.app.model.User;
import com.trainer.app.service.TraineeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/trainees")
@CrossOrigin(origins = "http://localhost:3000")
public class TraineeController {

    @Autowired
    private TraineeService traineeService;

    @GetMapping("/{empId}")
    public ResponseEntity<?> getTraineeByEmpId(@PathVariable String empId) {
        try {
            User trainee = traineeService.getTraineeByEmpId(empId);
            
            Map<String, Object> traineeData = new HashMap<>();
            traineeData.put("id", trainee.getId());
            traineeData.put("name", trainee.getName());
            traineeData.put("email", trainee.getEmail());
            traineeData.put("empId", trainee.getEmpId());
            traineeData.put("batchName", trainee.getBatchName());
            traineeData.put("trainerEmpId", trainee.getTrainerEmpId());
            traineeData.put("status", trainee.getStatus());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trainee", traineeData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching trainee: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{empId}")
    public ResponseEntity<Map<String, Object>> updateTrainee(@PathVariable String empId, @RequestBody TraineeUpdateDto updateDto) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User updatedTrainee = traineeService.updateTrainee(empId, updateDto);
            
            response.put("success", true);
            response.put("message", "Trainee profile updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating trainee profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}