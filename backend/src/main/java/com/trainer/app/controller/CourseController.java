package com.trainer.app.controller;

import com.trainer.app.dto.CourseCreateDto;
import com.trainer.app.dto.CourseResponseDto;
import com.trainer.app.dto.CourseProgressDto;
import com.trainer.app.dto.CertificateResponseDto;
import com.trainer.app.model.Course;
import com.trainer.app.model.CourseProgress;
import com.trainer.app.model.CourseCertificate;
import com.trainer.app.model.CourseFeedback;
import com.trainer.app.service.CourseService;
import com.trainer.app.service.UserService;
import com.trainer.app.service.CourseFeedbackService;
import com.trainer.app.service.CertificateService;
import com.trainer.app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CourseFeedbackService feedbackService;
    
    @Autowired
    private CertificateService certificateService;
    
    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CourseCreateDto courseDto) {
        try {
            CourseResponseDto createdCourse = courseService.createCourse(courseDto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course created successfully");
            response.put("course", createdCourse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create course: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId, @RequestBody CourseCreateDto courseDto) {
        try {
            CourseResponseDto updatedCourse = courseService.updateCourse(courseId, courseDto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course updated successfully");
            response.put("course", updatedCourse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update course: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/trainer/{trainerEmpId}")
    public ResponseEntity<?> getCoursesByTrainer(@PathVariable String trainerEmpId) {
        try {
            List<CourseResponseDto> courses = courseService.getCoursesByTrainerDto(trainerEmpId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch courses: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/trainee/{traineeEmpId}")
    public ResponseEntity<?> getCoursesForTrainee(@PathVariable String traineeEmpId) {
        try {
            // Get trainee's batch information
            User trainee = userService.findByEmpId(traineeEmpId);
            if (trainee == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trainee not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            String traineeBatch = trainee.getBatchName();
            List<CourseResponseDto> coursesWithProgress = courseService.getAvailableCoursesForTraineeDto(traineeEmpId, traineeBatch);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("courses", coursesWithProgress);
            response.put("traineeBatch", traineeBatch);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch courses: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseById(@PathVariable Long courseId) {
        try {
            CourseResponseDto course = courseService.getCourseByIdDto(courseId);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch course: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            courseService.deleteCourse(courseId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete course: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{courseId}/analytics")
    public ResponseEntity<?> getCourseAnalytics(@PathVariable Long courseId, @RequestParam String trainerEmpId) {
        try {
            // Get course details first
            CourseResponseDto course = courseService.getCourseByIdDto(courseId);
            
            // Verify trainer owns this course
            if (!course.getTrainerEmpId().equals(trainerEmpId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Access denied");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get real progress data
            List<CourseProgress> progressList = courseService.getCourseProgress(courseId);
            
            // Get certificates for this course
            List<CertificateResponseDto> certificates = certificateService.getCertificatesByCourse(courseId, trainerEmpId);
            
            // Get feedback for this course
            List<CourseFeedback> feedbackList = feedbackService.getFeedbackByCourse(courseId);
            
            List<Map<String, Object>> feedback = feedbackList.stream()
                .map(fb -> {
                    Map<String, Object> feedbackData = new HashMap<>();
                    feedbackData.put("id", fb.getId());
                    feedbackData.put("traineeEmpId", fb.getTraineeEmpId());
                    feedbackData.put("traineeName", fb.getTraineeName());
                    feedbackData.put("rating", fb.getRating());
                    feedbackData.put("keyLearnings", fb.getKeyLearnings());
                    feedbackData.put("feedback", fb.getFeedback());
                    feedbackData.put("submittedAt", fb.getSubmittedAt());
                    return feedbackData;
                })
                .toList();
            
            List<Map<String, Object>> enrollments = progressList.stream()
                .map(progress -> {
                    Map<String, Object> enrollment = new HashMap<>();
                    enrollment.put("id", progress.getId());
                    enrollment.put("traineeEmpId", progress.getTraineeEmpId());
                    
                    // Determine status based on progress data
                    String status;
                    if (progress.getCompleted() != null && progress.getCompleted()) {
                        status = "completed";
                    } else if (progress.getStarted() != null && progress.getStarted()) {
                        status = "in-progress";
                    } else {
                        status = "not-started";
                    }
                    
                    enrollment.put("status", status);
                    enrollment.put("progressPercentage", progress.getProgressPercentage() != null ? progress.getProgressPercentage() : 0);
                    enrollment.put("enrolledAt", progress.getStartedAt());
                    enrollment.put("completedAt", progress.getCompletedAt());
                    
                    // Get trainee name
                    try {
                        User trainee = userService.findByEmpId(progress.getTraineeEmpId());
                        enrollment.put("traineeName", trainee != null ? trainee.getName() : "Unknown");
                    } catch (Exception e) {
                        enrollment.put("traineeName", "Unknown");
                    }
                    
                    // Check for certificate
                    CertificateResponseDto traineCert = certificates.stream()
                        .filter(cert -> cert.getTraineeEmpId().equals(progress.getTraineeEmpId()))
                        .findFirst()
                        .orElse(null);
                    
                    enrollment.put("hasCertificate", traineCert != null);
                    if (traineCert != null) {
                        enrollment.put("certificateId", traineCert.getId());
                        enrollment.put("certificateFileName", traineCert.getFileName());
                        enrollment.put("certificateUploadedAt", traineCert.getUploadedAt());
                    }
                    
                    return enrollment;
                })
                .toList();
            
            // Calculate statistics
            long totalEnrollments = enrollments.size();
            long completedCount = enrollments.stream()
                .mapToLong(e -> "completed".equals(e.get("status")) ? 1 : 0)
                .sum();
            long inProgressCount = enrollments.stream()
                .mapToLong(e -> "in-progress".equals(e.get("status")) ? 1 : 0)
                .sum();
            long notStartedCount = enrollments.stream()
                .mapToLong(e -> "not-started".equals(e.get("status")) ? 1 : 0)
                .sum();
            
            // Calculate average rating from feedback
            double averageRating = feedback.isEmpty() ? 0.0 : 
                feedback.stream()
                    .mapToInt(f -> (Integer) f.get("rating"))
                    .average()
                    .orElse(0.0);
            
            double completionPercentage = totalEnrollments > 0 ? 
                enrollments.stream()
                    .mapToInt(e -> (Integer) e.get("progressPercentage"))
                    .average()
                    .orElse(0.0) : 0.0;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("course", Map.of(
                "id", course.getId(),
                "title", course.getTitle(),
                "description", course.getDescription(),
                "duration", course.getDuration(),
                "status", course.getStatus(),
                "instructor", course.getInstructor(),
                "trainerEmpId", course.getTrainerEmpId()
            ));
            response.put("enrollments", enrollments);
            response.put("feedback", feedback); // Real feedback data
            response.put("statistics", Map.of(
                "totalEnrollments", totalEnrollments,
                "completedCount", completedCount,
                "inProgressCount", inProgressCount,
                "notStartedCount", notStartedCount,
                "averageRating", Math.round(averageRating * 10.0) / 10.0,
                "feedbackCount", (long) feedback.size(),
                "completionPercentage", Math.round(completionPercentage * 10.0) / 10.0
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch analytics: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/progress/start")
    public ResponseEntity<?> startCourse(@RequestBody CourseProgressDto progressDto) {
        try {
            courseService.startCourse(progressDto.getCourseId(), progressDto.getTraineeEmpId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course started successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to start course: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/progress/{courseId}")
    public ResponseEntity<?> getCourseProgress(@PathVariable Long courseId) {
        try {
            List<CourseProgress> progressList = courseService.getCourseProgress(courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("progress", progressList);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch progress: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/progress/update")
    public ResponseEntity<?> updateCourseProgress(@RequestBody CourseProgressDto progressDto) {
        try {
            courseService.updateCourseProgress(progressDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Progress updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update progress: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/progress/complete")
    public ResponseEntity<?> completeCourse(@RequestBody CourseProgressDto progressDto) {
        try {
            courseService.completeCourse(progressDto.getCourseId(), progressDto.getTraineeEmpId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course completed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to complete course: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}