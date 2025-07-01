package ch.gatzka.rest.controller;

import ch.gatzka.data.entity.Task;
import ch.gatzka.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<Page<Task>> getTasks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        log.debug("REST request to get Tasks - search: {}, completed: {}, page: {}, size: {}, sort: {}",
                search, completed, page, size, sort);

        PageRequest pageRequest = PageRequest.of(page, size, createSort(sort));
        Page<Task> result = taskService.getTasks(search, completed, pageRequest);

        log.debug("Returning {} tasks of {} total", result.getNumberOfElements(), result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        log.debug("REST request to get Task : {}", id);
        Task task = taskService.getTask(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        log.debug("REST request to create Task : {}", task.getTitle());

        if (task.getId() != null) {
            log.warn("New task shouldn't have an ID");
            return ResponseEntity.badRequest().build();
        }

        Task result = taskService.createTask(task);
        log.info("Created Task with id: {}", result.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        log.debug("REST request to update Task : {}", id);

        if (task.getId() == null || !task.getId().equals(id)) {
            log.warn("Invalid Task ID in request");
            return ResponseEntity.badRequest().build();
        }

        Task result = taskService.updateTask(id, task);
        log.info("Updated Task: {}", id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.debug("REST request to delete Task : {}", id);
        taskService.deleteTask(id);
        log.info("Deleted Task: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Task>> getTasksByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.debug("REST request to get Tasks between {} and {}", startDate, endDate);
        List<Task> tasks = taskService.getTasksByDateRange(startDate, endDate);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/open-count")
    public ResponseEntity<Long> getOpenTasksCount() {
        log.debug("REST request to count open Tasks");
        long count = taskService.getOpenTasksCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Task>> getRecentTasks(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        log.debug("REST request to get recent Tasks since {}", since);
        if (since == null) {
            since = LocalDateTime.now().minusDays(7); // Default: letzte 7 Tage
        }
        List<Task> tasks = taskService.getRecentTasks(since);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/recently-updated")
    public ResponseEntity<List<Task>> getRecentlyUpdatedTasks() {
        log.debug("REST request to get recently updated Tasks");
        List<Task> tasks = taskService.getRecentlyUpdatedTasks();
        return ResponseEntity.ok(tasks);
    }

    private Sort createSort(String[] sort) {
        if (sort[0].contains(",")) {
            String[] sortParams = sort[0].split(",");
            return Sort.by(sortParams[1].equals("desc") ?
                            Sort.Direction.DESC : Sort.Direction.ASC,
                    sortParams[0]);
        }
        return Sort.by(Sort.Direction.ASC, sort[0]);
    }


}
